/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.analysis;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class TupleMaker extends ReconstructionEngine{

    public TupleMaker(String name, String author, String version) {
        super(name, author, version);
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
