/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.init;

import io.renren.modules.job.config.ThreadConfig;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
        task();
        List<ScheduleJobEntity> scheduleJobList = scheduleJobDao.selectList(null);
        for (ScheduleJobEntity scheduleJob : scheduleJobList) {
            CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getId());
            //如果不存在，则创建
            if (cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            } else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }
        }
        task();
    }


    public void task() {

        List<String> list = new ArrayList<>();
        //线上配置
//        list.add(sendUrl917);
//        list.add(sendUrl924);
//        list.add(sendUrl930);


        //本地调试
//        list.add("127.0.0.1");

        Thread thread = null;

        for (int i = 0; i < list.size(); i++) {
            try {
                thread = new XmlThread(list.get(i));
                Thread.sleep(2000);
                System.out.println("第" + i + "个线程名称为：" + Thread.currentThread().getName() + "开始执行...");
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }
    }
}