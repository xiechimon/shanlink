# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

闪链（ShanLink）是一个短链系统，采用 Spring Boot 3 微服务架构。

**模块**

| 模块          | 描述          | 端口   |
|-------------|-------------|------|
| `admin`     | 用户管理后台服务    | 8002 |
| `project`   | 短链核心业务服务    | —    |
| `gateway`   | 网关服务        | 8000 |
| `dashboard` | Vue3 前端管理面板 | —    |

## 构建与运行

```bash
# 构建所有模块（根目录）
mvn clean package -DskipTests

# 构建单个模块
mvn clean package -pl admin -DskipTests

# 运行 admin 服务
mvn spring-boot:run -pl admin

# 运行单个测试类
mvn test -pl admin -Dtest=UserServiceTest

# 运行单个测试方法
mvn test -pl admin -Dtest=UserServiceTest#testRegister
```

前置依赖：MySQL（`shanlink` 库，见 `resources/database/link_v0.sql`）、Redis（默认 127.0.0.1:6379）。

## 技术栈

- **Java 17**、Spring Boot 3.0.7、Spring Cloud 2022.0.3、Spring Cloud Alibaba
- **MyBatis-Plus** 3.5.15（Spring Boot 3 版）
- **ShardingSphere** 5.3.2（分库分表）
- **Redisson** 3.27.2（Redis 客户端）
- **Fastjson2** 2.0.36（JSON 序列化，用于 Redis 存取对象）
- **Hutool** 5.8.27（工具库）
- **TTL** 2.14.3（`TransmittableThreadLocal`，跨线程池传递用户上下文）
- **Lombok**（全局依赖，无需子模块单独引入）
- 前端：Vue3 + Element Plus + Vite

## 包结构（以 admin 为例）

```
com.xmon.shanlink.admin
├── common
│   ├── biz.user        # 用户上下文（UserContext、UserInfoDTO、UserTransmitFilter）
│   ├── constant        # 常量类（Redis Key、TTL 等）
│   ├── convention
│   │   ├── errorcode   # 错误码接口与基础枚举
│   │   ├── exception   # 异常体系
│   │   └── result      # 统一响应对象
│   ├── database        # BaseDO（公共字段基类）
│   ├── enums           # 业务枚举（错误码）
│   ├── serialize       # 自定义 Jackson 序列化器
│   └── web             # 全局异常处理
├── config              # 配置类（布隆过滤器、MetaObjectHandler、用户过滤器注册）
├── controller          # 控制层
├── dao
│   └── entity          # 数据库实体 (DO)
├── dto                 # 请求/响应 DTO
├── remote              # 远程调用
├── service             # 业务层
└── toolkit             # 工具类（RandomGenerator 等）
```

## 编码规范

### 实体类（DO）

继承 `BaseDO`（已包含 `createTime`、`updateTime`、`delFlag` 及对应 `@TableField(fill=...)` 注解），自定义字段只加业务列。每个字段
**必须添加 Javadoc 注释**，内容取自 SQL `COMMENT`。

```java

@Data
@Builder
@TableName("t_group")
public class GroupDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    private String gid;
}
```

- 主键：`@TableId(type = IdType.AUTO)`
- 时间字段用 `Date`，删除标识用 `Integer`
- 自动填充字段若不在 `BaseDO` 中，须手动加 `@TableField(fill = ...)`

### 统一响应

所有 Controller 返回值使用 `Result<T>`，通过 `Results` 工厂方法构造：

```java
return Results.success();           // 成功（无数据）
return Results.

success(userVO);     // 成功（带数据）
return Results.

failure(ex);         // 失败
```

### 异常体系

| 类型                 | 场景                  |
|--------------------|---------------------|
| `ClientException`  | 用户端错误（参数校验、用户名已存在等） |
| `ServiceException` | 服务端内部错误             |
| `RemoteException`  | 调用第三方服务失败           |

```java
throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
```

### 错误码

业务模块新增错误码时，创建独立枚举实现 `IErrorCode`，遵循前缀规范：

- `A` 开头：客户端错误（用户输入、权限、业务校验）
- `B` 开头：服务端错误（系统执行失败）
- `C` 开头：远程调用错误

```java
public enum UserErrorCodeEnum implements IErrorCode {
    USER_NULL("A000200", "用户记录不存在"),
    USER_NAME_EXIST("A000201", "用户名已存在"),
    USER_PASSWORD_ERROR("A000203", "用户密码错误"),
    USER_SAVE_ERROR("B000200", "用户记录新增失败");
}
```

### 对象映射

禁止手写 getter/setter 赋值，使用 Hutool 的 `BeanUtil`：

```java
// 单对象转换
UserDO userDO = BeanUtil.toBean(reqDTO, UserDO.class);

// 更新时忽略空字段
BeanUtil.

convertIgnoreNull(updateReqDTO, existingDO);
```

