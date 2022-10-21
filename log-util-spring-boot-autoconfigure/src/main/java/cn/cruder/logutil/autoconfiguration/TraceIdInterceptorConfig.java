package cn.cruder.logutil.autoconfiguration;

import cn.cruder.logutil.hand.TraceIdInterceptor;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 日志需要配置
 * <br/>
 * %X{TRACE_ID}  其中 TRACE_ID 为自定义参数名称{@link MDC#put(String, String)}的key
 * <br/>
 * 例如:  %d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{20}.%M [%line] - %X{TRACE_ID} %msg%n
 *
 * @author dousx
 */
@Configuration
@ConditionalOnClass({TraceIdInterceptor.class})
public class TraceIdInterceptorConfig implements WebMvcConfigurer {
    @Resource(name = TraceIdInterceptor.NAME)
    private TraceIdInterceptor traceIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**");
    }
}
