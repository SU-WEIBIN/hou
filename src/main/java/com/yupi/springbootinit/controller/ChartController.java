package com.yupi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.bizmq.BIMessageProducer;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.config.ThreadPoolExecuterConfig;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;

import com.yupi.springbootinit.model.vo.BIRespones;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.yupi.springbootinit.utils.SqlUtils;
import com.yupi.springbootinit.manager.xinhuoAPi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.yupi.springbootinit.utils.ExcelUtils.ExcelToCsv;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private ThreadPoolExecutor poolExecutor;

    @Resource
    private BIMessageProducer biMessageProducer;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());

        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion



    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }
    /**
     * 智能分析
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BIRespones> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws UnsupportedEncodingException {
        // 获取前端输入
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 用户
        User loginUser = userService.getLoginUser(request);
        //校验 空值
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() < 100,ErrorCode.PARAMS_ERROR,"目标不能为空");
        // 校验文件
        final long ONE_MB = 1024*1024L;
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffixList = Arrays.asList("csv","xlsx","xls");
        ThrowUtils.throwIf(!vaildFileSuffixList.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式错误");
        // 处理要交给ai的数据
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析目标:").append(goal).append("\\r");
        userInput.append("类型:").append(chartType).append("\\r");
        String csvData = ExcelToCsv(multipartFile);// 转csv格式 可以压缩文件大小(省token)
        userInput.append("数据:\\r").append(csvData).append("\\r");

        String AiRespones = xinhuoAPi.xinhuo(userInput.toString());

        // 分割出结论和代码
        String[] split = AiRespones.split("option代码：\n");
        if(split.length<2) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai生产错误");
        BIRespones biRespones = new BIRespones();
        // todo ai没调试好,每次返回的结果格式不相同  trim去除多余的空格, 我感觉上echart的属性就那么多,不如封装一下,再使用正则表达式获取字段
        // 使用正则匹配第一个{ 到最后一个} 之间的内容
        final String regex = "\\{([\\s\\S]*)\\}\n";
        String eChartCode = "{\n"+ReUtil.extractMulti(regex, split[1], "$1")+"\n}";
//        eChartCode = eChartCode.replaceAll("\\s+",""); // 应该去除多余的空白,但不是这个变量
//        String[] split1 = split[1].split("option =");
        biRespones.setGenResult(split[0]);
        biRespones.setGenChart(eChartCode);

        Chart chart = new Chart();

        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(eChartCode);
        chart.setGenResult(split[0]);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart); // 这里保存数据库后会返回完整的对象???
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        biRespones.setChartId(chart.getId());


        return ResultUtils.success(biRespones);
    }
/**
     * 异步--智能分析
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BIRespones> genChartAsyncByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws UnsupportedEncodingException {
        // 获取前端输入
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 用户
        User loginUser = userService.getLoginUser(request);
        //校验 空值
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() < 100,ErrorCode.PARAMS_ERROR,"目标不能为空");
        // 校验文件
        final long ONE_MB = 1024*1024L;
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffixList = Arrays.asList("csv","xlsx","xls");
        ThrowUtils.throwIf(!vaildFileSuffixList.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式错误");
        String csvData = ExcelToCsv(multipartFile);// 转csv格式 可以压缩文件大小(省token)
        //todo  这里应该多加一个限流

        // 数据没有问题先提交到数据库,再进行分析
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart); // 这里保存数据库后会返回完整的对象???yes
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表数据保存失败");

        CompletableFuture.runAsync(()->{
//           更新图表状态位running
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if(!b){
                handleChartUpdateError(chart.getId(), "更新图表状态->running失败");
                return;
            }
            // 拼接要交给ai的数据
            StringBuilder userInput = new StringBuilder();
            userInput.append("分析目标:").append(goal).append("\\r");
            userInput.append("类型:").append(chartType).append("\\r");
            userInput.append("数据:\\r").append(csvData).append("\\r");


            String AiRespones = xinhuoAPi.xinhuo(userInput.toString());

            // 分割出结论和代码
            String[] split = AiRespones.split("option代码：\n");

            if(split.length<2){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai生产错误");
                handleChartUpdateError(chart.getId(), "ai生产错误");
                return;
            }


            // todo ai没调试好,每次返回的结果格式不相同  trim去除多余的空格, 我感觉上echart的属性就那么多,不如封装一下,再使用正则表达式获取字段
            // 使用正则匹配第一个{ 到最后一个} 之间的内容
            final String regex = "\\{([\\s\\S]*)\\}\n";
            String eChartCode = "{\n"+ReUtil.extractMulti(regex, split[1], "$1")+"\n}";

//            biRespones.setGenResult(split[0]);
//            biRespones.setGenChart(eChartCode);

            Chart updateChartResult = new Chart();

            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(eChartCode);
            updateChartResult.setGenResult(split[0]);
            updateChartResult.setStatus("succeed");
            boolean updateResult = chartService.updateById(chart); // 这里保存数据库后会返回完整的对象???(
            if(!updateResult) {
//            ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }

        },poolExecutor);//指定线程池


        BIRespones biRespones = new BIRespones();
        biRespones.setChartId(chart.getId());

        return ResultUtils.success(biRespones);
    }
/**
     * 异步--消息队列--智能分析
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @PostMapping("/gen/async1")
    public BaseResponse<BIRespones> genChartMqByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws UnsupportedEncodingException {
        // 获取前端输入
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 用户
        User loginUser = userService.getLoginUser(request);
        //校验 空值
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() < 100,ErrorCode.PARAMS_ERROR,"目标不能为空");
        // 校验文件
        final long ONE_MB = 1024*1024L;
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffixList = Arrays.asList("csv","xlsx","xls");
        ThrowUtils.throwIf(!vaildFileSuffixList.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式错误");
        String csvData = ExcelToCsv(multipartFile);// 转csv格式 可以压缩文件大小(省token)
        //todo  这里应该多加一个限流

        // 数据没有问题先提交到数据库,再进行分析
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart); // 这里保存数据库后会返回完整的对象???yes
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表数据保存失败");

        biMessageProducer.sendMessage(String.valueOf(chart.getId()));
        BIRespones biRespones = new BIRespones();
        biRespones.setChartId(chart.getId());

        return ResultUtils.success(biRespones);
    }



    private void handleChartUpdateError(long charId,String execMessage){
        Chart chart = new Chart();
        chart.setId(charId);
        chart.setStatus("failed");
        chart.setExecMessage(execMessage);
        boolean b = chartService.updateById(chart);
        if(!b){
            log.error("更新图表失败信息失败--id:"+charId+"message:"+execMessage);
        }
    }


    /**
     * 获取查询包装类
     *
     * @param
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        String chartData = chartQueryRequest.getChartData();
        String chartType = chartQueryRequest.getChartType();

        long userId = chartQueryRequest.getUserId();
        long current = chartQueryRequest.getCurrent();
        long pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(id != null && id>0,"id",id);
//        queryWrapper.eq(StringUtils.isNotBlank(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}
