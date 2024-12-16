package com.yupi.springbootinit.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.yupi.springbootinit.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {


    public static String ExcelToCsv(MultipartFile multipartFile ){

//        // 测试  先获取一个excel文件来测试
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:test2.xlsx");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        // 读取excel
        List<Map<Integer,String>> list = null;
        //multipartFile.getInputStream()
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 转换为csv
        StringBuilder resultCSV = new StringBuilder();
        // 获取表头
        LinkedHashMap<Integer, String> headMap = (LinkedHashMap)list.get(0);
        //过滤表头
        List<String> headList = headMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        resultCSV.append(String.join(",",headList)).append("\\r");
//        System.out.println(StringUtils.join(headList,","));
        // 同理获取并处理每一行数据 并过滤
        for (int i=  1;i < list.size();i ++){
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(data -> StringUtils.isNotBlank(data)).collect(Collectors.toList());
            //
            resultCSV.append(String.join(",",dataList)).append("\\r");
        }

        return resultCSV.toString();


    }

//    public static void main(String[] args) throws FileNotFoundException {
//
//        ExcelToCsv();
//    }
}
