package io.renren.modules.job.task;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TestJob {

    private Logger logger = LoggerFactory.getLogger(getClass());

    //每天1点10分30秒触发任务
    @Scheduled(cron = "30 10 1 * * ?")
    public void sumTask(){
        //统计
        System.out.println("=====管家:定时统计BEGIN====");
        sendSumGetRequest();
        System.out.println("=====管家:定时统计END====");

    }


    //每月最后一天23点执行一次
    @Scheduled(cron = "0 0 23 L * ? ")
    public void clearTask(){
        //清理
        System.out.println("=====管家:定时清理BEGIN====");
        sendGetClear();
        System.out.println("=====管家:定时清理END====");
    }

    /**
     @Description: 每天执行一次统计管家
     @Author: ESION.CT
     @Date: 2023/5/31 9:29
     */
    public void sendSumGetRequest(){
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 设置 POST 请求的数据
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet("http://localhost:8080/admin/demo/dynamictowerstaitc/sum");
            // 发送 GET 请求
            response = httpClient.execute(httpGet);
            // 处理响应
            if (response.getStatusLine().getStatusCode() == 200) {
                //无返回值
                logger.info("======================数据统计完成============================");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        // 关闭响应和 HttpClient
        try {
            response.close();
            httpClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     @Description: 一月执行一次清理者
     @Author: ESION.CT
     */
    public void sendGetClear(){
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 设置 POST 请求的数据
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet("http://localhost:8080/admin/demo/dynamictoweralert/deleteAll");
            // 发送 GET 请求
            response = httpClient.execute(httpGet);
            // 处理响应
            if (response.getStatusLine().getStatusCode() == 200) {
                //无返回值
                logger.info("======================清理数据完成============================");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        // 关闭响应和 HttpClient
        try {
            response.close();
            httpClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
