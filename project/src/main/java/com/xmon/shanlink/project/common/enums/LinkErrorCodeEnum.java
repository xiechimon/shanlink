package com.xmon.shanlink.project.common.enums;

import com.xmon.shanlink.project.common.convention.errorcode.IErrorCode;

public enum LinkErrorCodeEnum implements IErrorCode {

    LINK_NOT_EXIST("A000400", "短链接记录不存在"),

    LINK_EXPIRED("A000401", "短链接已过期"),

    LINK_SAVE_ERROR("B000400", "短链接新增失败");

    private final String code;

    private final String message;

    LinkErrorCodeEnum(String code, String message) {
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
