package io.github.dousxcoder.logutil.annotation;


import io.github.dousxcoder.logutil.autoconfiguration.TaskExecutorConfigurer;
import io.github.dousxcoder.logutil.autoconfiguration.LogAutoConfiguration;
import io.github.dousxcoder.logutil.autoconfiguration.TraceIdInterceptorConfig;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 使用了@Import注解,取代从/META-INF/spring.factories加载配置
 *
 * @author dousx
 * @date 2022-06-05 14:17
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({LogAutoConfiguration.class, TaskExecutorConfigurer.class, TraceIdInterceptorConfig.class})
public @interface EnableAopLog {
}
