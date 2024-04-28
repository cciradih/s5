package org.eu.cciradih.s5;

public record Configuration(Proxy proxyClient, Proxy proxyServer, Aes aes) {
    public record Proxy(String address, int port, String key) {
    }

    public record Aes(String key, String iv) {
    }
}
