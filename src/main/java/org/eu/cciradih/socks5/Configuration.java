package org.eu.cciradih.socks5;

public record Configuration(Proxy proxyClient, Proxy proxyServer) {
    public record Proxy(String address, int port, int timeout) {
    }
}
