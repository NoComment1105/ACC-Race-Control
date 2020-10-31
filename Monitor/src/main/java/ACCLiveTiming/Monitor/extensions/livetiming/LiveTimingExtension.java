/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ACCLiveTiming.monitor.extensions.livetiming;

import ACCLiveTiming.monitor.client.events.RealtimeCarUpdate;
import ACCLiveTiming.monitor.client.events.RealtimeUpdate;
import ACCLiveTiming.monitor.eventbus.Event;
import ACCLiveTiming.monitor.eventbus.EventBus;
import ACCLiveTiming.monitor.eventbus.EventListener;
import ACCLiveTiming.monitor.extensions.AccClientExtension;
import ACCLiveTiming.monitor.networking.data.CarInfo;
import ACCLiveTiming.monitor.networking.data.RealtimeInfo;
import ACCLiveTiming.monitor.networking.data.SessionInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Leonard
 */
public class LiveTimingExtension
        extends AccClientExtension
        implements EventListener {

    /**
     * This classes logger.
     */
    private static Logger LOG = Logger.getLogger(LiveTimingExtension.class.getName());
    /**
     * Map from carId to ListEntry.
     */
    private final Map<Integer, CarInfo> entires = new HashMap<>();
    /**
     * Table model to display the live timing.
     */
    private LiveTimingTableModel model = new LiveTimingTableModel();

    public LiveTimingExtension() {
        EventBus.register(this);
    }

    public LiveTimingTableModel getTableModel() {
        return model;
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof RealtimeUpdate) {
            onRealtimeUpdate(((RealtimeUpdate) e).getSessionInfo());
        } else if (e instanceof RealtimeCarUpdate) {
            onRealtimeCarUpdate(((RealtimeCarUpdate) e).getInfo());
        }
    }

    public void onRealtimeUpdate(SessionInfo sessionInfo) {
        List<CarInfo> sorted = entires.values().stream()
                .filter(entry -> entry.isConnected())
                .sorted((e1, e2) -> compareTo(e1, e2))
                .collect(Collectors.toList());

        model.setEntries(sorted);
        model.setFocusedCarId(sessionInfo.getFocusedCarIndex());
    }

    public void onRealtimeCarUpdate(RealtimeInfo info) {
        CarInfo car = client.getModel().getCarsInfo().getOrDefault(info.getCarId(), new CarInfo());
        entires.put(car.getCarId(), car);
    }

    private int compareTo(CarInfo c1, CarInfo c2) {
        return (int) Math.signum(c1.getRealtime().getPosition() - c2.getRealtime().getPosition());
    }

    private boolean isFocused(CarInfo car) {
        return car.getCarId() == client.getModel().getSessionInfo().getFocusedCarIndex();
    }

}
