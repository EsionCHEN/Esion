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
 @Description: 每个月底清理数据
 @Author: ESION.CT
 @Date: 2023/5/22 9:43
 */
@Component("xmlMonthTask")
public class XmlMonthTask implements ITask{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void run(String params) throws IOException {
         logger.info("=======================每月底清理数据一次========================");
		XmlMonthThread xmlThread = new XmlMonthThread();
		xmlThread.run();
	}
}