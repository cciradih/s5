package org.eu.cciradih.s5;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.util.Arrays;

public class ProxyClientVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyClientVerticle.class);
    private static final ProtocolUtil PROTOCOL_UTIL = ProtocolUtil.getInstance();
    private final Configuration configuration;

    private static final byte[] NO_AUTHENTICATION_REQUIRED = new byte[]{5, 0};
    private static final byte[] REPLIES = new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0};

    public ProxyClientVerticle(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() {
        NetServerOptions netServerOptions = new NetServerOptions();
        netServerOptions.setHost(this.configuration.proxyClient().address())
                .setPort(this.configuration.proxyClient().port());
        NetServer proxyClientServer = this.vertx.createNetServer(netServerOptions);

        proxyClientServer.connectHandler(localClientSocket -> localClientSocket.handler(localClientBuffer1 -> {
            LOGGER.debug("0x01 - proxy client read from local client: " + Arrays.toString(localClientBuffer1.getBytes()));
            localClientSocket.handler(localClientBuffer2 -> {
                byte[] bytes = localClientBuffer2.getBytes();
                LOGGER.debug("0x03 - proxy client read from local client: " + Arrays.toString(bytes));
                NetClient proxyServerClient = this.vertx.createNetClient();
                proxyServerClient.connect(this.configuration.proxyServer().port(), this.configuration.proxyServer().address())
                        .onComplete(asyncResult -> {
                            if (asyncResult.succeeded()) {
                                NetSocket proxyServerSocket = asyncResult.result();

                                localClientSocket.closeHandler(handler -> proxyServerSocket.close());
                                proxyServerSocket.closeHandler(handler -> localClientSocket.close());

                                localClientSocket.handler(buffer -> {
                                    buffer = PROTOCOL_UTIL.generate(buffer);
                                    proxyServerSocket.write(buffer);
                                });

                                proxyServerSocket.handler(PROTOCOL_UTIL.getRecordParser(localClientSocket));

                                localClientSocket.write(Buffer.buffer(REPLIES));
                                LOGGER.debug("0x04 - proxy client write to local client: " + Arrays.toString(REPLIES));

                                Buffer buffer = PROTOCOL_UTIL.generate(localClientBuffer2);
                                proxyServerSocket.write(buffer);
                                LOGGER.debug("0x05 - proxy client write to proxy server: " + Arrays.toString(localClientBuffer2.getBytes()));
                            }
                        });
            });
            localClientBuffer1 = Buffer.buffer(NO_AUTHENTICATION_REQUIRED);
            localClientSocket.write(localClientBuffer1);
            LOGGER.debug("0x02 - proxy client write to local client: " + Arrays.toString(localClientBuffer1.getBytes()));
        }));

        proxyClientServer.listen().onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                LOGGER.info("proxy client listening on " + this.configuration.proxyClient().address() + ":" + this.configuration.proxyClient().port());
            }
        });
    }
}
