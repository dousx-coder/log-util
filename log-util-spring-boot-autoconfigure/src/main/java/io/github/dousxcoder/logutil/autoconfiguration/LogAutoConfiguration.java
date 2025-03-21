package io.github.dousxcoder.logutil.autoconfiguration;

import io.github.dousxcoder.logutil.annotation.EnableAopLog;
import io.github.dousxcoder.logutil.aop.AopLoggerAspect;
import io.github.dousxcoder.logutil.hand.TraceIdInterceptor;
import io.github.dousxcoder.logutil.service.LogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * EnableConfigurationProperties可以使LogProperties注入spring容器
 *
 * @author dousx
 * @date 2022-04-22 13:43
 */
@Configuration
public class LogAutoConfiguration {

    /**
     * 手动注入的原因是让Bean的注入归{@link EnableAopLog}控制
     */
    @Bean(value = LogService.NAME)
    public LogService logService() {
        return new LogService();
    }

    @Order(-10)
    @Bean(value = AopLoggerAspect.NAME)
    @ConditionalOnClass(LogService.class)
    public AopLoggerAspect aopLoggerAspect(LogService logService) {
        return new AopLoggerAspect(logService);
    }


    @Bean(value = TraceIdInterceptor.NAME)
    public TraceIdInterceptor traceIdInterceptor() {
        return new TraceIdInterceptor();
    }

}
