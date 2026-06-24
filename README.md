# ShanLink 闪链

<p align="center">
  <img src="resources/image/icon.png" width="120" alt="ShanLink Icon" />
</p>

<p align="center">基于 Spring Boot 3 微服务架构的高性能短链接系统</p>

## 模块

| 模块 | 端口   | 职责 |
|---|------|---|
| `gateway` | 8000 | 网关、鉴权、请求路由 |
| `admin` | 8002 | 用户、分组、短链接后台管理 |
| `project` | 8001 | 短链接核心（创建、跳转、统计） |
| `dashboard` | 5173 | Vue3 前端 |

## 技术栈

- **Java 17** + **Spring Boot 3.0.7**
- **MyBatis-Plus 3.5.15** + **ShardingSphere 5.3.2**（按 `gid` 分片）
- **Redisson 3.27.2**（分布式锁、布隆过滤器）
- **Redis**（缓存预热、空值缓存、跳转加速）
- **Vue3** + **Pinia** + **Element Plus**

## 功能

- 短链接创建 / 修改 / 删除（支持批量创建）
- 短链接跳转（布隆过滤器 + 空值缓存防穿透/击穿）
- 回收站（移入 / 恢复 / 彻底删除）
- 分组管理
- 访问统计（PV / UV / UIP）
- 用户注册 / 登录 / JWT 鉴权

## 前置依赖

- MySQL：创建数据库 `shanlink`，执行 `resources/` 下 SQL 文件
- Redis：`127.0.0.1:6379`

## 快速启动

```bash
# 构建
mvn clean package -DskipTests

# 启动顺序：gateway → admin → project
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl admin
mvn spring-boot:run -pl project

# 前端
cd dashboard && pnpm install && pnpm dev
```

## API 路径规范

```
/api/shan-link/{module}/v1/{resource}
```

示例：`GET /api/shan-link/admin/v1/user/{username}`
