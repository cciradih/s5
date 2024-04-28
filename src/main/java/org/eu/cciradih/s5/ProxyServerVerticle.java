package org.eu.cciradih.s5;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ProxyServerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServerVerticle.class);
    private static final ProtocolUtil PROTOCOL_UTIL = ProtocolUtil.getInstance();
    private final Configuration configuration;

    public ProxyServerVerticle(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() {
        NetServerOptions netServerOptions = new NetServerOptions();
        netServerOptions.setHost(this.configuration.proxyServer().address())
                .setPort(this.configuration.proxyServer().port());
        NetServer proxyServerServer = this.vertx.createNetServer(netServerOptions);

        proxyServerServer.connectHandler(proxyClientSocket -> proxyClientSocket.handler(proxyClientBuffer -> {
            byte[] dataBytes = Arrays.copyOfRange(proxyClientBuffer.getBytes(), 10, proxyClientBuffer.length());
            byte[] bytes = PROTOCOL_UTIL.getData(Buffer.buffer(dataBytes)).getBytes();
            LOGGER.debug("0x11 - proxy server read from proxy client: " + Arrays.toString(bytes));
            proxyClientSocket.pause();
            String remoteServerAddress = this.getRemoteServerAddress(bytes);
            int remoteServerPort = this.getRemoteServerPort(bytes);

            NetClient remoteServerClient = this.vertx.createNetClient();
            remoteServerClient.connect(remoteServerPort, remoteServerAddress)
                    .onComplete(asyncResult -> {
                        if (asyncResult.succeeded()) {
                            NetSocket remoteServerSocket = asyncResult.result();
                            proxyClientSocket.resume();

                            proxyClientSocket.closeHandler(handler -> remoteServerSocket.close());
                            remoteServerSocket.closeHandler(handler -> proxyClientSocket.close());

                            proxyClientSocket.handler(PROTOCOL_UTIL.getRecordParser(remoteServerSocket));

                            remoteServerSocket.handler(buffer -> {
                                buffer = PROTOCOL_UTIL.generate(buffer);
                                proxyClientSocket.write(buffer);
                            });
                        }
                    });
        }));

        proxyServerServer.listen().onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                LOGGER.info("proxy server listening on " + this.configuration.proxyServer().address() + ":" + this.configuration.proxyServer().port());
            }
        });
    }


    private String getRemoteServerAddress(byte[] bytes) {
        byte addressType = bytes[3];
        return switch (addressType) {
            case 1, 4 -> {
                byte[] ip = Arrays.copyOfRange(bytes, 4, bytes.length - 2);
                try {
                    yield InetAddress.getByAddress(ip).getHostAddress();
                } catch (UnknownHostException ignored) {
                    yield "";
                }
            }
            case 3 -> {
                byte[] domain = Arrays.copyOfRange(bytes, 5, bytes.length - 2);
                yield new String(domain);
            }
            default -> "";
        };
    }

    private int getRemoteServerPort(byte[] bytes) {
        bytes = new byte[]{0, 0, bytes[bytes.length - 2], bytes[bytes.length - 1]};
        return Buffer.buffer(bytes).getInt(0);
    }
}
