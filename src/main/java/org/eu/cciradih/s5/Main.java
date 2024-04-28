package org.eu.cciradih.s5;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Socks5Verticle());
        Thread.currentThread().join();
    }
}
