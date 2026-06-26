# ShanLink 闪链

<p align="center">
  <img src="resources/image/logo.png" width="120" alt="ShanLink Icon" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue" />
  <img src="https://img.shields.io/badge/Spring Boot-3.0.7-brightgreen" />
  <img src="https://img.shields.io/badge/Vue-3-42b883" />
  <img src="https://img.shields.io/badge/ShardingSphere-5.3.2-orange" />
</p>

<p align="center">基于 Spring Boot 3 单体架构的高性能短链接系统，重点解决高并发跳转场景下的缓存三大问题</p>

---

## 功能

| 模块 | 功能 |
|------|------|
| 短链接 | 创建 / 批量创建 / 修改 / 删除 / 跳转 |
| 分组 | 创建 / 修改 / 排序 / 统计分组内链接数 |
| 回收站 | 移入 / 恢复 / 彻底删除 |
| 访问统计 | PV / UV / UIP，按小时/星期/地区/浏览器/设备/系统/网络多维度统计 |
| 用户 | 注册 / 登录 / 个人信息管理 |

---

## 核心设计

### 跳转

```
GET /{short-uri}
  │
  ├─ ① Redis 正向缓存命中 ──────────────────────── sendRedirect（纳秒级）
  │
  ├─ ② 布隆过滤器不存在 ───────────────────────── 404（拦截不存在短链，防穿透）
  │
  ├─ ③ Redis 空值缓存命中 ──────────────────────── 404（拦截已删除/过期，BF 感知不到删除）
  │
  └─ ④ Redisson 分布式锁 + 双重检查 + 回源 DB
        ├─ 查 t_link_goto 路由表获取 gid
        ├─ 带 gid 查 t_link（ShardingSphere 分片键）
        ├─ 写正向缓存 / 空值缓存
        └─ sendRedirect
```

### 数据库分片

`t_link`、`t_link_goto`、`t_group` 均按 `gid` 水平分为 **16 张表**，由 ShardingSphere 自动路由。`t_link_goto` 作为路由表，解决"按 `full_short_url` 查询无法确定分片键"的问题。

### 访问统计异步化

跳转请求在 Web 线程提取 UA / IP / Cookie 数据后发送 RocketMQ 消息，消费者异步写入 **8 张统计表**，不阻塞跳转主流程：

| 表 | 维度 |
|----|------|
| `t_link_access_stats` | PV / UV / UIP（按小时/星期） |
| `t_link_access_logs` | 原始访问日志 |
| `t_link_browser_stats` | 浏览器分布 |
| `t_link_device_stats` | 设备分布 |
| `t_link_os_stats` | 操作系统分布 |
| `t_link_network_stats` | 网络类型分布 |
| `t_link_locale_stats` | 省市地区分布 |
| `t_link_stats_today` | 今日实时统计 |

MQ 消费者使用 `MessageQueueIdempotentHandler` 实现幂等，防止重复计数。

---

## 技术栈

| 分类 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.0.7 |
| ORM | MyBatis-Plus 3.5.15 |
| 分库分表 | ShardingSphere 5.3.2（按 `gid` 分 16 片） |
| 缓存 | Redis + Redisson 3.27.2 |
| 分布式锁 / BF | Redisson（布隆过滤器防穿透，分布式锁防击穿） |
| 消息队列 | RocketMQ 5.1.4（统计异步落库） |
| 工具库 | Hutool 5.8.27 / TTL 2.14.3 |
| 前端 | Vue3 + Pinia + Element Plus |

---

## 前置依赖

| 依赖 | 地址 | 说明 |
|------|------|------|
| MySQL | `localhost:3306` | 建库 `shanlink`，执行 `resources/database/link.sql` |
| Redis | `127.0.0.1:6379` | 无需额外配置 |
| RocketMQ | `127.0.0.1:9876` | `docker/rocketmq/docker-compose.yml` 一键启动 |

```bash
# 启动 RocketMQ
docker compose -f docker/rocketmq/docker-compose.yml up -d
```

---

## 快速启动

```bash
# 构建
mvn clean package -DskipTests

# 启动后端（端口 8000）
mvn spring-boot:run

# 启动前端（端口 5173）
cd dashboard && pnpm install && pnpm dev
```

---

## API 路径规范

```
用户 / 分组：POST /api/shan-link/admin/v1/{resource}
短链业务：  POST /api/shan-link/v1/{resource}
短链跳转：  GET  /{short-uri}
```
