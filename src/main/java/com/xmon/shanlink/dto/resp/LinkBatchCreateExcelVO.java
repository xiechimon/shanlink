package com.xmon.shanlink.dto.resp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 批量创建短链接 Excel 导出实体
 */
@Data
public class LinkBatchCreateExcelVO {

    /**
     * 原始链接
     */
    @ExcelProperty("原始链接")
    private String originUrl;

    /**
     * 完整短链接
     */
    @ExcelProperty("短链接")
    private String fullShortUrl;

    /**
     * 短链接描述
     */
    @ExcelProperty("描述")
    private String describe;
}
