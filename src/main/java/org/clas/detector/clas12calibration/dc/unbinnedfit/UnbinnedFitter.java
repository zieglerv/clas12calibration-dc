/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.unbinnedfit;

import java.util.HashMap;
import java.util.Map;
import static org.clas.detector.clas12calibration.dc.constants.Constants.wpdist;
import org.clas.detector.clas12calibration.dc.fit.DCTimeFunction;
import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.timetodistance.T2DFunctions;

/**
 *
 * @author ziegler
 */
public class UnbinnedFitter implements FCNBase {
    
    Map<Integer, Double> AlphaMapTBHits = new HashMap<Integer, Double>();
    DataBank bnkHits = null;
    DataBank segsMatch = null;
    public UnbinnedFitter() {
        
    }
    public void processEvent(DataEvent event) {
        AlphaMapTBHits.clear();
        bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
        segsMatch = (DataBank) event.getBank("TimeBasedTrkg::TBSegments");
        
        bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
                segsMatch = (DataBank) event.getBank("TimeBasedTrkg::TBSegments");
                
        for (int j = 0; j < segsMatch.rows(); j++){
            AlphaMapTBHits.put(segsMatch.getInt("Cluster_ID", j), (double) 
                    Math.toDegrees(Math.atan2(segsMatch.getFloat("fitSlope", j),1))
            );
        }


        for (int j = 0; j < bnkHits.rows(); j++){
            if(bnkHits.getFloat("TProp", j)==0 || bnkHits.getFloat("TFlight", j)==0 
                    || (double) bnkHits.getFloat("beta", j) <0.99 || 
                    (double) bnkHits.getFloat("beta", j)>1.01)
                continue; // select electrons
            superlayer = bnkHits.getInt("superlayer", j);
            time = DCTimeFunction.computeCalibTime(bnkHits.getInt("sector", j),
                    superlayer, 
                    bnkHits.getFloat("doca", j),
                    bnkHits.getInt("TDC", j) ,
                    bnkHits.getFloat("TProp", j),
                    bnkHits.getFloat("TFlight", j), 
                    bnkHits.getFloat("TStart", j),
                    bnkHits.getFloat("T0", j),
                    bnkHits.getFloat("beta", j));     
            x = (double)bnkHits.getFloat("doca", j);
            beta = (double)bnkHits.getFloat("beta", j);
            B = (double)bnkHits.getFloat("B", j);
            err = (double)bnkHits.getFloat("docaErr", j);
            int polarity = (int)Math.signum(event.getBank("RUN::config").getFloat("torus",0));
            //local angle correction
            double theta0 = Math.toDegrees(Math.acos(1-0.02*bnkHits.getFloat("B", j)));
            double alpha = AlphaMapTBHits.get(bnkHits.getInt("clusterID", j));                    
            // correct alpha with theta0, the angle corresponding to the isochrone lines twist due to the electric field
            alpha+=(double)polarity*theta0;
            //reduce the corrected angle
            ralpha = (double) this.getReducedAngle(alpha);
                       
        }
    }
    public int superlayer;
    public double ralpha;
    public double x;
    public double beta;
    public double B;
    public double time;
    public double err;
    
    public void runFitter() {
        MnMigrad migrad = new MnMigrad(this, new MnUserParameters());
    }
    @Override
    public double valueOf(double[] par) {
        double chisq = 0;
        double delta = 0;
        double dmax = 2 * wpdist[superlayer-1];
        double v_0 = par[0];
        double vm = par[1];
        double tmax = par[2];
        double distbeta = par[3]; 
        double delBf = par[4]; 
        double Bb1 = par[5]; 
        double Bb2 = par[6]; 
        double Bb3 = par[7]; 
        double Bb4 = par[8]; 
        double R = par[9];

        double deltatime_beta = (Math.sqrt(x * x + (distbeta * beta * beta) * (distbeta* beta * beta)) - x) / Constants.V0AVERAGED;

        double calcTime = T2DFunctions.polyFcnMac(x,  ralpha,  B,  v_0,  vm,  R, 
            tmax,  dmax,  delBf,  Bb1,  Bb2,  Bb3,  Bb4, superlayer) + deltatime_beta;
        delta = (time - calcTime) / err; 
        chisq += delta * delta;
        
        return chisq;
    }
    double getReducedAngle(double alpha) {
        double ralpha = 0;

        ralpha = Math.abs(Math.toRadians(alpha));

        while (ralpha > Math.PI / 3.) {
            ralpha -= Math.PI / 3.;
        }
        if (ralpha > Math.PI / 6.) {
            ralpha = Math.PI / 3. - ralpha;
        }

        return Math.toDegrees(ralpha);
    }  
}
