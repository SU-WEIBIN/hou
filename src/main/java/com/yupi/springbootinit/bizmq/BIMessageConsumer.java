package com.yupi.springbootinit.bizmq;

import cn.hutool.core.util.ReUtil;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.xinhuoAPi;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.vo.BIRespones;
import com.yupi.springbootinit.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class BIMessageConsumer {

    @Resource
    private ChartService chartService;

//    @SneakyThrows  //忽略异常
    @RabbitListener(queues = {BiMessageConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG)long deliveryTag) throws IOException {

        if(StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag,false,false);   //ack
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long id = Long.parseLong(message);

        Chart chart = chartService.getById(id);
        if(chart == null){
            channel.basicNack(deliveryTag,false,false);   //ack
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
        }
//        CompletableFuture.runAsync(()->{
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
        String s = buiderUserInput(chart);

        String AiRespones = xinhuoAPi.xinhuo(s);

            // 分割出结论和代码
            String[] split = AiRespones.split("option代码：\n");

            if(split.length<2){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai生产错误");
                handleChartUpdateError(chart.getId(), "ai生产错误");
                return;
            }


            //  ai没调试好,每次返回的结果格式不相同  trim去除多余的空格, 我感觉上echart的属性就那么多,不如封装一下,再使用正则表达式获取字段
            // 使用正则匹配第一个{ 到最后一个} 之间的内容
            final String regex = "\\{([\\s\\S]*)\\}\n";
            String eChartCode = "{\n"+ ReUtil.extractMulti(regex, split[1], "$1")+"\n}";

//            biRespones.setGenResult(split[0]);
//            biRespones.setGenChart(eChartCode);

            Chart updateChartResult = new Chart();

            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(eChartCode);
            updateChartResult.setGenResult(split[0]);
            updateChartResult.setStatus("succeed");
            boolean updateResult = chartService.updateById(updateChartResult); // 这里保存数据库后会返回完整的对象???(
            if(!updateResult) {
//            ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }

//        },poolExecutor);//指定线程池


//        BIRespones biRespones = new BIRespones();
//        biRespones.setChartId(chart.getId());

        log.info("receiveMessage message={}", message);
        channel.basicAck(deliveryTag,false);
    }

    //处理发给ai的数据
    private String buiderUserInput(Chart chart){

        // 拼接要交给ai的数据
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析目标:").append(chart.getGoal()).append("\\r");
        userInput.append("类型:").append(chart.getChartType()).append("\\r");
        userInput.append("数据:\\r").append(chart.getChartData()).append("\\r");
        return  userInput.toString();
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
}
