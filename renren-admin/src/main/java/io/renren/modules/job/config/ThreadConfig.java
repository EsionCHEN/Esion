package io.renren.modules.job.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

/**
 @Description: 线程池配置
 @Author: ESION.CT
 @Date: 2022/1/24 14:26
 */
@Configuration
@ComponentScan(value = "io.renren.modules.demo.service")
@EnableAsync
@Slf4j
public class ThreadConfig implements AsyncConfigurer {

    // ThredPoolTaskExcutor的处理流程
    // 当池子大小小于corePoolSize，就新建线程，并处理请求
    // 当池子大小等于corePoolSize，把请求放入workQueue中，池子里的空闲线程就去workQueue中取任务并处理
    // 当workQueue放不下任务时，就新建线程入池，并处理请求，如果池子大小撑到了maximumPoolSize，就用RejectedExecutionHandler来做拒绝处理
    // 当池子的线程数大于corePoolSize时，多余的线程会等待keepAliveTime长时间，如果无请求可处理就自行销毁
    /**
     @Description: 初始化线程池
     @Author: ESION.CT
     @Date: 2022/1/24 14:34
     */
    @Override
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //存活线程数
        executor.setCorePoolSize(3);
        //最大线程
        executor.setMaxPoolSize(15);
        //队列线程
        executor.setQueueCapacity(25);
        //线程名称前缀
        executor.setThreadNamePrefix("ES_ThreadPool_");
        //处理拒绝者 拒绝task的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //线程存活时间 60s
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        log.info("====线程池初始化完成====\r\n当前线程池存在线程：{}",executor.getCorePoolSize());
        return executor;
    }

    /**
     @Description: 异步方法错误拦截
     @Author: ESION.CT
     @Date: 2022/1/24 14:34
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) -> {
            String errorBuilder = "异步方法执行错误::" + method.toString() + " 参数为:"
                    + Arrays.toString(params);
            log.error("线程池异步执行结果反馈：{}",errorBuilder);
        };
    }
}