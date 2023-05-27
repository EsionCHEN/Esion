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
import io.renren.modules.job.task.XmlDayThread;
import io.renren.modules.job.task.XmlThread;
import io.renren.modules.job.utils.ScheduleUtils;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 初始化定时任务数据
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
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
            XmlThread xmlThread930 = new XmlThread(sendUrl930);
            XmlThread xmlThread924 = new XmlThread(sendUrl924);
            XmlThread xmlThread3917 = new XmlThread(sendUrl917);

//            XmlThread xmlThread = new XmlThread("127.0.0.1");
//            XmlThread xmlThread2 = new XmlThread("127.0.0.1");
//            XmlThread xmlThread3 = new XmlThread("127.0.0.1");
            xmlThread930.run();
            xmlThread924.run();
            xmlThread3917.run();
        }
    }
}