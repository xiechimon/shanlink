# CLAUDE.md

## 项目概述
闪链（ShanLink）短链系统，Spring Boot 3 单体应用（:8000，单一 Maven 模块、单一启动类 `ShanLinkApplication`），内含用户/分组管理与短链核心两大业务；前端 `dashboard`（Vue3）。

## 环境
Java 17、Spring Boot 3.0.7、MyBatis-Plus 3.5.15、ShardingSphere 5.3.2、Redisson 3.27.2、Hutool 5.8.27、TTL 2.14.3、RocketMQ 5.1.4（starter 2.3.0）。
前置依赖：MySQL（`shanlink` 库）、Redis（127.0.0.1:6379）、RocketMQ（127.0.0.1:9876，`docker/rocketmq/docker-compose.yml` 一键启动）。

## 常用命令
```bash
mvn clean package -DskipTests     # 构建
mvn spring-boot:run               # 运行
mvn test -Dtest=Foo#bar           # 单测
```

## 包结构
```
com.xmon.shanlink
├── common/biz.user     # UserContext、UserTransmitFilter
├── common/constant     # Redis Key、TTL 常量
├── common/convention   # errorcode / exception / result
├── common/database     # BaseDO
├── config              # 配置类（布隆过滤器等）
├── controller / service / dao/entity / dto / mq / toolkit
```

## 编码规范

### 实体类（DO）
继承 `BaseDO`（含 `createTime`/`updateTime`/`delFlag`）。每个字段**必须加 Javadoc**，内容取自 SQL COMMENT。主键用 `@TableId(type = IdType.AUTO)`。

### DTO
每个操作独立 DTO，即使字段相同也不复用（语义不同、各自可独立演化）：
`SaveReqDTO` / `RecoverReqDTO` / `RemoveReqDTO` / `PageReqDTO`。

### Service 接口
方法必须保留 `@param requestParam 请求参数` 注释，不得删除。

### 对象映射
用 `BeanUtil.toBean` / `BeanUtil.convertIgnoreNull`，禁用 Spring `BeanUtils.copyProperties`。

### 统一响应
`Results.success()` / `Results.success(data)` / `Results.failure(ex)`。

### 异常
`ClientException`（用户端）/ `ServiceException`（服务端）/ `RemoteException`（远程调用）。
错误码枚举实现 `IErrorCode`，前缀：`A`=客户端，`B`=服务端，`C`=远程。

### Redis Key
格式：`shan-link:{业务}:{资源}`，层级用 `:`，同层用 `-`，禁混 `_`。
全部常量放 `RedisCacheConstant`，禁止硬编码 Key 字符串。

### API 路径
用户/分组：`/api/shan-link/admin/v1/{resource}`（示例 `/api/shan-link/admin/v1/user/{username}`）；短链业务：`/api/shan-link/v1/{resource}`（示例 `/api/shan-link/v1/link`）。

### 依赖版本
统一在 `pom.xml` `<dependencyManagement>` 声明，业务依赖不写版本号。Lombok 全局依赖。

## 注意事项
- ShardingSphere 分片键为 `gid`，查 `t_link` 必须带 `gid`，否则广播全分片。
- `@Transactional` 事务内不能写布隆过滤器，事务回滚后 BF 无法撤销。
- 鉴权由应用内 `UserTransmitFilter` 完成：从请求 Header 解析用户信息（登录态查 Redis）写入 `UserContext`；请求结束 `finally` 必须 `removeUser()`。
- 访问统计经 RocketMQ 异步落库：生产者（`saveStats`）在 Web 线程提取请求数据（UA/IP/Cookie/Redis 去重）后发消息，消费者（`ShortLinkStatsSaveConsumer`）幂等写 8 张统计表（`actualSaveStats`）。MQ 组件在 `mq/{producer,consumer,idempotent}` 包，消息至少投递一次，靠 `MessageQueueIdempotentHandler` 防重复计数。
