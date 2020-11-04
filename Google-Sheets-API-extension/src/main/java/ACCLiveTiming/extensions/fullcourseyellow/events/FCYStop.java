/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ACCLiveTiming.extensions.fullcourseyellow.events;

import ACCLiveTiming.monitor.eventbus.Event;

/**
 *
 * @author Leonard
 */
public class FCYStop extends Event {
    
    private final float sessionTime;

    public FCYStop(float sessionTime) {
        this.sessionTime = sessionTime;
    }

    public float getSessionTime() {
        return sessionTime;
    }
    
}
