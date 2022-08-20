# 自定义log-util-spring-boot-starter
打包
```shell
mvn clean package install -Dmaven.test.skip=true
```
引入依赖

```xml
<dependency>
    <groupId>cn.cruder</groupId>
    <artifactId>log-util-spring-boot-starter</artifactId>
    <version>1.1.2022082012</version>
</dependency>
```
启动类添加`@EnableAopLog`注解打开日志功能
