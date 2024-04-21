# S5（Socks 5）

Socks 5 代理。

## 功能

- [x] Socks 5 协议。

## 网络模型

![](https://github.com/cciradih/s5/blob/master/model.jpg)

## 使用

JDK 21 环境开箱即用（Out of the box），代码很简单，没有过度封装。

```shell
java -jar socks5-1.1.0.jar
```

### Proxy 配置
```json
{
  "proxyClient": {        //  代理客户端
    "address": "0.0.0.0", //  监听地址
    "port": 25700,        //  监听端口
    "timeout": 5000       //  连接代理服务器的 soTimeout 和 connectTimeout 的超时时间（ms）
  },
  "proxyServer": {        //  代理服务器
    "address": "0.0.0.0", //  监听地址
    "port": 25701,        //  监听端口
    "timeout": 5000       //  连接远程服务器的 soTimeout 和 connectTimeout 的超时时间（ms）
  }
}
```

### 日志配置

文件位置 `src/main/resources/logback.xml`

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} -%kvp- %msg%n</pattern>
        </encoder>
    </appender>
    <root level="off">  // 日志级别默认 off 关闭，可选 trace、debug、info、warn、error、all、off。
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### 构建

```shell
mvn clean package -DskipTests
```

### 运行

```shell
java -jar target/socks5-1.1.0.jar
```
