package io.renren.modules.job.task;

import com.alibaba.fastjson.JSONObject;
import io.renren.common.utils.XmlToMap;
import io.renren.common.utils.XmltoJsonUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 @Description: 采集数据
 @Author: ESION.CT
 @Date: 2023/5/22 9:37
 */
@Component
public class XmlThread extends Thread{

    private Logger logger = LoggerFactory.getLogger(getClass());

   private String url;


    @SneakyThrows
    @Override
    public void run() {
        logger.info("{}{}",Thread.currentThread().getName(),this.url);
        Socket socket = new Socket(url, 30001);
        //得到一个输出流，用于向服务器发送数据
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = null;
        try {
            while (true) {
                long startTime = System.currentTimeMillis();
                //刷新缓冲
                outputStream.flush();
                //得到一个输入流，用于接收服务器响应的数据
                inputStream = socket.getInputStream();

                byte[] bytes = new byte[1]; // 一次读取一个byte
                String info = "";
                String s = "";

                while (true) {
                    if (inputStream.available() > 0) {
                        inputStream.read(bytes);
                        String hexStr = XmlToMap.ByteArrayToHexStr(bytes);
                        s = s + hexStr;
                        if (System.currentTimeMillis() - startTime > 2*1000) {
                            //检测心跳
                            break;
                        }
                        //已经读完
                        if (inputStream.available() == 0) {
                            info = XmlToMap.HexStrToStr(s);

                            logger.debug("数据：{}", info);
                            String yanmd5 = info.substring(0, info.length() - 43);
                            JSONObject taAll = XmltoJsonUtil.xmlToJson(yanmd5);
                            if (taAll.size() < 1) {
                                break;
                            }
                            if(StringUtils.isNotBlank(yanmd5) && yanmd5.contains("TowerMonitor")){
                                logger.debug("开始发送XML解析数据....");
                                //秒
                                sendPostSecond(yanmd5);
                                //报警
                                sendPostAlert(yanmd5);
                                //日
                                sendDayPost(yanmd5);
                            }
                            //
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }finally {
            System.out.println(">>>线程"+this.getId()+"的连接释放\n");
            try{
                if(outputStream != null){
                    outputStream.close();
                }
                if(inputStream != null){
                    inputStream.close();
                }
                if(socket != null){
                    socket.close();
                }
            }catch (Exception e){

            }
        }
    }


    public void sendPostSecond(String json){
        //进行数据传输
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建 HttpPost 对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/demo/dynamictowersecond/collect");
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
//                System.out.println(result);
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
        // 关闭响应和 HttpClient
        try {
            System.out.println("正在释放秒级数据解析服务的资源...");
            response.close();
            httpClient.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public void sendDayPost(String json){
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
//                System.out.println(result);
            }
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
        // 关闭响应和 HttpClient
        try {
            System.out.println("正在释放天级数据解析服务的资源...");
            response.close();
            httpClient.close();
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
    }

    public void sendPostAlert(String json){
        //进行数据传输
        System.out.println("==================报警数据采集=====================");
        // 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建 HttpPost 对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/demo/dynamictoweralert/collect");
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
//                System.out.println(result);
            }
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
        // 关闭响应和 HttpClient
        try {
            System.out.println("正在释放报警数据解析服务的资源...");
            response.close();
            httpClient.close();
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
    }

    public XmlThread(String url) {
        this.url = url;
    }

    public XmlThread() {
    }
}
