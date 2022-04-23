package cn.cruder.logutil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dousx
 * @date 2022-04-22 13:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AopLogger {
    /**
     * 描述
     *
     * @return 描述
     */
    String describe() default "";

}
