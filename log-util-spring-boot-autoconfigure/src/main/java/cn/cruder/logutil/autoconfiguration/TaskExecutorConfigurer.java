package cn.cruder.logutil.autoconfiguration;


import org.slf4j.Logger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;


/**
 * @author dousx
 */
@EnableAsync
@Configuration
public class TaskExecutorConfigurer implements AsyncConfigurer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TaskExecutorConfigurer.class);

    public static final String LOG_POOL = "log-pool";


    /**
     * 日志异步保存输出线程池
     * <br/>
     * log打印专用线程池，拒绝策略:什么也不做，不抛异常；
     *
     * @return 返回线程池
     */
    @Bean(value = LOG_POOL)
    public TaskExecutor logExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(600);
        threadPoolTaskExecutor.setKeepAliveSeconds(600);
        threadPoolTaskExecutor.setThreadNamePrefix(LOG_POOL + "-");
        threadPoolTaskExecutor.setRejectedExecutionHandler((r, poolExecutor) -> {
            // log打印专用线程池，拒绝策略:什么也不做，不抛异常；
            //Thread thread = new Thread();
            //thread.start();
        });
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
        return threadPoolTaskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error(String.format("异步异常:%s\t %s\t ", throwable.getMessage(), method.getName()), throwable);
        };
    }
}
