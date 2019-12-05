/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import static org.clas.detector.clas12calibration.dc.constants.Constants.iSecMax;
import static org.clas.detector.clas12calibration.dc.constants.Constants.iSecMin;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSL;
import static org.clas.detector.clas12calibration.dc.constants.Constants.wpdist;
import org.clas.detector.clas12calibration.dc.fit.DCFitDrawerForXDoca;
import org.clas.detector.clas12calibration.dc.fit.DCFitFunction;
import org.clas.detector.clas12calibration.dc.init.Coordinate;

import org.jlab.rec.dc.timetodistance.TableLoader;
import static org.jlab.rec.dc.timetodistance.TableLoader.AlphaBounds;
import static org.jlab.rec.dc.timetodistance.TableLoader.BfieldValues;

import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.hipo.HipoDataSource;


/**
 *
 * @author ziegler
 */
public class FillHistograms {
    public Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>(); 																			// SliceViewer
    public Map<Coordinate, H2F> h2timeVcalcDoca = new HashMap<Coordinate, H2F>();
    private Map<Coordinate, H1F> h1timeFitRes = new HashMap<Coordinate, H1F>(); 
    private Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();

    private Map<Integer, Integer> layerMapTBHits;
	private Map<Integer, Integer> wireMapTBHits;
	private Map<Integer, Double> timeMapTBHits;
	private Map<Integer, Double> TPropMapTBHits;
	private Map<Integer, Double> TFlightMapTBHits;
	private Map<Integer, Double> trkDocaMapTBHits;
	private Map<Integer, Double> calcDocaMapTBHits;
	private Map<Integer, Double> timeResMapTBHits;
	private Map<Integer, Double> BMapTBHits;
        private Map<Integer, Double> AlphaMapTBHits;
	private Map<Integer, Double> BetaMapTBHits;
	
	private H1F h1bField;
	private H1F[] h1bFieldSL = new H1F[nSL];
	private H1F[] h1LocalAngleSL = new H1F[nSL];
	private H2F h2ResidualVsTrkDoca;
	private H1F h1trkDoca4NegRes, h1trkDoca4PosRes;
	private Map<Coordinate, H1F> h1timeRes = new HashMap<Coordinate, H1F>(); // Time Residual Plot : The main result
	private Map<Coordinate, H2F> h2timeResVsTrkDoca = new HashMap<Coordinate, H2F>();
        double timeAxisMax[] = { 400.0, 400.0, 850.0, 850.0, 900.0, 950.0 };
    
