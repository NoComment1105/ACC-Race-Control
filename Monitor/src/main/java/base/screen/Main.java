/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base.screen;

import base.screen.visualisation.Visualisation;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import processing.core.PApplet;
import base.screen.extensions.AccClientExtension;
import base.screen.networking.AccBroadcastingClient;
import base.screen.visualisation.gui.LPContainer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import base.ACCLiveTimingExtensionFactory;

/**
 *
 * @author Leonard
 */
public class Main {

    /**
     * This classes logger.
     */
    private static Logger LOG = Logger.getLogger(Main.class.getName());
    /**
     * Extension modules.
     */
    private static List<ACCLiveTimingExtensionFactory> modules = new LinkedList<>();
    /**
     * List of client extensions.
     */
    private static List<AccClientExtension> extensions = new LinkedList<>();
    /**
     * Connection dialog
     */
    private static ConnectionDialog dialog = new ConnectionDialog();
    /**
     * Connection client.
     */
    private static AccBroadcastingClient client;
    /**
     * Visualisation for this program.
     */
    private static Visualisation visualisation;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new UncoughtExceptionHandler());
        setupLogging();
        loadModules();

        client = new AccBroadcastingClient();
        visualisation = new Visualisation(client);

        //start the visualisation
        String[] a = {"MAIN"};
        PApplet.runSketch(a, visualisation);

        //start the client
        startConnection();

        //stop the program
        visualisation.exitExplicit();
    }

    private static void setupLogging() {
        //set logging file.
        LogManager logManager = LogManager.getLogManager();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String logPath = System.getProperty("user.dir") + "/log/" + dateFormat.format(new Date()) + ".log";
            Properties prop = new Properties();
            prop.load(Main.class.getResourceAsStream("/logging.properties"));
            prop.put("java.util.logging.FileHandler.pattern", logPath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            prop.store(out, "");
            logManager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An error happened while setting up the logger.", e);
        }
    }

    private static void loadModules() {
        ServiceLoader.load(ACCLiveTimingExtensionFactory.class).forEach(module -> {
            LOG.info("Loading extension " + module.getName());
            modules.add(module);
        });
    }

    private static void showConfigurationDialog() {
        for (ACCLiveTimingExtensionFactory module : modules) {
            JPanel configurationPanel = module.getExtensionConfigurationPanel();
            if (configurationPanel != null) {
                dialog.addTabPanel(configurationPanel);
            }
        }
        dialog.setVisible(true);
    }

    public static void startConnection() {
        LOG.info("Starting");

        boolean retryConnection = true;
        while (retryConnection) {
            retryConnection = false;
            showConfigurationDialog();
            if (dialog.exitWithConnect()) {
                //connect the client.
                try {
                    client.connect(dialog.getDisplayName(),
                            dialog.getConnectionPassword(),
                            dialog.getCommandPassword(),
                            dialog.getUpdateInterval(),
                            dialog.getHostAddress(),
                            dialog.getPort());

                } catch (SocketException e) {
                    LOG.log(Level.SEVERE, "Error starting the connection to the game.", e);
                }

                //enable extensions.
                extensions.clear();
                for (ACCLiveTimingExtensionFactory module : modules) {
                    AccClientExtension extension = module.createExtension();
                    if (extension != null) {
                        extensions.add(extension);
                    }
                }

                //send registration.
                client.sendRegisterRequest();

                //wait for the client to be closed or for a critical failure that
                //closes the client.
                AccBroadcastingClient.ExitState exitstatus = client.waitForFinish();
                if (exitstatus != AccBroadcastingClient.ExitState.NORMAL) {
                    retryConnection = true;
                    showErrorMessage(exitstatus);
                }
            }
        }

        LOG.info("Stopping");
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

    public static List<LPContainer> getExtensionPanels() {
        return extensions.stream()
                .map(extension -> extension.getPanel())
                .filter(panel -> panel != null)
                .collect(Collectors.toList());
    }

    public static AccBroadcastingClient getClient() {
        return client;
    }

    public static class UncoughtExceptionHandler
            implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOG.log(Level.SEVERE, "Uncought exception:", e);
        }

    }

}