禁止使用 Spring 的 `BeanUtils.copyProperties`。

### 参数校验

DTO 字段加 `@NotBlank` 等注解，Controller 方法加 `@Validated`，`GlobalExceptionHandler` 已统一处理
`MethodArgumentNotValidException`：

```java
// DTO
@NotBlank(message = "用户名不能为空")
private String username;

// Controller
public Result<Void> register(@RequestBody @Validated UserRegisterReqDTO requestParam)
```

### 敏感字段脱敏

DTO 中手机号、身份证字段使用 `@JsonSerialize` 注解，序列化时自动打码：

```java

@JsonSerialize(using = PhoneDesensitizationSerializer.class)
private String phone;
```

### 布隆过滤器

`RBloomFilter` 在 `RBloomFilterConfiguration` 中注册为 Spring Bean，`tryInit` 直接在 `@Bean` 方法里调用：

```java

@Bean
public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient) {
    RBloomFilter<String> bf = redissonClient.getBloomFilter("userRegisterCachePenetrationBloomFilter");
    bf.tryInit(100000000L, 0.001);
    return bf;
}
```

- `tryInit` 若 Redis 中已存在该 BF 则返回 `false`，数据不被清空
- BF 只能减少查库，不能保证唯一性，**数据库必须加唯一索引兜底**

### 注册安全

三层防护，缺一不可：

```java
// 1. BF 前置拦截（减少查库）
if(bloomFilter.contains(username)){
        throw new

ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
}
// 2. 分布式锁（防相同用户名并发注册）
RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + username);
if(!lock.

tryLock()){
        throw new

ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
}
        try{

save(userDO);
    bloomFilter.

add(username);
}catch(
DuplicateKeyException e){
        // 3. 唯一索引兜底（防极端竞态）
        throw new

ClientException(UserErrorCodeEnum.USER_EXIST);
}finally{
        lock.

unlock();
}
```

- `tryLock()` 不等待，抢不到直接拒绝，锁粒度为用户名级别
- 有 `@Transactional` 时，**不能**在事务内写 BF，否则事务回滚后 BF 数据无法撤销

### 用户登录

登录态基于 Redis Hash 存储，token 为 UUID：

```
Key:   shan-link:login:{username}   (Hash)
Field: token (UUID)
Value: 用户信息 JSON（Fastjson2 序列化）
TTL:   USER_LOGIN_TTL（默认 30 天）
```

- 已登录则续期并返回已有 token，不重复生成
- 用 `StringRedisTemplate` 操作 Hash（值为明文 JSON，便于调试）；避免用 `RMap`（Redisson 默认二进制序列化，Redis CLI 查看乱码）
- 所有过期时间等魔法值抽取到 `RedisCacheConstant`

### 用户上下文传递

Gateway 鉴权后将用户信息注入请求头，下游服务通过 `UserTransmitFilter` 读取并存入 `TransmittableThreadLocal`：

```
请求
 ├─ Gateway：鉴权 → 写入 Header（username / userId / realName）
 ├─ UserTransmitFilter（order=0）：读 Header → UserContext.setUser(UserInfoDTO)
 ├─ Controller / Service：UserContext.getUsername() / getUserId() / getRealName()
 └─ 请求结束（finally）：UserContext.removeUser()  ← 必须清除，防内存泄漏
```

`UserContext` 使用 `TransmittableThreadLocal`，可跨线程池（异步任务）透传用户信息。

### Redis Key 命名规范

- 用 `:` 分隔命名空间层级，同一段内统一用 `-`，禁止混用 `_` 和 `-`
- 格式：`shan-link:{业务}:{资源}:`

```java
"shan-link:lock:user-register:"   // 正确
        "shan-link:login:"                // 正确
        "shan-link:lock_user-register:"   // 错误，混用了 _ 和 -
```

所有 Key 和 TTL 常量统一放在 `RedisCacheConstant`。

### API 路径规范

```
/api/shan-link/{module}/v1/{resource}
```

示例：`/api/shan-link/admin/v1/user/{username}`

### 依赖版本管理

所有依赖版本统一在根模块 `shanlink-all/pom.xml` 的 `<dependencyManagement>` 中声明，版本号提取为 `<properties>`：

```xml
<!-- 根 pom.xml -->
<properties>
    <jsoup.version>1.15.3</jsoup.version>
</properties>

<dependencyManagement>
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>${jsoup.version}</version>
    </dependency>
</dependencyManagement>
```

子模块引用时**不写版本号**：

```xml
<!-- 子模块 pom.xml -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
</dependency>
```

Lombok 是全局依赖，直接声明在根 pom 的 `<dependencies>` 中，子模块无需引入。
