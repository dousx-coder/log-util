package cn.cruder.logutil.annotation;


import cn.cruder.logutil.autoconfiguration.TaskExecutorConfigurer;
import cn.cruder.logutil.autoconfiguration.TraceIdInterceptorConfig;
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
@Import({cn.cruder.logutil.autoconfiguration.LogAutoConfiguration.class, TaskExecutorConfigurer.class, TraceIdInterceptorConfig.class})
public @interface EnableAopLog {
}
