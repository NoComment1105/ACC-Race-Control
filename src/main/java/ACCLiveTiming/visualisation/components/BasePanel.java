/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ACCLiveTiming.visualisation.components;

import ACCLiveTiming.client.BasicAccBroadcastingClient;
import ACCLiveTiming.utility.SpreadSheetService;
import ACCLiveTiming.visualisation.LookAndFeel;
import ACCLiveTiming.visualisation.gui.LPComponent;
import ACCLiveTiming.visualisation.gui.LPContainer;
import ACCLiveTiming.visualisation.gui.LPTabPanel;

/**
 *
 * @author Leonard
 */
public class BasePanel extends LPContainer {

    private final LPComponent header;
    private final LPComponent body;

    public BasePanel(BasicAccBroadcastingClient client) {
        header = new HeaderPanel(client, SpreadSheetService.isRunning());
        addComponent(header);

        LPTabPanel tabs = new LPTabPanel();
        for (LPContainer c : client.getPanels()) {
            tabs.addTab(c);
        }
        tabs.setTabIndex(0);
        body = tabs;
        addComponent(body);
    }

    @Override
    public void onResize(int w, int h) {
        int headerSize = LookAndFeel.LINE_HEIGHT;
        if(SpreadSheetService.isRunning()){
            headerSize = LookAndFeel.LINE_HEIGHT*2;
        }
        header.setSize(w, headerSize);
        header.setPosition(0, 0);
        
        body.setSize(w, h - headerSize);
        body.setPosition(0, headerSize);
        invalidate();
    }

}
