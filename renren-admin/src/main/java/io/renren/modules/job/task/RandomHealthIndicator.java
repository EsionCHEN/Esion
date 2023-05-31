package io.renren.modules.job.task;

import io.renren.common.utils.DateUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 @Description: 五分钟健康检查
 @Author: ESION.CT
 @Date: 2023/5/31 9:42
 */
@Component
public class RandomHealthIndicator implements HealthIndicator {
    @Override
    public Health getHealth(boolean includeDetails) {
        return HealthIndicator.super.getHealth(includeDetails);
    }


    @Scheduled(cron = "0 0/5 * * * ? ")
    @Override
    public Health health() {
        //线程波比
        double chance = ThreadLocalRandom.current().nextDouble();
        //剩余内存
        long maxMemory = Runtime.getRuntime().maxMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long useMemory = maxMemory - freeMemory;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateUtils.DATE_TIME_PATTERN);
        System.out.println("=================="+ dateTimeFormatter.format(LocalDateTime.now())+"健康检查 BEGIN====================");
        System.out.println("系统: 最大可用内存（"+maxMemory+"bit)");
        System.out.println("JVM: 已占JVM内存（"+totalMemory+"bit), 剩余JVM内存("+freeMemory+"bit)");
        boolean invalid = Runtime.getRuntime().freeMemory() < (100*100*1024);
        Health.Builder status = Health.up();
        if (invalid) {
            status = Health.down();
            System.out.println("JVM 内存运行状态"+status.build().getStatus()+",正在释放....");
            System.gc();
        }
        System.out.println(Thread.currentThread().getName()+"健康状态："+status.build().getStatus());
        try {
            if(Health.down().build().getStatus().getCode().equals(status.build().getStatus().getCode())){
                System.out.println("系统内存处于亚健康状态,正在修复...");
            }
        } catch (Exception e) {
        }
        System.out.println("=================="+ dateTimeFormatter.format(LocalDateTime.now())+"健康检查 E N D====================");
        return status.build();
    }
}
