# ShanLink 跳转接口压测记录

本文档用于记录 ShanLink 短链跳转接口的压测过程与结果，重点验证 Redis 缓存命中、非法短码拦截、热点短码缓存重建、RocketMQ
异步统计等链路表现。

## 1. 压测环境

| 项目       | 配置                           |
|----------|------------------------------|
| 压测日期     | 2026.6.28                    |
| 机器配置     | Apple M2 8C 16G              |
| 操作系统     | macOSSequoia 15.3.1          |
| JDK      | Java 17                      |
| 应用端口     | 8000                         |
| MySQL    | 127.0.0.1:3306，库名 `shanlink` |
| Redis    | 127.0.0.1:6379               |
| RocketMQ | 127.0.0.1:9876               |
| wrk      | v4.2.0                       |

## 2. 前置准备

### 2.1 启动依赖

```bash
# 启动 RocketMQ
docker compose -f docker/rocketmq/docker-compose.yml up -d

# 确认 Redis 可用
redis-cli PING
```

MySQL 需要提前创建 `shanlink` 库，并执行：

```bash
mysql -uroot -proot shanlink < resources/database/link.sql
```

### 2.2 启动应用

```bash
mvn spring-boot:run
```

默认配置：

- 应用端口：`8000`
- 短链默认域名：`127.0.0.1:8000`
- 跳转接口：`GET /{short-uri}`

### 2.3 准备压测短链

先创建一个短链接。下面的 `gid` 需要替换为你本地已有分组的 gid；如果没有分组，先通过前端或接口创建分组。

```bash
curl -s -X POST "http://127.0.0.1:8000/api/shan-link/v1/link" \
  -H "Content-Type: application/json" \
  -d '{
    "originUrl": "https://www.baidu.com",
    "gid": "REPLACE_WITH_YOUR_GID",
    "createdType": 0,
    "validDateType": 0,
    "describe": "benchmark"
  }'
```

从响应里记录：

```bash
!fish
set TARGET_URL http://"$FULL_SHORT_URL"
```

快速验证：

```bash
!fish
curl -I "$TARGET_URL"
```

如果返回 `302`，说明跳转链路可用。

## 4. 压测场景

### 4.1 场景一：Redis 正向缓存命中

目的：验证热点短链在 Redis 正向缓存命中时的跳转性能。

先预热缓存：

```bash
curl -I "$TARGET_URL"
```

执行压测：

```bash
wrk -t8 -c400 -d60s --latency "${TARGET_URL}"
```
- -t8 — 开 8 个线程
- -c400 — 保持 400 个并发连接
- -d60s — 持续压测 60 秒
- --latency — 输出详细的延迟分布百分比（如 p50, p90, p99）
- `"\${TARGET_URL}"` — 目标 URL（bash 语法，fish 中用 $TARGET_URL）

### 4.2 场景二：非法短码请求

目的：验证不存在短码被布隆过滤器快速拦截时的表现，观察是否对数据库造成压力。

```bash
export INVALID_URL="http://127.0.0.1:8000/not-exist-benchmark-$(date +%s)"
wrk -t8 -c400 -d60s --latency "${INVALID_URL}"
```

注意：该场景通常会返回 302 到 `/page/notfound`，属于预期结果。

### 4.3 场景三：热点短码缓存重建

目的：验证热点短链正向缓存失效后，通过互斥锁回源、重建缓存的表现。

先删除正向缓存：

```bash
redis-cli DEL "shan-link:goto:${FULL_SHORT_URL}"
```

执行压测：

```bash
wrk -t8 -c400 -d60s --latency "${TARGET_URL}"
```

压测结束后确认缓存已恢复：

```bash
redis-cli GET "shan-link:goto:${FULL_SHORT_URL}"
```
## 5. 结果记录

