/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 测试定时任务(演示Demo，可删除)
 *
 * testTask为spring bean的名称
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component("testTask")
public class TestTask implements ITask{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${url.switch}")
	private Boolean urlFlag;

	@Override
	public void run(String params) throws IOException {
		logger.debug("===============定时任务开始执行===================");

		// 创建 HttpClient 对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		// 创建 HttpGet 对象
		HttpGet httpGet = new HttpGet();
		if (urlFlag) {
			//在线数据读取
			 httpGet = new HttpGet("192.168.33.92");
		}else{
			//文件读取
			 httpGet = new HttpGet("http://localhost:8080/admin/demo/dynamictowerstaitc/test");
		}
		// 发送 GET 请求
		CloseableHttpResponse response = httpClient.execute(httpGet);
		try {
			// 处理响应
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				logger.info(result);
				logger.info("定时任务正在执行，数据提取：{}", result);
				if(StringUtils.isNotBlank(result)){
					//实时发送JSON
					sendPost(result);
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
//				System.out.println(result);
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
}