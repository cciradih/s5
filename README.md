# S5（Socks 5）

Socks 5 代理。

## 功能

- [x] Socks 5 协议。
- [x] 64-AES-CTR 加密协议。

## 效果

![](https://github.com/cciradih/s5/blob/master/example.png)

## 网络模型

![](https://github.com/cciradih/s5/blob/master/model.jpg)

## 64-AES-CTR 加密协议

```
+-------------+--------+----------------+
| MAGIC ASCII | LENGTH | DATA (AES-CTR) |
+-------------+--------+----------------+
| 6           | 4      | variable       |
+-------------+--------+----------------+
```

* MAGIC ASCII: 6 random ascii between 0x20 and 0x7E
* LENGTH: data length
* DATA: data to be transferred

## 使用

```shell
# jar
java -jar s5-1.2.0.jar
# 可执行文件
./s5
```

### Proxy 配置

> [!NOTE]
> AES 密钥和偏移量生成可以参考 `src/test/java/org/eu/cciradih/socks5/MainTests.java`。

文件位置 `src/main/resources/configuration.json`

```json
{
  "proxyClient": {        //  代理客户端
    "address": "0.0.0.0", //  监听地址
    "port": 25700         //  监听端口
  },
  "proxyServer": {        //  代理服务器
    "address": "0.0.0.0", //  监听地址
    "port": 25701         //  监听端口
  },
  "aes": {
    "key": "...",         //  密钥
    "iv": "..."           //  偏移量
  }
}
```

### 日志配置

文件位置 `src/main/resources/vertx-default-jul-logging.properties`

```properties
handlers=java.util.logging.ConsoleHandler
#   全局日志级别
java.util.logging.ConsoleHandler.level=INFO
#   包日志级别
org.eu.cciradih.s5.level=FINEST
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tc %4$s --- %2$s: %5$s%6$s%n
```

### 构建

```shell
# 使用 shade 构建 jar
mvn clean package -P shade -DskipTests
# 使用 GraalVN 构建可执行文件

# 运行时做类似于覆盖率测试的行为以便 agent 能够找到所有反射类。
# 然后停止运行后将生成的 JSON 文件覆盖 src/main/resources/META-INF/native-image/org.eu.cciradih/s5 目录下的 JSON 文件。
mkdir META-INF
java -agentlib:native-image-agent=config-output-dir=./META-INF -jar ./target/s5-1.2.0.jar
mvn clean package -P graalvm -DskipTests
```

### 运行

```shell
# jar
java -jar ./target/s5-1.2.0.jar
# 可执行文件
./target/s5
```
