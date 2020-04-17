/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.calt0;
import org.freehep.math.minuit.FCNBase; 
import org.jlab.groot.data.H1F;

/**
 *
 * @author ziegler
 */
public class FitFunction implements FCNBase{

    private H1F _tdc;
    
    public FitFunction() {
        
    }
    public FitFunction(H1F tdc) {
        
        _tdc = tdc;
    }
         
    public double eval(double x, double[] par) {
        
        double value =  par[1] * (x - par[0]);
        return value;
        
        
    }
    
    @Override
    public double valueOf(double[] par) {
        double chisq = 0;
        double delta = 0;
        H1F gr = _tdc;
        
        int maxbin = gr.getMaximumBin();
        int halfmaxbin = 0;
        double halfmax = gr.getBinContent(maxbin)/2;
        
        for (int ix =0; ix< maxbin; ix++) {
            double y = gr.getBinContent(ix);
            double err = gr.getBinError(ix);
            
            if(err>0 && y>0) {
                if(y>halfmax) {
                    
                    halfmaxbin = ix-1;
                    break;
                }
                    
            }
        }
        int delBin = (maxbin - halfmaxbin)/2;
        System.out.println("delBin "+delBin);
        for (int ix =halfmaxbin-delBin; ix< halfmaxbin+delBin; ix++) {
            double x = gr.getDataX(ix);
            double y = gr.getBinContent(ix);
            double err = gr.getBinError(ix);
            if(err>0 && y>0) {
                double f = this.eval(x, par);
                delta = (y - f) / err; 
                chisq += delta * delta;
            }
        }
        return chisq;
        
    }
    
}
