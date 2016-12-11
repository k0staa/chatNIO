/**
 * @author Kostewicz Michał S11474
 */

package pl.codeaddict.chatnio;


import pl.codeaddict.chatnio.client.ClientConnectionService;
import pl.codeaddict.chatnio.client.ClientFrame;
import pl.codeaddict.chatnio.server.Server;

import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        Thread serverThread = new Thread(() -> {
            new Server();
        });
        serverThread.start();
        Thread client1Thread = new Thread(() -> {
            new ClientFrame(new ClientConnectionService());
        });
        client1Thread.start();
        Thread client2Thread = new Thread(() -> {
            new ClientFrame(new ClientConnectionService());
        });
        client2Thread.start();
    }
}
