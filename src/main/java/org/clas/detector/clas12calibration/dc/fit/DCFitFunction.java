/**
 *  @author m.c.kunkel, kpadhikari, Latif Kabir, ziegler
 *  based of the KrishnaFcn.java
 */
package org.clas.detector.clas12calibration.dc.fit;

import static org.clas.detector.clas12calibration.dc.constants.Constants.wpdist;

import java.util.HashMap;
import java.util.Map;

import org.freehep.math.minuit.FCNBase;
import org.clas.detector.clas12calibration.dc.init.Coordinate;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.rec.dc.timetodistance.TableLoader;

public class DCFitFunction implements FCNBase
{
	private GraphErrors profileX;
	private int iSec;
	private int iSL;
	private double alpha;
        private double bField;
	private boolean isPolynomialFcn;
	private int meanErrorType = 2; // 0: RMS, 1=RMS/sqrt(N), 2 = 1.0 (giving equal weight to all
									// profile means)
	private double docaNormMin = 0.0, docaNormMax = 0.9;
	private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
	double dMax;// = 2 * wpdist[superlayer];
	boolean[] selectedAngleBins = new boolean[6];

	private DCTimeFunction timeFunc;

	
	// ------------------------ In Use ---------------------------------
	public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDoca, int iSector, int iSuperlayer,
			int meanErrorType, double docaNormMin, double docaNormMax, 
                        boolean fc, boolean[] selectedAngleBins)
	{
		this.h2timeVtrkDoca = h2timeVtrkDoca;
		this.iSec = iSector;
		this.iSL = iSuperlayer;
		this.isPolynomialFcn = fc;
		this.meanErrorType = meanErrorType;
		this.docaNormMin = docaNormMin;
		this.docaNormMax = docaNormMax;
                
		dMax = 2 * wpdist[iSuperlayer]; 
		this.selectedAngleBins = selectedAngleBins;

		System.out.println("Inside DCFitFunction constructor ...");
		for (int i = 0; i < selectedAngleBins.length; i++)
		{
			System.out.println(i + " " + selectedAngleBins[i]);
		}
	}


	@Override
	public double valueOf(double[] par)
	{
		double chisq = 0.0;
		chisq = getChisq(par);
		return chisq;
	}

        public double getBFieldDepChi2(double par[]){
            double chisq = 0;
            double delta = 0;
            H1F h1;
            for (int th = 0; th < 6; th++) {
                if (selectedAngleBins[th] == false)
                    continue;
                for(int bb = 0; bb < 4; bb++) {
                    h1 = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th, bb)).projectionX();
                    int nBinsX = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th, bb)).getXAxis().getNBins();
                    int nBinsY = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th, bb)).getYAxis().getNBins();
                    timeFunc = new DCTimeFunction(iSL, TableLoader.AlphaMid[th], TableLoader.BfieldValues[bb], par);
                            
                    for (int i = 0; i < nBinsX; i++){
                        for (int j = 0; j < nBinsY; j++){
                            double x = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th, bb)).getDataX(i) ;
                            double measTime = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th, bb)).getDataY(j);// profileX.getDataY(i); //jth y-Bin
                            double binContent = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th, bb)).getData(i, j);// getData(i,j) and
                            if(binContent < 10)
                                continue; 
                            
                            double  measTimeErr = h1.getDataEY(j);
                            if (measTimeErr<10)
                                    measTimeErr=10;
                            double calcTime = isPolynomialFcn ? timeFunc.polynFit(x) : timeFunc.polynFit2(x);
                            delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                            chisq += delta * delta;
                        }
                    }
                }
            }
            return chisq;
        }
        public double getNonBFieldDepChi2(double par[]){
            double chisq = 0;
            double delta = 0;
            H1F h1;
            for (int th = 0; th < 6; th++) {
                if (selectedAngleBins[th] == false)
                    continue;
                timeFunc = new DCTimeFunction(iSL, TableLoader.AlphaMid[th], par);
                h1 = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th)).projectionX();
                
                int nBinsX = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th)).getXAxis().getNBins();
                int nBinsY = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th)).getYAxis().getNBins();
                for (int i = 0; i < nBinsX; i++){
                    for (int j = 0; j < nBinsY; j++){
                        double x = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th)).getDataX(i) ;// x-Bin center
                        double measTime = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th)).getDataY(j);// profileX.getDataY(i); //jth y-Bin
                        double binContent = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, th)).getData(i, j);// getData(i,j) and
                        if(binContent < 10)
                            continue; 
                        double  measTimeErr = h1.getDataEY(j);
                        
                        double calcTime = isPolynomialFcn ? timeFunc.polynFit(x) : timeFunc.polynFit2(x);
                        delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                        chisq += delta * delta;
                    }
                }
            }
            return chisq;
        }
	
	// --------------- In Use ------------------------------
	public double getChisq(double par[])
	{
            double chisq = 0;
            if(iSL == 2 || iSL==3) {
                chisq = this.getBFieldDepChi2(par);
            } else {
                chisq = this.getNonBFieldDepChi2(par);
            }
            return chisq;
	}

	
}
