package org.eu.cciradih.socks5;

public record Configuration(Proxy proxyClient, Proxy proxyServer, Aes aes) {
    public record Proxy(String address, int port, int timeout, String key) {
    }

    public record Aes(String key, String iv) {
    }
}
