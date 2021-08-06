/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.calt0;

import org.freehep.math.minuit.MnUserParameters;
import org.jlab.groot.math.Func1D; 

/**
 *
 * @author kpadhikari, ziegler
 */
public class FitLine extends Func1D{
    public int i;
    public int j;
    public int k;
    public int l;
    public double min ;
    public double max ;
    public FitLine() {
        super("fcn", -100, 100);
    }
    public static final int nPars = 2;
    private double[] par = new double[nPars];
    public FitLine(String name, int i, int j, int k, int l, double min, double max) {
        super(name, min, max);
        this.i = i;
        this.j = j;
        this.k = k;
        this.l = l;
        this.min = min;
        this.max = max;
    }
    
    public FitLine(String name, int i, int j, double min, double max) {
        super(name, min, max);
        this.i = i;
        this.j = j;
        this.min = min;
        this.max = max;
    }

    public void setParameters(double[] pars) {
        for(int p = 0; p< nPars; p++) {
            par[p] = pars[p];
        }
    }
    @Override
    public double evaluate(double x) { 
        return par[0]*x+par[1];
    }
       
}
