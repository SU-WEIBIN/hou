package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
* @author 70928
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-10-08 21:02:45
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * 每一个原始数据创建一个表,chart_id
     *
     */

    /**
     * 调用这些表
     * TODO 使用string.format拼接sql语句,然后传进来就好,注意这种方式存在sql注入问题
     */
    List<Map<String,Object>> queryChartData(String querySql);
}




