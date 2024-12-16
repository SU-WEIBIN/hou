package com.yupi.springbootinit.manager;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.dto.chart.ChartAddRequest;
import com.yupi.springbootinit.model.vo.BIRespones;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class xinhuoAPi {

    private static String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";


    public static String xinhuo(String content)  {
        BIRespones biRespones = new BIRespones();
        // 组成需求向ai提问   那么data数据应该预先处理好json的换行符号
//        String content = "目标:"+chartAddRequest.getGoal() + ";类型:"+chartAddRequest.getChartType() + "\\r数据:\\r" + chartAddRequest.getChartData();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer GqpHLhKLHgAoPYuZXvoI:SlMUmRJCfcZAcXguLLcP");
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
//        System.out.println(content);
        String json = " {\n" +
                "        \"model\": \"generalv3\",\n" +
                "        \"messages\": [\n" +
                "           \n" +
                "            {\n" +
                "                \"role\": \"system\",\n" +
                "                \"content\": \"根据输入的csv格式的数据和分析目标,输出分析结论,以及根据输入的图表类型,生成eCharts图表,使用option的代码(注意严格遵守json格式);只输出我指定的内容,不要输出任何注释语句,不要输出多余的内容;我的输入格式:目标,类型,csv数据;你的输出格式:分析结论,option代码;\"\n" +
                "            },\n" +
                "             {\n" +
                "                \"role\": \"user\",\n" +
                "                \"content\": \"" + content + "\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }";
//        System.out.println(json);
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        String content1 = null;
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

//            System.out.println("----------------------------------------------------------");
//            System.out.println(response.getStatusLine());
//            System.out.println(EntityUtils.toString(response.getEntity().getContent()));
            String read = IoUtil.read(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JSONObject jsonObject = JSONUtil.parseObj(read);
            // 获取 choices 数组

            JSONArray choices = jsonObject.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                // 获取第一个 choice 对象
                JSONObject firstChoice = choices.getJSONObject(0);
                // 获取 message 对象
                JSONObject message = firstChoice.getJSONObject("message");
                // 获取 content 字段
                content1 = message.getStr("content");
                // 输出 content 内容
//                System.out.println(content1);

//                // 分割出结论和代码
//                String[] split = content1.split("option代码：\n```javascript");
//                if(split.length<2) return ResultUtils.error("输出错误");
//                split[1] = split[1].substring(0,split[1].length()-3);
//                biRespones.setGenResult(split[0]);
//                biRespones.setGenChart(split[1]);

//                System.out.println(split[1]);


            } else {
                System.out.println("choices 数组为空或不存在");
            }

            // 获取响应实体并转换为字符串

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content1;
    }

//    public static void main(String[] args) throws UnsupportedEncodingException {
//        xinhuo("目标:分析工资趋势;类型:折线图;\\r数据:\\r月份,工资\\r1,100\\r2,200\\r3,300\\r");
//    }
}
