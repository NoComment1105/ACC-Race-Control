/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base.screen.visualisation.components;

import base.screen.networking.AccBroadcastingClient;
import base.screen.visualisation.LookAndFeel;
import base.screen.visualisation.gui.LPButton;
import base.screen.visualisation.gui.LPContainer;
import base.screen.visualisation.gui.LPLabel;
import base.screen.visualisation.gui.LPTextField;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static processing.core.PConstants.CENTER;

/**
 *
 * @author Leonard
 */
public class ConfigPanel
        extends LPContainer {

    /**
     * This classes logger.
     */
    private static Logger LOG = Logger.getLogger(ConfigPanel.class.getName());
    /**
     * Client for game connection.
     */
    private final AccBroadcastingClient client;

    private final LPLabel connectionHeading = new LPLabel("Connection Settings:");
    private final LPLabel ipLabel = new LPLabel("IP:");
    private final LPTextField ipTextField = new LPTextField();
    private final LPLabel portLabel = new LPLabel("Port:");
    private final LPTextField portTextField = new LPTextField();
    private final LPLabel connectionPWLabel = new LPLabel("Connection PW:");
    private final LPTextField connectionPWTextField = new LPTextField();
    private final LPLabel updateIntervalLabel = new LPLabel("Update Interval:");
    private final LPTextField updateIntervalTextField = new LPTextField();
    private final LPButton connectButton = new LPButton("Connect");

    public ConfigPanel(AccBroadcastingClient client) {
        setName("Configuration");

        this.client = client;

        initComponents();
    }

    private void initComponents() {
        connectionHeading.setSize(400, LookAndFeel.LINE_HEIGHT);
        connectionHeading.setHAlign(CENTER);
        addComponent(connectionHeading);

        addComponent(ipLabel);
        ipTextField.setSize(200, LookAndFeel.LINE_HEIGHT);
        ipTextField.setValue("127.0.0.1");
        addComponent(ipTextField);

        addComponent(portLabel);
        portTextField.setSize(200, LookAndFeel.LINE_HEIGHT);
        portTextField.setValue("9000");
        addComponent(portTextField);

        addComponent(connectionPWLabel);
        connectionPWTextField.setSize(200, LookAndFeel.LINE_HEIGHT);
        connectionPWTextField.setValue("asd");
        addComponent(connectionPWTextField);

        addComponent(updateIntervalLabel);
        updateIntervalTextField.setSize(200, LookAndFeel.LINE_HEIGHT);
        updateIntervalTextField.setValue("250");
        addComponent(updateIntervalTextField);

        connectButton.setSize(400, LookAndFeel.LINE_HEIGHT);
        connectButton.setAction(() -> connectButtonPressed());
        addComponent(connectButton);
    }

    private void connectButtonPressed() {
        InetAddress hostAddress;
        try {
            hostAddress = InetAddress.getByName(ipTextField.getValue());
        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(null, ipTextField.getValue() + " is not a valid ip address.");
            return;
        }

        int hostPort;
        try {
            hostPort = Integer.valueOf(portTextField.getValue());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, portTextField.getValue() + " is not a valid port.");
            return;
        }

        int updateInterval;
        try {
            updateInterval = Integer.valueOf(updateIntervalTextField.getValue());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, updateIntervalTextField.getValue() + " is not a valid port.");
            return;
        }

        try {
            client.connect("ACC Live timing",
                    connectionPWTextField.getValue(),
                    "",
                    updateInterval,
                    hostAddress,
                    hostPort);

        } catch (SocketException e) {
            LOG.log(Level.SEVERE, "Error starting the connection to the game.", e);
        }
        /*
        AccBroadcastingClient.ExitState exitstatus = client.waitForFinish();
        if (exitstatus != AccBroadcastingClient.ExitState.NORMAL) {
            //retryConnection = true;
            showErrorMessage(exitstatus);
        }
        */

    }

    private static void showErrorMessage(AccBroadcastingClient.ExitState exitStatus) {
        if (exitStatus == AccBroadcastingClient.ExitState.PORT_UNREACHABLE) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to game. The game needs to be on track to connect.",
                    "Error connecting to game",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (exitStatus == AccBroadcastingClient.ExitState.REFUSED) {
            JOptionPane.showMessageDialog(null,
                    "Connection refused by the game. Wrong password.",
                    "Error connecting to game",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (exitStatus == AccBroadcastingClient.ExitState.EXCEPTION) {
            JOptionPane.showMessageDialog(null,
                    "Unknown error while connecting to game",
                    "Error connecting to game",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void draw() {
        applet.fill(LookAndFeel.COLOR_DARK_GRAY);
        applet.rect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void onResize(int w, int h) {
        int lh = (int) (LookAndFeel.LINE_HEIGHT * 1.2f);
        connectionHeading.setPosition(0, 0);

        ipLabel.setPosition(20, lh * 1 + 10);
        ipTextField.setPosition(200, lh * 1);

        portLabel.setPosition(20, lh * 2 + 10);
        portTextField.setPosition(200, lh * 2f);

        connectionPWLabel.setPosition(20, lh * 3 + 10);
        connectionPWTextField.setPosition(200, lh * 3f);

        updateIntervalLabel.setPosition(20, lh * 4 + 10);
        updateIntervalTextField.setPosition(200, lh * 4);

        connectButton.setPosition(0, lh * 5);
    }

}
