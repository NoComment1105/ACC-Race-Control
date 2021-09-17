/*
 * Copyright (c) 2021 Leonard Sch�ngel
 * 
 * For licensing information see the included license (LICENSE.txt)
 */
package racecontrol.app.racecontrol.virtualsafetycar;

import racecontrol.app.racecontrol.virtualsafetycar.controller.VirtualSafetyCarController;
import racecontrol.app.AppController;
import racecontrol.app.racecontrol.virtualsafetycar.controller.VSCEndEvent;
import racecontrol.app.racecontrol.virtualsafetycar.controller.VSCStartEvent;
import racecontrol.client.events.AfterPacketReceivedEvent;
import racecontrol.eventbus.Event;
import racecontrol.eventbus.EventBus;
import racecontrol.eventbus.EventListener;

/**
 *
 * @author Leonard
 */
public class VirtualSafetyCarConfigController
        implements EventListener {

    /**
     * Settings panel.
     */
    private final VirtualSafetyCarPanel panel = new VirtualSafetyCarPanel();
    /**
     * The app controller.
     */
    private final AppController appController;
    /**
     * Reference to the vsc controller.
     */
    private final VirtualSafetyCarController vscController;
    /**
     * The status panel to show a VSC is active.
     */
    private final VSCStatusPanel statusPanel;

    public VirtualSafetyCarConfigController() {
        EventBus.register(this);
        appController = AppController.getInstance();
        vscController = VirtualSafetyCarController.getInstance();
        statusPanel = new VSCStatusPanel();

        panel.startButton.setAction(() -> startVSC());
        panel.stopButton.setAction(() -> vscController.stopVSC());
        statusPanel.settingsButton.setAction(() -> openSettingsPanel());
    }

    public void openSettingsPanel() {
        appController.launchNewWindow(panel, false);
    }

    private void startVSC() {
        vscController.startVSC(panel.targetSpeedTextField.getNumber(),
                panel.speedToleranceTextField.getNumber(),
                panel.timeToleranceTextField.getNumber() * 1000);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof VSCStartEvent) {
            panel.isVSCDisabled = false;
            panel.updateComponents();
            panel.invalidate();

            statusPanel.setVSCStart();
            appController.addStatusPanel(statusPanel);
        } else if (e instanceof VSCEndEvent) {
            panel.isVSCDisabled = true;
            panel.updateComponents();
            panel.invalidate();

            appController.removeStatusPanel(statusPanel);
        } else if (e instanceof AfterPacketReceivedEvent) {
            if (vscController.isActive()) {
                statusPanel.invalidate();
            }
        }
    }

}