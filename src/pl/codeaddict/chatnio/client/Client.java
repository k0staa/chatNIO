/**
 * @author Kostewicz Michał S11474
 */

package pl.codeaddict.chatnio.client;


import javax.swing.*;

public class Client {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientFrame(new ClientConnectionService());
            }
        });

    }
}
