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
     * 删除标识 0：未删除 1：已删除
     */
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
