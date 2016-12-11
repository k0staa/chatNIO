package pl.codeaddict.chatnio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Created by Michal Kostewicz on 06.11.16.
 */
public class ClientConnectionService {
    public final static int port = 6001;
    private static Charset charset = Charset.forName("ISO-8859-2");
    private String server = "localhost";
    private SocketChannel channel;
    private LinkedList<String> messageList;
    private Boolean connectedToServer;

    public ClientConnectionService() {
        this.messageList = new LinkedList<>();
        this.connectedToServer = false;
    }

    public void connect(String nickname) {
        try {
            if (connectedToServer) {
                return;
            }
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            if (!channel.isOpen()) {
                channel = SocketChannel.open();
            }
            channel.connect(new InetSocketAddress(server, port));
            System.out.println("CLIENT LOG: Connecting to " + server + " on port " + port);
            while (!channel.finishConnect()) {
                try {
                    Thread.sleep(200);
                } catch (Exception exc) {
                    return;
                }
                System.out.print(".");
            }
            System.out.println("CLIENT LOG: Connected!");
            connectedToServer = true;

            // Create a new thread to listen to InputStream event
            InputStreamEvent inputStreamEvent = new InputStreamEvent(channel, messageList, connectedToServer);
            inputStreamEvent.start();
            sendMessage(ClientServerCommand.LOGIN,nickname, "");
        } catch (IOException exc) {
            System.out.println("CLIENT LOG: ERROR -> Problem with connecting to " + server +
                    " , " + exc.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void disconnect(String nickname) {
        try {
            if (!connectedToServer) {
                return;
            }
            sendMessage(ClientServerCommand.EXIT, nickname, "");
            connectedToServer = false;
        } catch (Exception exc) {
            System.out.println("CLIENT LOG: ERROR -> Problem with disconnecting " + server);
            System.exit(3);
        }
    }

    public boolean sendMessage(ClientServerCommand clientServerCommand, String nickname, String msg) {
        if(!connectedToServer){
            return false;
        }
        String messageToSend = clientServerCommand.name() + " " + nickname + ": " + msg + '\n';
        System.out.println("CLIENT LOG: Sending message: " + messageToSend);
        ByteBuffer messageByteBuffer = ByteBuffer.wrap(messageToSend.getBytes(charset));
        try {
            channel.write(messageByteBuffer);
        } catch (IOException e) {
            System.out.println("CLIENT LOG: Error -> Problem with read or write to/from server " +
                    e.getLocalizedMessage());
        }
        return true;
    }

    public String popMessage() {
        if (messageList.size() > 0) {
            return this.messageList.pop();
        }
        return null;
    }

    static class InputStreamEvent extends Thread {
        private SocketChannel socketChannel;
        private LinkedList messageList;
        private Boolean connectedToServer;

        public InputStreamEvent(SocketChannel socketChannel, LinkedList messageList, Boolean connectedToServer) {
            this.socketChannel = socketChannel;
            this.messageList = messageList;
            this.connectedToServer = connectedToServer;
        }

        public void run() {
            try {
                ByteBuffer inBuf = ByteBuffer.allocate(8192);

                while (connectedToServer) {
                    inBuf.clear();
                    int readBytes = 0;
                    readBytes = socketChannel.read(inBuf);
                    if (readBytes == 0) {
                        continue;
                    } else if (readBytes == -1) {
                        break;
                    } else {
                        inBuf.flip();
                        String response = new String(inBuf.array(), 0, readBytes);
                        System.out.println("CLIENT LOG: Server response -> " + response);
                        response.trim();
                        this.messageList.add(response);
                    }
                }
                socketChannel.close();
                socketChannel.socket().close();
                System.out.println("CLIENT LOG: Server logout!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

