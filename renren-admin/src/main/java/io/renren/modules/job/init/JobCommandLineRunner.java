/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.init;

import io.renren.modules.job.dao.ScheduleJobDao;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.task.XmlThread;
import io.renren.modules.job.utils.ScheduleUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化定时任务数据
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
@Slf4j
public class JobCommandLineRunner implements CommandLineRunner {
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ScheduleJobDao scheduleJobDao;

    @Value("${url.send930}")
    private String sendUrl930;
    @Value("${url.send924}")
    private String sendUrl924;
    @Value("${url.send917}")
    private String sendUrl917;

    @Override
    public void run(String... args) {
        List<ScheduleJobEntity> scheduleJobList = scheduleJobDao.selectList(null);
        for (ScheduleJobEntity scheduleJob : scheduleJobList) {
            CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getId());
            //如果不存在，则创建
            if (cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            } else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }


            List<String> list = new ArrayList<>();

            list.add(sendUrl930);
            list.add(sendUrl924);
            list.add(sendUrl917);

//            list.add("127.0.0.1");

            Thread thread = null;
            for (int i = 0; i < list.size(); i++) {
                thread = new XmlThread(list.get(i));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }
                System.out.println("第"+i+"个线程名称为：" + Thread.currentThread() + "开始执行...");
                thread.start();
            }

//            XmlThread xmlThread930 = new XmlThread(sendUrl930);
//            XmlThread xmlThread924 = new XmlThread(sendUrl924);
//            XmlThread xmlThread3917 = new XmlThread(sendUrl917);

//            XmlThread xmlThread = new XmlThread("127.0.0.1");
//            XmlThread xmlThread2 = new XmlThread("127.0.0.1");
//            XmlThread xmlThread3 = new XmlThread("127.0.0.1");
//            xmlThread924.run();
//            System.out.println("=======================924线程采集==================");
//            xmlThread3917.run();
//            System.out.println("=======================917线程采集==================");
//            xmlThread930.run();
//            System.out.println("=======================930线程采集==================");


        }
    }
}