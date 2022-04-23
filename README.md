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
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

配置:可自定义前后缀,以及日志输入级别

```yaml
cruder:
  log:
    prefix: '----'
    suffix: '||'
    level: 'info'
```