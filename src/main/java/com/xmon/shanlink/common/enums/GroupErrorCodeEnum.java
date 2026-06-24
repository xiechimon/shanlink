package com.xmon.shanlink.common.enums;

import com.xmon.shanlink.common.convention.errorcode.IErrorCode;

/**
 * 分组错误码枚举
 */
public enum GroupErrorCodeEnum implements IErrorCode {

    GROUP_NAME_EXIST("A000300", "分组名已存在"),

    GROUP_NOT_EXIST("A000301", "分组不存在"),

    GROUP_MAX_NUM("A000302", "分组数量已达上限"),

    GROUP_SAVE_ERROR("B000300", "分组新增失败");

    private final String code;

    private final String message;

    GroupErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
