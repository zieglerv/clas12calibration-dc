/*
 *   @author m.c.kunkel
 *   @author KPAdhikari		
 *   @author ziegler
 */
package org.clas.detector.clas12calibration.dc.fit;

import static org.clas.detector.clas12calibration.dc.constants.Constants.wpdist;
import org.clas.detector.clas12calibration.dc.io.ReadT2DparsFromCCDB;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.timetodistance.T2DFunctions;

public class DCTimeFunction
{

    public static synchronized double computeCalibTime(int sector, int superlayer, float doca, int tdc, float tProp, 
            float tFlight, float tStart, float T0, float beta) {
        //double x = (double) doca;
        //double distbeta = TableLoader.distbetaValues[superlayer-1];
        //double tBeta = (Math.sqrt(x * x + (distbeta * beta * beta) * (distbeta* beta * beta)) - x) / Constants.V0AVERAGED;
        double time = (double) tdc - (double) tProp - (double) tFlight - (double) tStart - (double) T0 ;
        //- tBeta;
        //double db = ReadT2DparsFromCCDB.parsFromCCDB[sector-1][superlayer-1][3];
        double delt = 0.08/Constants.AVEDRIFTVEL; 
        //see [CLAS-Note 96-008]
        double tBeta = (0.5 *delt*delt*delt*time)/(delt*delt*delt+time*time*time)*beta*beta;
        
        return time - tBeta;
    }
	private int iSuperlayer;
	private double alpha;
	private double[] par;
	private double bfield;

	public DCTimeFunction(int iSL, double reducedAngle, double[] par)
	{
		this.iSuperlayer = iSL;
		this.alpha = reducedAngle;
		this.par = par;
		this.bfield = 0.0;
	}

	public DCTimeFunction(int iSL, double reducedAngle, double bfield, double[] par)
	{
		this.iSuperlayer = iSL;
		this.alpha = reducedAngle; 
		this.par = par;
		this.bfield = bfield;
	}

        public double polynFit(double x)
	{
		double dmax = 2 * wpdist[iSuperlayer];
		double v_0 = par[0];
		double vm = par[1];
		double tmax = par[2];
		double distbeta = par[3]; // 8/3/16: initial value given by Mac is 0.050 cm.
		// Now the B-field parameters (applicable only to SL=3 & 4 i.e region-2)
		double delBf = par[4]; // =0.0;
		double Bb1 = par[5]; // =0.0;
		double Bb2 = par[6]; // =0.0;
                double Bb3 = par[7]; // =0.0;
		double Bb4 = par[8]; // =0.0;
		double R = par[9];
                
                double deltatime_beta = (Math.sqrt(x * x + (distbeta * beta * beta) * (distbeta* beta * beta)) - x) / Constants.V0AVERAGED;
               
                double calcTime = T2DFunctions.polyFcnMac(x,  alpha,  bfield,  v_0,  vm,  R, 
                    tmax,  dmax,  delBf,  Bb1,  Bb2,  Bb3,  Bb4, iSuperlayer+1) + deltatime_beta;

		return calcTime;
                
	}
        public double polynFit2(double x)
	{
		double dmax = 2 * wpdist[iSuperlayer];
		double v_0 = par[0];
		double vm = par[1];
		double tmax = par[2];
		double distbeta = par[3]; // 8/3/16: initial value given by Mac is 0.050 cm.
		// Now the B-field parameters (applicable only to SL=3 & 4 i.e region-2)
		double delBf = par[4]; // =0.0;
		double Bb1 = par[5]; // =0.0;
		double Bb2 = par[6]; // =0.0;
		double Bb3 = par[7]; // =0.0;
		double Bb4 = par[8]; // =0.0;
		double R = par[9];
                
                double deltatime_beta =(Math.sqrt(x * x + (distbeta * beta * beta) * (distbeta* beta * beta)) - x) / Constants.V0AVERAGED;
               
                double calcTime = T2DFunctions.polyFcnMac(x,  alpha,  bfield,  v_0,  vm,  R, 
                    tmax,  dmax,  delBf,  Bb1,  Bb2,  Bb3,  Bb4, iSuperlayer+1) + deltatime_beta;

		return calcTime;
                
	}
        
        public double beta = 1;
}