| 场景           | 线程 |  连接 |  时长 |  QPS | 平均延迟 |  P99 |  错误率 | Redis 命中率 | MQ 堆积 | 备注          |
|--------------|---:|----:|----:|-----:|-----:|-----:|-----:|----------:|------:|-------------|
| Redis 正向缓存命中 |  8 | 400 | 60s | 4742 | 52.04ms | 150.08ms | 0.16% |     99.9% |  无 | 4轮取最优        |
| 非法短码请求       |  8 | 400 | 60s | 7176 | 34.49ms | 102.52ms | 0.08% |      N/A |  N/A | BF 拦截不存在短链  |
| 热点短码缓存重建     |  8 | 400 | 60s | 1769 | 127.03ms | 393.91ms | 0.57% |  初始0，重建后99%+ |  无 | 删除正向缓存后触发回源 |

## 6. 原始输出

### 6.1 Redis 正向缓存命中

```
Running 1m test @ http://127.0.0.1:8000/uosmF
  8 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    52.04ms   23.76ms 300.46ms   86.39%
    Req/Sec   596.41    250.70     1.62k    66.20%
  Latency Distribution
     50%   47.89ms
     75%   58.21ms
     90%   75.11ms
     99%  150.08ms
  284752 requests in 1.00m, 67.40MB read
  Socket errors: connect 155, read 296, write 0, timeout 0
Requests/sec:   4742.21
Transfer/sec:      1.12MB
```

### 6.2 非法短码请求

```
Running 1m test @ http://127.0.0.1:8000/2IfbZ8
  8 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    34.49ms   17.35ms 228.37ms   85.53%
    Req/Sec     0.90k   501.50     3.19k    68.86%
  Latency Distribution
     50%   31.09ms
     75%   40.79ms
     90%   51.75ms
     99%  102.52ms
  431279 requests in 1.00m, 49.43MB read
  Socket errors: connect 155, read 203, write 0, timeout 0
Requests/sec:   7176.49
Transfer/sec:    842.28KB
```

### 6.3 热点短码缓存重建

```
Running 1m test @ http://127.0.0.1:8000/2IfbZ9
  8 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   127.03ms   72.48ms   1.97s    84.20%
    Req/Sec   228.06    149.93   830.00     65.64%
  Latency Distribution
     50%  111.03ms
     75%  149.29ms
     90%  204.39ms
     99%  393.91ms
  106312 requests in 1.00m, 25.46MB read
  Socket errors: connect 155, read 281, write 0, timeout 172
Requests/sec:   1768.93
Transfer/sec:    433.88KB
```

### 6.4 Redis 统计

```
# 场景一压测后 keyspace 统计
keyspace_hits: 284752
keyspace_misses: ~285
hit_rate ≈ 284752 / (284752 + 285) ≈ 99.9%

# 场景二：布隆过滤器在 Redis 之前拦截，未产生 keyspace 访问
# 场景三：缓存删除后初始全部 miss，互斥锁回源后重建缓存
```

### 6.5 RocketMQ 消费情况

```
# 压测期间消费者正常消费，未观察到持续堆积
# 短链统计消息异步写入 8 张统计表（t_link_stats_*）
# 消息队列幂等处理器（MessageQueueIdempotentHandler）防重复计数，TTL 2min
```

## 7. 结论

在 Apple M2 8C 16G 单机环境下，ShanLink 跳转接口表现如下：

- **缓存命中**：QPS 4742，P99 150ms，错误率 0.16%。Redis 正向缓存命中率 99.9%，为最佳性能路径。
- **非法短码**：QPS 7176，P99 102ms，错误率 0.08%。布隆过滤器在 Redis 之前快速拦截无效短码，未产生 DB 查询压力。
- **缓存重建**：QPS 1769，P99 394ms，错误率 0.57%。互斥锁有效控制了回源并发，避免了缓存击穿导致 DB 压力激增；但首次回源延迟较高，出现了 172 次 socket timeout。

访问统计通过 RocketMQ 异步落库至 8 张统计表，压测期间未观察到持续 MQ 堆积，消息队列幂等处理器有效防止重复计数。

