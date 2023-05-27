/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 @Description: 每分钟执行
 @Author: ESION.CT
 @Date: 2023/5/22 9:43
 */
@Component("xmlTask")
public class XmlTask implements ITask{
	private Logger logger = LoggerFactory.getLogger(getClass());
//
//	@Value("${url.send}")
//	private String sendUrl;

	@Override
	public void run(String params) throws IOException {
		logger.info("当前ip{}\n\r=======================每分钟采集一次========================");
		XmlThread xmlThread = new XmlThread("http://192.168.33.92");
		XmlThread xmlThread2 = new XmlThread("http://192.168.14.92");
		XmlThread xmlThread3 = new XmlThread("http://192.168.17.92");
		xmlThread.run();
		xmlThread2.run();
		xmlThread3.run();
	}

	public static void main(String[] args) {
		XmlThread xmlThread = new XmlThread("127.0.0.1");
//		XmlThread xmlThread2 = new XmlThread("http://localhost");
//		XmlThread xmlThread3 = new XmlThread("http://localhost");
		xmlThread.run();
//		xmlThread2.run();
//		xmlThread3.run();
	}
}