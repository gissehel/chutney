package com.chutneytesting.task.ssh;


import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import com.chutneytesting.task.spi.injectable.Target;

public class Connection {

    private static final String EMPTY = "";

    public final String serverHost;
    public final int serverPort;
    public final String username;
    public final String password;
    public final String privateKey;
    public final String passphrase;

    private Connection(String serverHost, int serverPort, String username, String password, String privateKey, String passphrase) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
    }

    public static Connection from(Target target) {
        guardClause(target);

        final String host = target.host();
        final int port = extractPort(target);
        final String username = target.user().orElse(EMPTY);
        final String password = target.userPassword().orElse(EMPTY);
        final String privateKey = target.privateKey().orElse(EMPTY);
        final String passphrase = target.privateKeyPassword().orElse(EMPTY);

        return new Connection(host, port, username, password, privateKey, passphrase);
    }

    public boolean usePrivateKey() {
        return isNotBlank(privateKey);
    }

    private static void guardClause(Target target) {
        if (target.uri() == null) {
            throw new IllegalArgumentException("Target URL is undefined");
        }
        if (target.host() == null || target.host().isEmpty()) {
            throw new IllegalArgumentException("Target is badly defined");
        }
    }

    private static int extractPort(Target target) {
        int serverPort = target.port();
        return serverPort == -1 ? 22 : serverPort;
    }
}
