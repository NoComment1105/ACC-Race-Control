/*
 * Copyright (c) 2021 Leonard Sch�ngel
 * 
 * For licensing information see the included license (LICENSE.txt)
 */
package base.screen.extensions.incidents;

import base.screen.Main;
import base.screen.networking.SessionId;
import base.screen.networking.events.AfterPacketReceived;
import base.screen.networking.BroadcastingEventEvent;
import base.screen.eventbus.Event;
import base.screen.eventbus.EventBus;
import base.screen.eventbus.EventListener;
import base.screen.extensions.AccClientExtension;
import base.screen.extensions.GeneralExtentionConfigPanel;
import base.screen.extensions.logging.LoggingExtension;
import base.screen.networking.data.AccBroadcastingData;
import base.screen.networking.data.BroadcastingEvent;
import base.screen.networking.enums.BroadcastingEventType;
import base.screen.utility.TimeUtils;
import base.screen.extensions.incidents.events.Accident;
import base.screen.extensions.replayoffset.ReplayOffsetExtension;
import base.screen.extensions.replayoffset.ReplayStart;
import base.screen.networking.AccBroadcastingClient;
import base.screen.visualisation.gui.LPContainer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Leonard
 */
public class IncidentExtension
        implements EventListener, AccClientExtension {

    /**
     * This classes logger.
     */
    private static final Logger LOG = Logger.getLogger(IncidentExtension.class.getName());
    /**
     * Reference to the client.
     */
    private final AccBroadcastingClient client;
    /**
     * The visualisation for this extension.
     */
    private final IncidentPanel panel;
    /**
     * Last accident that is waiting to be commited.
     */
    private IncidentInfo stagedAccident = null;
    /**
     * List of accidents that have happened.
     */
    private List<IncidentInfo> accidents = new LinkedList<>();
    /**
     * Table model for the incident panel table.
     */
    private final IncidentTableModel model = new IncidentTableModel();
    /**
     * Flag indicates that the replay offset is known.
     */
    private boolean replayTimeKnown = false;

    public IncidentExtension() {
        this.client = Main.getClient();
        this.panel = new IncidentPanel(this);
        EventBus.register(this);
    }

    @Override
    public LPContainer getPanel() {
        if (GeneralExtentionConfigPanel.getInstance().isIncidentLogEnabled()) {
            return panel;
        }
        return null;
    }

    public AccBroadcastingData getModel() {
        return client.getModel();
    }

    public IncidentTableModel getTableModel() {
        return model;
    }

    public List<IncidentInfo> getAccidents() {
        List<IncidentInfo> a = new LinkedList<>(accidents);
        Collections.reverse(a);
        return Collections.unmodifiableList(a);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof AfterPacketReceived) {
            afterPacketReceived(((AfterPacketReceived) e).getType());
            if (!replayTimeKnown && ReplayOffsetExtension.requireSearch()) {
                panel.enableSearchButton();
            }
        } else if (e instanceof BroadcastingEventEvent) {
            BroadcastingEvent event = ((BroadcastingEventEvent) e).getEvent();
            if (event.getType() == BroadcastingEventType.ACCIDENT) {
                onAccident(event);
            }
        } else if (e instanceof ReplayStart) {
            replayTimeKnown = true;
            panel.setReplayOffsetKnown();
            updateAccidentsWithReplayTime();
        }
    }

    public void afterPacketReceived(byte type) {
        if (stagedAccident != null) {
            long now = System.currentTimeMillis();
            if (now - stagedAccident.getSystemTimestamp() > 1000) {
                commitAccident(stagedAccident);
                stagedAccident = null;
            }
        }
    }

    public void onAccident(BroadcastingEvent event) {
        float sessionTime = client.getModel().getSessionInfo().getSessionTime();
        String logMessage = "Accident: #" + client.getModel().getCar(event.getCarId()).getCarNumber()
                + "\t" + TimeUtils.asDuration(sessionTime)
                + "\t" + TimeUtils.asDuration(ReplayOffsetExtension.getReplayTimeFromConnectionTime(event.getTimeMs()));
        LoggingExtension.log(logMessage);
        LOG.info(logMessage);

        SessionId sessionId = client.getSessionId();
        if (stagedAccident == null) {
            stagedAccident = new IncidentInfo(sessionTime,
                    client.getModel().getCar(event.getCarId()),
                    sessionId);
        } else {
            float timeDif = stagedAccident.getSessionLatestTime() - sessionTime;
            if (timeDif > 1000) {
                commitAccident(stagedAccident);
                stagedAccident = new IncidentInfo(sessionTime,
                        client.getModel().getCar(event.getCarId()),
                        sessionId);
            } else {
                stagedAccident = stagedAccident.addCar(sessionTime,
                        client.getModel().getCar(event.getCarId()),
                        System.currentTimeMillis());
            }
        }
    }

    public void addEmptyAccident() {
        commitAccident(new IncidentInfo(client.getModel().getSessionInfo().getSessionTime(),
                client.getSessionId()));
    }

    private void commitAccident(IncidentInfo a) {
        List<IncidentInfo> newAccidents = new LinkedList<>();
        newAccidents.addAll(accidents);
        newAccidents.add(a);
        accidents = newAccidents;
        model.setAccidents(accidents);

        EventBus.publish(new Accident(a));
        panel.invalidate();
    }

    @Override
    public void removeExtension() {
        EventBus.unregister(this);
    }

    private void updateAccidentsWithReplayTime() {
        SessionId currentSessionId = Main.getClient().getSessionId();
        List<IncidentInfo> newAccidents = new LinkedList<>();
        for (IncidentInfo incident : accidents) {
            if (incident.getSessionID().equals(currentSessionId)) {
                newAccidents.add(incident.withReplayTime(
                        ReplayOffsetExtension.getReplayTimeFromSessionTime((int) incident.getSessionEarliestTime())
                ));
            }
        }
        accidents = newAccidents;
        model.setAccidents(accidents);
        panel.invalidate();

        if (stagedAccident != null) {
            stagedAccident = stagedAccident.withReplayTime(
                    ReplayOffsetExtension.getReplayTimeFromSessionTime((int) stagedAccident.getSessionEarliestTime())
            );
        }
    }

}
