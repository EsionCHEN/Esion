package io.renren.modules.job.task;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 @Description: 采集数据
 @Author: ESION.CT
 @Date: 2023/5/22 9:37
 */
@Component
public class XmlDayThread implements Runnable{

    private Logger logger = LoggerFactory.getLogger(getClass());

   private String url;


    @SneakyThrows
    @Override
    public void run() {
        logger.info("{}{}",Thread.currentThread().getName(),this.url);
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建 HttpGet 对象
        HttpGet httpGet = new HttpGet(url);
        // 发送 GET 请求
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            // 处理响应
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                logger.info(result);
                logger.debug("定时任务正在执行，数据提取：{}", result);
                if(StringUtils.isNotBlank(result)){
                    //实时发送JSON
                    sendPost(result);
                    sendGet();
                }
            }
            logger.debug("===============定时任务完成===================");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭响应和 HttpClient
            response.close();
            httpClient.close();
        }
    }


    public void sendPost(String json){
        //进行数据传输
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建 HttpPost 对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/demo/dynamictowerday/collect");
        // 设置 POST 请求的数据
        CloseableHttpResponse response = null;
        try {
            StringEntity stringEntity = new StringEntity(json,"utf-8");
            stringEntity.setContentType("text/plain");
            httpPost.setEntity(stringEntity);
            // 发送 POST 请求
            response = httpClient.execute(httpPost);
            // 处理响应
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                System.out.println(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 关闭响应和 HttpClient
        try {
            response.close();
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendGet(){
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

    public XmlDayThread(String url) {
        this.url = url;
    }

    public XmlDayThread() {
    }
}