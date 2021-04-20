/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base.extensions.googlesheetsapi;

import base.screen.visualisation.gui.LPContainer;
import base.ACCLiveTimingExtensionFactory;
import base.screen.extensions.AccClientExtension;

/**
 *
 * @author Leonard
 */
public class GoogleSheetsAPIExtensionFactory
        implements ACCLiveTimingExtensionFactory {

    private GoogleSheetsAPIConfigurationPanel configurationPanel;
    private GoogleSheetsAPIExtension extension;

    public GoogleSheetsAPIExtensionFactory() {

    }

    @Override
    public String getName() {
        return "Google Sheets API extension";
    }

    @Override
    public void createExtension() {
        removeExtension();
        if (configurationPanel.isExtensionEnabled()) {
            extension = new GoogleSheetsAPIExtension();
            extension.setReplayOffsetCell(configurationPanel.getReplayOffsetCell());
            extension.setFindEmptyRowRange(configurationPanel.getFindEmptyRowRange());
            extension.setSessionColumn(configurationPanel.getSessionColumn());
            extension.setCarInfoColumn(configurationPanel.getCarColumn());
            extension.start(configurationPanel.getSpreadSheetLink());
        }
    }

    @Override
    public LPContainer getExtensionConfigurationPanel() {
        if (configurationPanel == null) {
            configurationPanel = new GoogleSheetsAPIConfigurationPanel();
        }
        return configurationPanel;
    }

    @Override
    public void removeExtension() {
        if (extension != null) {
            extension.removeExtension();
            extension = null;
        }
    }

    @Override
    public AccClientExtension getExtension() {
        return extension;
    }

}
