/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base.extensions.fullcourseyellow;

import base.ACCLiveTimingExtensionModule;
import base.screen.extensions.AccClientExtension;
import base.screen.visualisation.gui.LPContainer;
import javax.swing.JPanel;

/**
 *
 * @author Leonard
 */
public class FullCourseYellowExtensionModule implements ACCLiveTimingExtensionModule{
    
    private FullCourseYellowExtension extension;
    private FullCourseYellowPanel panel;
    
    public FullCourseYellowExtensionModule(){
        extension = new FullCourseYellowExtension();
        panel = new FullCourseYellowPanel(extension);
    }

    @Override
    public String getName() {
        return "Full Course Yellow extension";
    }

    @Override
    public AccClientExtension getExtension() {
        return extension;
    }

    @Override
    public LPContainer getExtensionPanel() {
        return panel;
    }

    @Override
    public JPanel getExtensionConfigurationPanel() {
        return null;
    }
    
}