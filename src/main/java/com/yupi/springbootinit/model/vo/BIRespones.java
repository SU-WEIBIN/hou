package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * ai返回值
 */
@Data
public class BIRespones {

    /**
     * 生成的图表代码
     */
    private String genChart;
    /**
     * 生成的分析结果
     */
    private String genResult;

    private Long chartId;
}
