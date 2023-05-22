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
public class XmlMonthThread implements Runnable{

    private Logger logger = LoggerFactory.getLogger(getClass());

   private String url;


    @SneakyThrows
    @Override
    public void run() {
        logger.info("{}{}",Thread.currentThread().getName(),this.url);
        sendGet();
    }


    public void sendGet(){
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

    public XmlMonthThread(String url) {
        this.url = url;
    }

    public XmlMonthThread() {
    }
}
