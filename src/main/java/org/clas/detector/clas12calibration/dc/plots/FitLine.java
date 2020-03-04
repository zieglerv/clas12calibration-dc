/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.plots;

import org.freehep.math.minuit.MnUserParameters;
import org.jlab.groot.math.Func1D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.timetodistance.T2DFunctions;

/**
 *
 * @author ziegler
 */
public class FitLine extends Func1D{
    public int i;
    public int j;
    public int k;
    private FitFunction fc ;
    public FitLine() {
        super("fcn", 0.0, 2.0);
        fc = new FitFunction();
    }
    public static final int nPars = 11;
    private double[] par = new double[nPars];
    public FitLine(String name, int i, int j, int k, MnUserParameters pars) {
        super(name, 0.0, 2.0);
        this.i = i;
        this.j = j;
        this.k = k;
        fc = new FitFunction();
        this.initParameters(pars);
    }

    private void initParameters(MnUserParameters pars) {
        for(int p = 0; p< nPars; p++) {
            par[p] = pars.value(p);
        }
    }
    @Override
    public double evaluate(double x) { 
        double calcTime = 0;
        double B = 0;
        //local angle correction
        double alpha = PlotMaker.AlphaValues[j];
        if(this.i>1 && this.i<4) {
            double theta0 = Math.toDegrees(Math.acos(1-0.02*PlotMaker.BfieldValuesUpd[i-2][j][k]));
            // correct alpha with theta0, the angle corresponding to the isochrone lines twist due to the electric field
            alpha-=(double)PlotMaker.polarity*theta0;
            B = PlotMaker.BfieldValuesUpd[i-2][j][k];
        }
        //reduce the corrected angle
        double ralpha = (double) fc.getReducedAngle(alpha);
        double v_0 = par[0];
        double vm = par[1];
        double tmax = par[3];
        double distbeta = par[4]; 
        double delBf = par[5]; 
        double Bb1 = par[6]; 
        double Bb2 = par[7]; 
        double Bb3 = par[8]; 
        double Bb4 = par[9]; 
        double R = par[2];
        double dmax = par[10];
        double deltatime_beta = (Math.sqrt(x * x + (distbeta * fc._beta * fc._beta) 
                * (distbeta* fc._beta * fc._beta)) - x) / Constants.V0AVERAGED;

        calcTime = T2DFunctions.polyFcnMac(x,  ralpha,  B,  v_0,  vm,  R, 
            tmax,  dmax,  delBf,  Bb1,  Bb2,  Bb3,  Bb4, i+1) + deltatime_beta ;
        
        //System.out.println("ijk "+i+""+j+""+k+" b "+(float)PlotMaker.BfieldValues[k]+" ralpha "+(float)ralpha+" x "+x+" time "+(float)calcTime);
        return calcTime;
    }

    
}
