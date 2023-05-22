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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 @Description: 每天晚上12点执行
 @Author: ESION.CT
 @Date: 2023/5/22 9:43
 */
@Component("xmlDayTask")
public class XmlDayTask implements ITask{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${url.send}")
	private String sendUrl;

	@Override
	public void run(String params) throws IOException {
		logger.info("当前ip{}\n\r=======================每日采集一次========================",sendUrl);
		XmlDayThread xmlThread = new XmlDayThread(sendUrl);
//		XmlDayThread xmlThread2 = new XmlDayThread("http://192.168.14.92:30002");
//		XmlDayThread xmlThread3 = new XmlDayThread("http://192.168.17.92:30002");
		xmlThread.run();
//		xmlThread2.run();
//		xmlThread3.run();
	}
}