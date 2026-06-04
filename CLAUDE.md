# ShanLink 项目规范

## 项目概述

闪链（ShanLink）是一个短链系统，采用 Spring Boot 3 微服务架构。

**模块**

| 模块          | 描述          |
|-------------|-------------|
| `admin`     | 用户管理后台服务    |
| `project`   | 短链核心业务服务    |
| `gateway`   | 网关服务        |
| `dashboard` | Vue3 前端管理面板 |

## 技术栈

- **Java 17**、Spring Boot 3.0.7、Spring Cloud 2022.0.3、Spring Cloud Alibaba
- **MyBatis-Plus** 3.5.15（Spring Boot 3 版）
- **ShardingSphere** 5.3.2（分库分表）
- **Redisson** 3.27.2（Redis 客户端）
- **Dozer** 6.5.2（对象属性映射）
- **Hutool** 5.8.27（工具库）
- **Lombok**（全局依赖，无需子模块单独引入）
- 前端：Vue3 + Element Plus + Vite

## 包结构（以 admin 为例）

```
com.xmon.shanlink.admin
├── common
│   ├── convention
│   │   ├── errorcode   # 错误码接口与基础枚举
│   │   ├── exception   # 异常体系
│   │   └── result      # 统一响应对象
│   ├── enums           # 业务枚举
│   ├── serialize       # 自定义 Jackson 序列化器
│   └── web             # 全局异常处理
├── config              # 配置类
├── controller          # 控制层
├── dao
│   └── entity          # 数据库实体 (DO)
├── dto                 # 请求/响应 DTO
├── remote              # 远程调用
├── service             # 业务层
└── toolkit             # 工具类
```

## 编码规范

### 实体类（DO）

- 注解：`@Data`、`@TableName("表名")`
- 主键：`@TableId(type = IdType.AUTO)`
- **每个字段必须添加 Javadoc 注释**，注释内容取自 SQL 的 `COMMENT`
- 字段类型：时间用 `Date`，删除标识用 `Integer`
- 自动填充字段必须加 `@TableField(fill = FieldFill.INSERT)` 或 `@TableField(fill = FieldFill.INSERT_UPDATE)`，否则
  `MetaObjectHandler` 不生效

```java

@Data
@TableName("t_user")
public class UserDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
```

### 统一响应

所有 Controller 返回值使用 `Result<T>`，通过 `Results` 工厂方法构造：

```java
// 成功（无数据）
return Results.success();

// 成功（带数据）
return Results.

success(userVO);

// 失败
return Results.

failure(abstractException);
```

### 异常体系

抛出异常时根据来源选择对应类型：

| 类型                 | 场景                  |
|--------------------|---------------------|
| `ClientException`  | 用户端错误（参数校验、用户名已存在等） |
| `ServiceException` | 服务端内部错误             |
| `RemoteException`  | 调用第三方服务失败           |

```java
throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST_ERROR);
```

### 错误码

业务模块新增错误码时，创建独立枚举实现 `IErrorCode`，遵循前缀规范：

- `A` 开头：客户端错误
- `B` 开头：服务端错误
- `C` 开头：远程调用错误

```java
public enum UserErrorCodeEnum implements IErrorCode {
    USER_NAME_EXIST_ERROR("A000111", "用户名已存在");
    // ...
}
```

### 对象映射

禁止手写 getter/setter 赋值，使用 `BeanUtil`：

```java
// 单对象转换
UserDO userDO = BeanUtil.convert(reqDTO, UserDO.class);

// 更新时忽略空字段
BeanUtil.

convertIgnoreNull(updateReqDTO, existingDO);
```

### 敏感字段脱敏

VO 中手机号、身份证字段使用 `@JsonSerialize` 注解，序列化时自动打码：

```java

@JsonSerialize(using = PhoneDesensitizationSerializer.class)
private String phone;

@JsonSerialize(using = IdCardDesensitizationSerializer.class)
private String idCard;
```

### 布隆过滤器

`RBloomFilter` 注册为 Spring Bean，由 `ApplicationRunner` 负责初始化并加载存量数据：

```java
// config/RBloomFilterConfiguration.java
@Bean
public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient) {
    return redissonClient.getBloomFilter("userRegisterCachePenetrationBloomFilter");
}

// ApplicationRunner 里调用 tryInit，返回 true 表示 BF 是新建的，需要从 DB 加载存量数据
boolean isNew = bloomFilter.tryInit(100000000L, 0.001);
if(isNew){
        // 查库全量写入
        }
```

- `tryInit` 若 Redis 中已存在该 BF 则返回 `false`，数据不会被清空
- 注册成功后调用 `bloomFilter.add(username)` 同步写入
- BF 只能减少查库，不能保证唯一性，**数据库必须加唯一索引兜底**

### 注册安全

用户名唯一性校验需双重保障：

```java
// 1. BF 前置拦截（减少查库）
if(bloomFilter.contains(username)){
        throw new

ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
}
        // 2. 唯一索引兜底（防竞态条件）
        try{

save(userDO);
}catch(
DuplicateKeyException e){
        throw new

ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
}
```

密码存储前必须加密，使用 Hutool 的 `DigestUtil.md5Hex(password)`。

### Redis Key 命名规范

- 用 `:` 分隔命名空间层级
- 同一段内统一用 `-`，禁止混用 `_` 和 `-`
- 格式：`shan-link:{业务}:{资源}:`

```java
// 正确
"shan-link:lock:user-register:"
        "shan-link:login:"

        // 错误（混用 _ 和 -）
        "shan-link:lock_user-register:"
```

### ShardingSphere

使用 JDBC 模式，数据源配置在 `shardingsphere-config.yaml`，`application.yml` 只保留驱动声明：

```yaml
# application.yml
spring:
  datasource:
    driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver
    url: jdbc:shardingsphere:classpath:shardingsphere-config.yaml
```

调试时在 `shardingsphere-config.yaml` 开启 `props.sql-show: true` 查看路由后的实际 SQL。

### API 路径规范

```
/api/shan-link/{module}/v1/{resource}
```

示例：`/api/shan-link/admin/v1/user/{username}`

## Git 提交规范

使用 Conventional Commits，**描述和 body 用中文**：

```
feat(user): 新增用户注册接口

实现用户名唯一性校验与密码加密存储。
```

类型：`feat` / `fix` / `refactor` / `docs` / `build` / `chore`
