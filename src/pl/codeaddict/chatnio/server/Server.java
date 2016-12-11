/**
 * @author Kostewicz Michał S11474
 */

package pl.codeaddict.chatnio.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class Server {
    private static Pattern reqPatt = Pattern.compile(" +", 3);
    private static Charset charset = Charset.forName("ISO-8859-2");
    private static final int BSIZE = 1024;
    private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);
    private StringBuffer requestString = new StringBuffer();
    private Set<SocketChannel> clientChannels;

    private ServerSocketChannel ssc = null;
    private Selector selector = null;
    private String host = "localhost";
    private int port = 6001;

    public Server() {
        try {
            clientChannels = new HashSet<>();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(host, port));
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }
        System.out.println("SERVER LOG: Server started and ready for handling requests");
        serviceConnections();
    }

    private void serviceConnections() {
        boolean serverIsRunning = true;

        while (serverIsRunning) {
            try {
                selector.select();
                Set keys = selector.selectedKeys();
                Iterator iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = (SelectionKey) iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        SocketChannel cc = ssc.accept();
                        this.clientChannels.add(cc);
                        cc.configureBlocking(false);
                        cc.register(selector, SelectionKey.OP_READ);
                        continue;
                    }

                    if (key.isReadable()) {
                        SocketChannel cc = (SocketChannel) key.channel();
                        serviceRequest(cc);
                        continue;
                    }
                }
            } catch (Exception exc) {
                exc.printStackTrace();
                continue;
            }
        }
    }

    private void serviceRequest(SocketChannel sc) {
        if (!sc.isOpen()) {
            return;
        }
        requestString.setLength(0);
        bbuf.clear();
        try {
            readLoop:
            while (true) {
                int n = sc.read(bbuf);
                if (n > 0) {
                    bbuf.flip();
                    CharBuffer cbuf = charset.decode(bbuf);
                    while (cbuf.hasRemaining()) {
                        char c = cbuf.get();
                        if (c == '\r' || c == '\n') {
                            break readLoop;
                        }
                        requestString.append(c);
                    }
                }
            }
            // analize request
            String[] req = reqPatt.split(requestString, 3);
            String cmd = req[0];

            if (cmd.equals("EXIT")) {
                String[] logoutMessage = reqPatt.split(ServerMessage.LOGOUT.getMessage(), 3);
                String[] message = Arrays.copyOf(req, req.length + logoutMessage.length);
                System.arraycopy(logoutMessage, 0, message, req.length, logoutMessage.length);
                writeResp(prepareEchoMessage(message));
                System.out.println("SERVER LOG: Client logout successfully!");
                sc.close();
                clientChannels.remove(sc);
                //sc.socket().close();
            } else if (cmd.equals("SEND")) {
                //do the job
                System.out.println("SERVER LOG: Handling GET request: " + Arrays.deepToString(req));
                writeResp(prepareEchoMessage(req));
            } else if (cmd.equals("LOGIN")) {
                String[] loginMessage = reqPatt.split(ServerMessage.LOGIN.getMessage(), 3);
                String[] message = Arrays.copyOf(req, req.length + loginMessage.length);
                System.arraycopy(loginMessage, 0, message, req.length, loginMessage.length);
                writeResp(prepareEchoMessage(message));
                System.out.println("SERVER LOG: Client login successfully!");
            } else {
                writeResp(ServerMessage.INVALID_COMMAND.getMessage());
            }

        } catch (Exception exc) {
            exc.printStackTrace();
            try {
                sc.close();
                sc.socket().close();
            } catch (Exception e) {
            }
        }
    }

    private String prepareEchoMessage(String[] messages) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int x = 1; x < messages.length; x++) {
            stringBuilder.append(messages[x]);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    private StringBuffer echoResponse = new StringBuffer();

    private void writeResp(String addMsg)
            throws IOException {
        for (SocketChannel clientSocketChannel : clientChannels) {
            echoResponse.setLength(0);
            if (addMsg != null) {
                echoResponse.append(addMsg);
            }
            echoResponse.append('\n');
            ByteBuffer buf = charset.encode(CharBuffer.wrap(echoResponse));
            clientSocketChannel.write(buf);
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
