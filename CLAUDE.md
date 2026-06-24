# CLAUDE.md

## 项目概述
闪链（ShanLink）短链系统，Spring Boot 3 微服务：`admin`（:8002 用户管理）、`project`（短链核心）、`gateway`（:8000）、`dashboard`（Vue3 前端）。

## 环境
Java 17、Spring Boot 3.0.7、MyBatis-Plus 3.5.15、ShardingSphere 5.3.2、Redisson 3.27.2、Hutool 5.8.27、TTL 2.14.3。
前置依赖：MySQL（`shanlink` 库）、Redis（127.0.0.1:6379）。

## 常用命令
```bash
mvn clean package -DskipTests           # 构建全部
mvn clean package -pl admin -DskipTests # 构建单模块
mvn spring-boot:run -pl admin           # 运行
mvn test -pl admin -Dtest=Foo#bar       # 单测
```

## 包结构（以 admin 为例）
```
com.xmon.shanlink.admin
├── common/biz.user     # UserContext、UserTransmitFilter
├── common/constant     # Redis Key、TTL 常量
├── common/convention   # errorcode / exception / result
├── common/database     # BaseDO
├── config              # 配置类（布隆过滤器等）
├── controller / service / dao/entity / dto / remote / toolkit
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
`/api/shan-link/{module}/v1/{resource}`，示例：`/api/shan-link/admin/v1/user/{username}`

### 依赖版本
统一在根 `pom.xml` `<dependencyManagement>` 声明，子模块不写版本号。Lombok 全局依赖，子模块无需引入。

## 注意事项
- ShardingSphere 分片键为 `gid`，查 `t_link` 必须带 `gid`，否则广播全分片。
- `@Transactional` 事务内不能写布隆过滤器，事务回滚后 BF 无法撤销。
- Gateway 鉴权后将用户信息写入 Header，下游通过 `UserContext` 读取；请求结束 `finally` 必须 `removeUser()`。
