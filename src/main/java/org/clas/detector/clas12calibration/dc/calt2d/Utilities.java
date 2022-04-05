/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.calt2d;

import org.jlab.rec.dc.Constants;

/**
 *
 * @author ziegler
 */
public class Utilities {
    public static boolean NEWDELTATBETAFCN = true;
    
    public double calcDeltaTimeBeta(double x, double distbeta, double beta){
        return (Math.sqrt(x * x + (distbeta * beta * beta) * 
                (distbeta * beta * beta)) - x) / Constants.V0AVERAGED;
    }
    
    public double calcDeltaTimeBetaNewFCN(double t,double distbeta, double beta){
        double ct = (distbeta/.0050)*beta*beta;
        //see [CLAS-Note 96-008]
        double tBeta = (0.5 *ct*ct*ct*t)/(ct*ct*ct+t*t*t);
        return tBeta;
    }
    
    public double getReducedAngle(double alpha) {
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