    public void init() {   
        TableLoader.FillAlpha();
        double dMax;
        String hNm;
        String hTtl;
        double BMaxBinIdx = 4;
    
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < nSL+1; j++)
            {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < 6; k++)
                {
                    
                    if(k==2 || k==3){
                    //B-field histograms
                        for (int l = 0; l < BMaxBinIdx; l++)
                        {
                            hNm = String.format("Sector %d timeVtrkDocaS%dTh%02dB%d", i, j, k, l);
                            h2timeVtrkDoca.put(new Coordinate(i, j, k, l),
                                            new H2F(hNm, 200, 0.0, 1.5 * dMax, 150, -50, timeAxisMax[j]));
                            hTtl = String.format("time - fit (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, AlphaBounds[k][0],
                                    AlphaBounds[k][1]);
                            if(l < BfieldValues.length-1) {
                                hTtl += String.format(" %2.1f < B < %2.1f", BfieldValues[l], BfieldValues[l+1]);
                            } else {
                                hTtl += String.format(" B >= %2.1f", BfieldValues[l]);
                            }
                            h2timeVtrkDoca.get(new Coordinate(i, j, k, l)).setTitle(hTtl);
                            h2timeVtrkDoca.get(new Coordinate(i, j, k, l)).setTitleX("trkDoca (cm)");
                            h2timeVtrkDoca.get(new Coordinate(i, j, k, l)).setTitleY("Time (ns)");
                        }
                    } else {
                    
                        hTtl = String.format("time vs. |normDoca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, AlphaBounds[k][0],
                                        AlphaBounds[k][1]);

                        hNm = String.format("Sector %d timeVtrkDocaS%dTh%02d", i, j, k);
                        h2timeVtrkDoca.put(new Coordinate(i, j, k),
                                        new H2F(hNm, 200, 0.0, 1.5 * dMax, 150, -50, timeAxisMax[j]));

                        hTtl = String.format("time vs. |trkDoca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, AlphaBounds[k][0],
                                        AlphaBounds[k][1]);
                        h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                        h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitleX("trkDoca (cm)");
                        h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                        hNm = String.format("Sector %d timeVcalcDocaS%dTh%02d", i, j, k);
                        h2timeVcalcDoca.put(new Coordinate(i, j, k),
                                        new H2F(hNm, 200, 0.0, 1.5 * dMax, 150, -50, timeAxisMax[j]));

                        hTtl = String.format("time vs. |calcDoca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, AlphaBounds[k][0],
                                        AlphaBounds[k][1]);
                        h2timeVcalcDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                        h2timeVcalcDoca.get(new Coordinate(i, j, k)).setTitleX("|calcDoca| (cm)");
                        h2timeVcalcDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                        hNm = String.format("Sector %d timeFitResS%dTh%02d", i, j, k);
                        h1timeFitRes.put(new Coordinate(i, j, k),
                                        new H1F(hNm, 150, -timeAxisMax[j] / 2, timeAxisMax[j] / 2));
                        hTtl = String.format("time - fit (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, AlphaBounds[k][0],
                                        AlphaBounds[k][1]);
                        h1timeFitRes.get(new Coordinate(i, j, k)).setTitle(hTtl);
                        h1timeFitRes.get(new Coordinate(i, j, k)).setTitleX("Time (ns)");

                    }
                }
            }
        }
    }

    DataBank bnkHits = null;
    DataBank segsMatch = null;
    
    private boolean selectElectrons(DataBank bnkHits, int j) {
        if((double) bnkHits.getFloat("beta", j) <0.99 || (double) bnkHits.getFloat("beta", j)>1.01) {
            return true;
        } else {
            return false;
        }
    }
    private void processTBhits(DataEvent event){
		double bFieldVal = 0.0;
		int sector = -1;
                int superlayer = -1;
		layerMapTBHits = new HashMap<Integer, Integer>();
		wireMapTBHits = new HashMap<Integer, Integer>();
		timeMapTBHits = new HashMap<Integer, Double>();
		trkDocaMapTBHits = new HashMap<Integer, Double>();
		calcDocaMapTBHits = new HashMap<Integer, Double>();
		timeResMapTBHits = new HashMap<Integer, Double>();
		BMapTBHits = new HashMap<Integer, Double>();
                AlphaMapTBHits = new HashMap<Integer, Double>();
		BetaMapTBHits = new HashMap<Integer, Double>();
		TFlightMapTBHits = new HashMap<Integer, Double>();
		TPropMapTBHits = new HashMap<Integer, Double>();
                
		bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
                segsMatch = (DataBank) event.getBank("TimeBasedTrkg::TBSegments");
                
                if(bnkHits==null || segsMatch==null)
                    return;
                
                for (int j = 0; j < segsMatch.rows(); j++){
                    AlphaMapTBHits.put(segsMatch.getInt("Cluster_ID", j), 
                            (double) SegLocAngle(segsMatch.getFloat("fitSlope", j)));
                }
                
                
		for (int j = 0; j < bnkHits.rows(); j++){
                    if(bnkHits.getFloat("TProp", j)==0 || bnkHits.getFloat("TFlight", j)==0 )
                        continue; 
                        // select electrons
                        if(this.selectElectrons(bnkHits, j)==false)
                            continue;
			layerMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("layer", j));
			wireMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("wire", j));
                        double calibTime = (double) (bnkHits.getInt("TDC", j) - bnkHits.getFloat("TProp", j)
					- bnkHits.getFloat("TFlight", j) - bnkHits.getFloat("TStart", j) 
                                        - bnkHits.getFloat("T0", j));
			timeMapTBHits.put(bnkHits.getInt("id", j), calibTime); // Do not subtract Tbeta here
			trkDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("trkDoca", j));
			calcDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("doca", j));
			timeResMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("timeResidual", j));
			TFlightMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("TFlight", j));
			TPropMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("TProp", j));
			bFieldVal = (double) bnkHits.getFloat("B", j);
			sector = bnkHits.getInt("sector", j); sector = 1;
			superlayer = bnkHits.getInt("superlayer", j);
			BMapTBHits.put(bnkHits.getInt("id", j), bFieldVal);
                        
                        double Alph = (double) this.getReducedAngle(AlphaMapTBHits.get(bnkHits.getInt("clusterID", j)));
                       
			BetaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("beta", j));
                        h1LocalAngleSL[superlayer - 1].fill(Alph);
			h1bFieldSL[superlayer - 1].fill(bFieldVal);
			if (superlayer == 3 || superlayer == 4)
			{
				h1bField.fill(bFieldVal); // for a quick look
			}
                        
                        int tbinIdx = TableLoader.getAlphaBin(Alph);
                        
                        
                        h2timeVcalcDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("doca", j)), calibTime);
                        
                        h1timeSlTh.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx)).fill(calibTime);
                        // Following two for all angle-bins combined (but for individual
                        // superlayers in each sector)
                        //h1timeRes.get(new Coordinate(sector - 1, superlayer - 1)).fill((double) bnkHits.getFloat("timeResidual", j));
                        h1timeRes.get(new Coordinate(sector - 1, superlayer - 1)).fill((double) bnkHits.getFloat("fitResidual", j));
                        h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), (double) bnkHits.getFloat("timeResidual", j));
                        // Following two for individual angular bins as well.
                        //h1timeRes.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx)).fill((double) bnkHits.getFloat("timeResidual", j));
                        h1timeRes.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx)).fill((double) bnkHits.getFloat("fitResidual", j));
                        h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), (double) bnkHits.getFloat("timeResidual", j));
                        h2ResidualVsTrkDoca.fill((double) bnkHits.getFloat("trkDoca", j), (double) bnkHits.getFloat("timeResidual", j));
                        
                        if ((double) bnkHits.getFloat("timeResidual", j) > 0.0)
                        {
                            h1trkDoca4PosRes.fill((double) bnkHits.getFloat("trkDoca", j));
                        }
                        else
                        {
                            h1trkDoca4NegRes.fill((double) bnkHits.getFloat("trkDoca", j));
                        }    
                        
                        double trkDoca = Math.abs((double) bnkHits.getFloat("trkDoca", j));
                        this.FillTvsDocaHistos(h2timeVtrkDoca, sector, superlayer, calibTime, trkDoca, Alph, bFieldVal);
                        
                        
                }
    }
    
    public double SegLocAngle(double clusterLineFitSlope) {
        double cosTrkAngle = 1. / Math.sqrt(1. + clusterLineFitSlope * clusterLineFitSlope);    
        return Math.toDegrees(Math.acos(cosTrkAngle));
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

    private void FillTvsDocaHistos(Map<Coordinate, H2F> h2timeVtrkDoca, int sector, int superlayer, double calibTime, double trkDoca, double Alph, double bFieldVal) {
        int tbinIdx = TableLoader.getAlphaBin(Alph);
        if (superlayer == 3 || superlayer == 4)
        {
            int maxBinIdxB = TableLoader.BfieldValues.length-1;
            DecimalFormat df = new DecimalFormat("#");
            df.setRoundingMode(RoundingMode.CEILING);

            int bbinIdx =0;
            try{
                bbinIdx = Integer.parseInt(df.format(bFieldVal*bFieldVal) ) -1; 
            } catch (NumberFormatException e) {
                System.out.println(" field bin error "+bFieldVal+" ");
            }
            if(bbinIdx<0) {
                bbinIdx = 0;
            }
            if(bbinIdx>maxBinIdxB)
                bbinIdx = maxBinIdxB;
            try {
                h2timeVtrkDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx, bbinIdx))
                        .fill(trkDoca, calibTime);

            } catch (Exception e) {
            }
        } else {
            try {
                h2timeVtrkDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx))
                        .fill(trkDoca, calibTime);
            } catch (Exception e) {
            }
        }
        
        
    }
}
