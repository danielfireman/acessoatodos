package com.acessoatodos;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Exposes an available socket port as a test resource.
 */
public final class AvailablePort  {
    private ServerSocket socket = null;

    protected synchronized void acquire() throws Throwable {
        this.getPort();
    }

    public String getPort() {
        if (null == this.socket) {
            try (final ServerSocket serverSocket = new ServerSocket(0)) {
                this.socket = serverSocket;
            } catch (IOException e) {
                throw new RuntimeException("Available port was not found", e);
            }
        }
        return String.valueOf(this.socket.getLocalPort());
    }

    public synchronized void release() {
        try {
            if (null != this.socket) {
                this.socket.close();
            }
            this.socket = null;
        } catch (Exception e) {
            throw new RuntimeException("Error closing socket port for available port.", e);
        }
    }
}
