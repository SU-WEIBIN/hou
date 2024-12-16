package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

/**
 * 文件上传请求
 */
@Data
public class GenChartByAiRequest {
    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String ChartType;

    private static final long serialVersionUID = 1L;
}
