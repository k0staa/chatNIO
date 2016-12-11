package pl.codeaddict.chatnio.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static pl.codeaddict.chatnio.client.ClientServerCommand.SEND;

/**
 * Created by Michal Kostewicz on 05.11.16.
 */
public class ClientFrame extends JFrame{
    private final JLabel mainLabel = new JLabel("<html><h1>Welcome in CHAT-NIO</h1></html>");
    private JButton loginButton;
    private JButton logoutButton;
    private JButton sendMsgButton;
    private JPanel leftButtonsPanel;
    private JPanel rightMsgPanel;
    private JTextField sendMsgTextField;
    private JTextArea chatMsgTextArea;
    private JLabel nicknameLabel;
    private JTextField nicknameTextField;
    private ClientConnectionService clientConnectionService;
    private boolean appRunning;

    public ClientFrame(ClientConnectionService clientConnectionService) {
        super("TPO3");
        this.clientConnectionService = clientConnectionService;
        setLayout(new BorderLayout());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(100, 100, (int) dim.getWidth(), (int) dim.getHeight());
        setLocationRelativeTo(null);
        setSize(dim);
        prepareGUI();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
        appRunning = true;
        new Thread(() -> {
            while (appRunning) {
                try {
                    Thread.sleep(100);
                    String message = clientConnectionService.popMessage();
                    if (message != null) {
                        chatMsgTextArea.append(message);
                    }
                } catch (InterruptedException e) {
                    System.out.println("GUI Thread problem: " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

    private void prepareGUI() {
        //Left Buttons Panel setup
        leftButtonsPanel = new JPanel();
        leftButtonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        loginButton = new JButton("Login");
        loginButton.addActionListener((event) -> {
            nicknameTextField.setEnabled(false);
            clientConnectionService.connect(nicknameTextField.getText());
        });
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener((event) -> {
            clientConnectionService.disconnect(nicknameTextField.getText());
            nicknameTextField.setEnabled(true);
        });
        nicknameLabel = new JLabel("User nickname:");
        nicknameTextField = new JTextField();
        nicknameTextField.setText("user");

        c.weighty = 0.1;
        leftButtonsPanel.add(mainLabel);
        c.gridy = 1;
        leftButtonsPanel.add(loginButton, c);
        c.gridy = 2;
        leftButtonsPanel.add(logoutButton, c);
        c.gridy = 3;
        leftButtonsPanel.add(nicknameLabel, c);
        c.gridy = 4;
        leftButtonsPanel.add(nicknameTextField, c);

        this.add(leftButtonsPanel, BorderLayout.NORTH);

        //Right Messages Panel
        rightMsgPanel = new JPanel();
        rightMsgPanel.setLayout(new GridBagLayout());
        GridBagConstraints rightPanelGridBagConstraint = new GridBagConstraints();
        rightPanelGridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
        sendMsgButton = new JButton("Send");
        sendMsgButton.addActionListener((event) -> {
            clientConnectionService.sendMessage(SEND, nicknameTextField.getText()
                    , sendMsgTextField.getText());
            sendMsgTextField.setText("");
        });
        sendMsgTextField = new JTextField();
        sendMsgTextField.addKeyListener(new SendMessageTextFieldKeyListener(this));
        chatMsgTextArea = new JTextArea(30, 20);
        chatMsgTextArea.setLineWrap(true);
        JScrollPane scrollPaneWithChatTextArea = new JScrollPane(chatMsgTextArea);
        rightPanelGridBagConstraint.gridy = 0;
        rightPanelGridBagConstraint.gridx = 0;
        rightMsgPanel.add(sendMsgButton, rightPanelGridBagConstraint);
        rightPanelGridBagConstraint.gridx = 1;
        rightPanelGridBagConstraint.gridy = 0;
        rightMsgPanel.add(sendMsgTextField, rightPanelGridBagConstraint);
        rightPanelGridBagConstraint.gridy = 1;
        rightPanelGridBagConstraint.weightx = 0.7;
        rightMsgPanel.add(scrollPaneWithChatTextArea, rightPanelGridBagConstraint);
        this.add(rightMsgPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                appRunning = false;
                clientConnectionService.disconnect(nicknameTextField.getText());
                dispose();
            }
        });
    }

    private static class SendMessageTextFieldKeyListener implements KeyListener {
        private JTextField nicknameTextField;
        private JTextField sendMsgTextField;
        private ClientConnectionService clientConnectionService;

        public SendMessageTextFieldKeyListener(ClientFrame clientFrame) {
            this.nicknameTextField = clientFrame.nicknameTextField;
            this.sendMsgTextField = clientFrame.sendMsgTextField;
            this.clientConnectionService = clientFrame.clientConnectionService;
        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                clientConnectionService.sendMessage(SEND, nicknameTextField.getText()
                        , sendMsgTextField.getText());
                sendMsgTextField.setText("");
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

        }
    }
}


