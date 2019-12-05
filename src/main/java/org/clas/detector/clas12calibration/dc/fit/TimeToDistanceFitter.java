/*              
 * 		@author KPAdhikari
 *              @author m.c.kunkel
 *              @author ziegler    
 */
package org.clas.detector.clas12calibration.dc.fit;

import static org.clas.detector.clas12calibration.dc.constants.Constants.histTypeToUseInFitting;
import static org.clas.detector.clas12calibration.dc.constants.Constants.iSecMax;
import static org.clas.detector.clas12calibration.dc.constants.Constants.iSecMin;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nLayer;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSL;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSectors;
import static org.clas.detector.clas12calibration.dc.constants.Constants.outFileForFitPars;
import static org.clas.detector.clas12calibration.dc.constants.Constants.parName;
import static org.clas.detector.clas12calibration.dc.constants.Constants.wpdist;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnUserParameters;

import org.clas.detector.clas12calibration.dc.constants.Constants;
import org.clas.detector.clas12calibration.dc.init.Coordinate;
import org.clas.detector.clas12calibration.dc.io.FileOutputWriter;
import org.clas.detector.clas12calibration.dc.ui.DCTabbedPane;
import org.clas.detector.clas12calibration.dc.ui.FitControlUI;
import org.clas.detector.clas12calibration.dc.ui.OrderOfAction;
import org.clas.detector.clas12calibration.dc.ui.SliceViewer;
import org.jlab.clas.swimtools.Swimmer;
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
import org.jlab.rec.dc.timetodistance.TableLoader;
import static org.jlab.rec.dc.timetodistance.TableLoader.AlphaBounds;
import static org.jlab.rec.dc.timetodistance.TableLoader.AlphaMid;
import static org.jlab.rec.dc.timetodistance.TableLoader.BfieldValues;

public class TimeToDistanceFitter implements ActionListener, Runnable
{
	// ------------------ step# : Declare all data field and histograms -------------------------------------
	private DataBank bnkHits;
        private DataBank segsMatch;
	private int colIndivFit = 1, colSimulFit = 4;
	int[][][] segmentIDs; // [nTrks][3][2] //3 for crosses per track, 2 for segms per cross.
	double[][][] trkChi2;// Size equals the # of tracks for the event
	int nTracks = 0;

	private Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1ThSL = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();
	public Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>(); // Time Residual vs trkDoca
																					// SliceViewer
	public Map<Coordinate, H2F> h2timeVcalcDoca = new HashMap<Coordinate, H2F>();// made it public -
																					// SliceViewer
//	private Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> h2timeFitResVtrkDoca = new HashMap<Coordinate, H2F>();// time -
																						// fitLine
	private Map<Coordinate, H1F> h1timeFitRes = new HashMap<Coordinate, H1F>(); // time - fitLine

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
	private Map<Coordinate, DCFitFunction> mapOfFitFunctions = new HashMap<Coordinate, DCFitFunction>(); // Map fo Fit functions
	private Map<Coordinate, MnUserParameters> mapOfFitParameters = new HashMap<Coordinate, MnUserParameters>(); // Map of fit parameters
	private Map<Coordinate, double[]> mapOfUserFitParameters = new HashMap<Coordinate, double[]>();
	private Map<Coordinate, DCFitDrawerForXDoca> mapOfFitLinesX = new HashMap<Coordinate, DCFitDrawerForXDoca>();
	
	private H1F h1bField;
	private H1F[] h1bFieldSL = new H1F[nSL];
	private H1F[] h1LocalAngleSL = new H1F[nSL];
	//private H1F h1fitChisqProb, h1fitChi2Trk, h1fitChi2Trk2, h1ndfTrk, h1zVtx;
	private H2F testHist, h2ResidualVsTrkDoca;
	private H1F h1trkDoca4NegRes, h1trkDoca4PosRes;// Temp, 4/27/17
	private Map<Coordinate, H1F> h1timeRes = new HashMap<Coordinate, H1F>(); // Time Residual Plot : The main result
	private Map<Coordinate, H1F> h1fitRes = new HashMap<Coordinate, H1F>(); // Time Residual Plot : The main result
	
        private Map<Coordinate, H2F> h2timeResVsTrkDoca = new HashMap<Coordinate, H2F>();

	private GraphErrors[] vertLineDmax = new GraphErrors[nSL]; // Vertical line to show boundary of Dmax
	private GraphErrors[] vertLineDmaxCos30 = new GraphErrors[nSL]; // Vertical line to show boundary of Dmax x cos(30)
	private GraphErrors[][] vertLineDmaxCosTh = new GraphErrors[nSL][6]; // Vertical line to show boundary of Dmax x cos(theta)

	private boolean acceptorder = false;
	private boolean isPolynomialFit;

	private ArrayList<String> fileArray;
	private EvioDataChain reader;
	private HipoDataSource readerH;
	private OrderOfAction OAInstance;
	private DCTabbedPane dcTabbedPane;
        private double BMax = 4; //Max B bin
	double[] tupleVars;
	public static int runNumber = 0;
	private boolean runNumberFound = false;
	public double[] fPars = new double[10];
	public double[] fErrs = new double[10];
        
	// ----------------------- step# : Constructor --------------------------------------
	public TimeToDistanceFitter(ArrayList<String> files, boolean isPolynFit)
	{
                
		this.fileArray = files;
		this.reader = new EvioDataChain();
		this.readerH = new HipoDataSource();
		this.dcTabbedPane = new DCTabbedPane("DC Calibration");
		this.isPolynomialFit = isPolynFit;
		this.tupleVars = new double[5];

		createVerticalLinesForDMax();
		createHists();

		System.out.println("Calibration Configuration before data processing: Sector - " + iSecMax + " HistType- "
				+ histTypeToUseInFitting);
	}

	public TimeToDistanceFitter(OrderOfAction OAInstance, ArrayList<String> files, boolean isPolynFit)
	{
                
		this.fileArray = files;
		this.OAInstance = OAInstance;
		this.reader = new EvioDataChain();
		this.readerH = new HipoDataSource();
		this.dcTabbedPane = new DCTabbedPane("DC Calibration");
		this.tupleVars = new double[5];
		this.isPolynomialFit = isPolynFit;
                System.out.println(" Running T2D.................................................");
		createVerticalLinesForDMax();
		createHists();

		System.out.println("Calibration Configuration before data processing: Sector - " + iSecMax + " HistType- "
				+ histTypeToUseInFitting);
	}

	// I didn't know how to make a vertical line out of the function classes such as Func1D.
	private void createVerticalLinesForDMax()
	{
		double rad2deg = 180.0 / Math.PI;
		double cos30 = Math.cos(30.0 / rad2deg);
		double cosTh = 0.0, reducedTh = 0.0;
		for (int i = 0; i < nSL; i++)
		{
			vertLineDmax[i] = new GraphErrors();
			vertLineDmaxCos30[i] = new GraphErrors();

			// Drawing an array of points (as I didn't know how to draw the lines to join them)
			// Drawing the line as the error bar of a single point graph (with zero size for marker)
			vertLineDmax[i].addPoint(2 * wpdist[i], 50, 0, 50);
			vertLineDmaxCos30[i].addPoint(2 * wpdist[i] * cos30, 50, 0, 50);
			vertLineDmax[i].setMarkerSize(0);
			vertLineDmaxCos30[i].setMarkerSize(0);

			vertLineDmax[i].setMarkerColor(2);
			vertLineDmax[i].setLineColor(2);
			vertLineDmax[i].setLineThickness(1);

			vertLineDmaxCos30[i].setMarkerColor(2);
			vertLineDmaxCos30[i].setLineColor(2);
			vertLineDmaxCos30[i].setLineThickness(1);

			// Making more vertical lines at each dmax*cos(th) rather than dmax*cos(30)
			for (int k = 0; k < 6; k++)
			{
				double cos30minusalpha = Math.cos(Math.toRadians(30.)) + (double) (k)*(1. - Math.cos(Math.toRadians(30.)))/5.;

                                double alpha = -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);
				cosTh = Math.cos(alpha / rad2deg);
				vertLineDmaxCosTh[i][k] = new GraphErrors();
				vertLineDmaxCosTh[i][k].addPoint(2 * wpdist[i] * cosTh, 50, 0, 50);
				vertLineDmaxCosTh[i][k].setMarkerColor(1);
				vertLineDmaxCosTh[i][k].setMarkerSize(0);
				vertLineDmaxCosTh[i][k].setLineColor(1);
				vertLineDmaxCosTh[i][k].setLineThickness(1);
			}
		}
	}

        
	// -------------------- step# : Instantiate all the histograms declared and assign to corresponding S, SL, variables -------------------------------
	private void createHists()
	{
            TableLoader.FillAlpha();
                
                double timeAxisMax[] = { 400.0, 400.0, 850.0, 850.0, 900.0, 950.0 };
                
		initializeBFieldHistograms();

		
		h1trkDoca4NegRes = new H1F("trkDoca4NegRes", 200, -2.0, 2.0);
		h1trkDoca4NegRes.setTitle("trkDoca for negative residual");
		h1trkDoca4PosRes = new H1F("trkDoca4PosRes", 200, -2.0, 2.0);
		h1trkDoca4PosRes.setTitle("trkDoca for positive residual");
		h2ResidualVsTrkDoca = new H2F("ResidualVsTrkDoca", 200, -2.0, 2.0, 100, -0.5, 0.5);
		h2ResidualVsTrkDoca.setTitle("residual vs trkDoca");
		h2ResidualVsTrkDoca.setTitleY("residual [cm]");

		testHist = new H2F("A test of superlayer6 at thetabin6", 200, 0.0, 1.0, 150, 0.0, 200.0);

//		TStyle.createAttributes();
		String hNm = "";
		String hTtl = "";
		for (int i = 0; i < nSL; i++)
		{
			for (int j = 0; j < nLayer; j++)
			{
				for (int k = 0; k < 8; k++)
				{
					hNm = String.format("wireS%dL%dDb%02d", i + 1, j + 1, k);
					hArrWire.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.0, 119.0));
					hTtl = String.format("wire (SL=%d, Layer%d, DocaBin=%02d)", i + 1, j + 1, k);
					hArrWire.get(new Coordinate(i, j, k)).setTitleX(hTtl);
					hArrWire.get(new Coordinate(i, j, k)).setLineColor(i + 1);
				}
			}
		}
		for (int i = 0; i < nSL; i++)
		{
			hNm = String.format("thetaSL%d", i + 1);
			hTtl = "#theta";
			h1ThSL.put(new Coordinate(i), new H1F(hNm, 120, -60.0, 60.0));
			h1ThSL.get(new Coordinate(i)).setTitle(hTtl);
			h1ThSL.get(new Coordinate(0)).setLineColor(i + 1);
		}
		for (int i = iSecMin; i < iSecMax; i++)
		{
			
			for (int j = 0; j < nSL; j++)
			{
				for (int k = 0; k < 6; k++)
				{
					hNm = String.format("timeS%dSL%dThBn%d", i, j, k);
					h1timeSlTh.put(new Coordinate(i, j, k), new H1F(hNm, 200, -50, 190.0)); // -10.0,
                                        
					hTtl = String.format("time (S=%d, SL=%d, th(%.1f,%.1f)", i + 1, j + 1, AlphaBounds[k][0], AlphaBounds[k][1]);
					h1timeSlTh.get(new Coordinate(i, j, k)).setTitleX(hTtl);
					h1timeSlTh.get(new Coordinate(i, j, k)).setLineColor(j + 1);
				}
			}
		}

		String[] hType =
		{ "all hits", "matchedHitID==-1", "Ratio==Ineff." };// as
		
		double dMax;
		for (int i = 0; i < 6+1; i++)
		{
			for (int j = 0; j < nSL; j++)
			{
				dMax = 2 * wpdist[j];
				for (int k = 0; k < 6; k++)
				{

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

					hNm = String.format("Sector %d timeFitResVtrkDocaS%dTh%02d", i, j, k);
					h2timeFitResVtrkDoca.put(new Coordinate(i, j, k),
							new H2F(hNm, 200, 0.0, 1.5 * dMax, 150, -timeAxisMax[j] / 2, timeAxisMax[j] / 2));
					hTtl = String.format("time - fit vs. |Doca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1,
							AlphaBounds[k][0], AlphaBounds[k][1]);
					h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitleX("|Doca| (cm)");
					h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

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

		// ------------ Include B-field binnings -------------
		for (int i = 0; i < 6+1; i++)
		{
			for (int j = 2; j <= 3; j++)
			{
				dMax = 2 * wpdist[j];
				for (int k = 0; k < 6; k++)
				{
					for (int l = 0; l < BMax; l++)
					{
						hNm = String.format("Sector %d timeVtrkDocaS%dTh%02dB%d", i, j, k, l);
						h2timeVtrkDoca.put(new Coordinate(i, j, k, l),
								new H2F(hNm, 200, 0.0, 1.5 * dMax, 150, -50, timeAxisMax[j]));
                                                if(l < BfieldValues.length-1) {
                                                    hTtl = String.format("%2.1f < B < %2.1f", BfieldValues[l], BfieldValues[l+1]);
                                                } else {
                                                    hTtl = String.format("B >= %2.1f", BfieldValues[l]);
                                                }
						h2timeVtrkDoca.get(new Coordinate(i, j, k, l)).setTitle(hTtl);
						h2timeVtrkDoca.get(new Coordinate(i, j, k, l)).setTitleX("trkDoca (cm)");
						h2timeVtrkDoca.get(new Coordinate(i, j, k, l)).setTitleY("Time (ns)");
					}
				}
			}
		}

		for (int i = iSecMin; i < iSecMax+1; i++)
		{
			for (int j = 0; j < nSL; j++)
			{
				dMax = 2 * wpdist[j];

				// Following is used for all angle-bins combined
				hNm = String.format("ResS%dSL%d", (i + 1), (j + 1));
				h1timeRes.put(new Coordinate(i, j), new H1F(hNm, 200, -1.0, 1.0));
                                h1fitRes.put(new Coordinate(i, j), new H1F(hNm, 200, -1.0, 1.0));
				hTtl = String.format("residual (cm) (Sec=%d, SL=%d)", i + 1, j + 1);
				h1timeRes.get(new Coordinate(i, j)).setTitle(hTtl);
				h1timeRes.get(new Coordinate(i, j)).setTitleX("residual [cm]");
                                h1fitRes.get(new Coordinate(i, j)).setTitle(hTtl);
				h1fitRes.get(new Coordinate(i, j)).setTitleX("fit residual [cm]");

				// h2timeResVsTrkDoca
				hNm = String.format("timeResVsTrkDocaS%dSL%d", i, j);
				h2timeResVsTrkDoca.put(new Coordinate(i, j), new H2F(hNm, 200, 0.0, 1.2 * dMax, 200, -1.0, 1.0));
				hTtl = String.format("residual (cm) (Sec=%d, SL=%d)", i, j + 1);
				h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitle(hTtl);
				h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitleX("|trkDoca| [cm]");
				h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitleY("residual [cm]");

				// Following is used for individual angle bins
				for (int k = 0; k < 6; k++)
				{
					hNm = String.format("timeResS%dSL%dTh%02d", i, j, k);
					h1timeRes.put(new Coordinate(i, j, k), new H1F(hNm, 200, -1.0, 1.0));
					hTtl = String.format(" Sec=%d, SL=%d, Th(%2.1f,%2.1f)", i, j + 1, AlphaBounds[k][0], AlphaBounds[k][1]);
					h1timeRes.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h1timeRes.get(new Coordinate(i, j, k)).setTitleX("residual [cm]");
                                        hNm = String.format("fitResS%dSL%dTh%02d", i, j, k);
					h1fitRes.put(new Coordinate(i, j, k), new H1F(hNm, 200, -1.0, 1.0));
					hTtl = String.format(" Sec=%d, SL=%d, Th(%2.1f,%2.1f)", i, j + 1, AlphaBounds[k][0], AlphaBounds[k][1]);
					h1fitRes.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h1fitRes.get(new Coordinate(i, j, k)).setTitleX("fit residual [cm]");

					// h2timeResVsTrkDoca
					hNm = String.format("timeResVsTrkDocaS%dSL%d", i, j, k);
					h2timeResVsTrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2 * dMax, 200, -1.0, 1.0));
					hTtl = String.format("Sec=%d, SL=%d, Th(%2.1f,%2.1f)", i, j + 1, AlphaBounds[k][0], AlphaBounds[k][1]);
					h2timeResVsTrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h2timeResVsTrkDoca.get(new Coordinate(i, j, k)).setTitleX("|trkDoca| [cm]");
					h2timeResVsTrkDoca.get(new Coordinate(i, j, k)).setTitleY("residual [cm]");
				}
			}
		}
	}

	// -------------------------- step# : Instantiate B-field histograms ----------------------------------
	private void initializeBFieldHistograms()
	{
		// Overall for SL=3 & 4
                int nBFieldBins = BfieldValues.length;
                double bFieldMin = BfieldValues[0];
                double bFieldMax = BfieldValues[BfieldValues.length-1];
		h1bField = new H1F("Bfield", nBFieldBins, bFieldMin, bFieldMax);
		h1bField.setTitle("B field");
		h1bField.setLineColor(2);

		// For each of the 6 SLs
		String hName = "", hTitle = "";
		for (int i = 0; i < nSL; i++)
		{
			String.format(hName, "BfieldSL%d", i + 1);
			h1bFieldSL[i] = new H1F(hName, 10 * nBFieldBins, bFieldMin, bFieldMax);
			String.format(hTitle, "B field for SL=%d", i + 1);
			h1bFieldSL[i].setTitle(hTitle);
			h1bFieldSL[i].setLineColor(2);

			String.format(hName, "LocalAngleSL%d", i + 1);
			h1LocalAngleSL[i] = new H1F(hName, 6, -3, 33);
			String.format(hTitle, "Local angle (alpha) for SL=%d", i + 1);
			h1LocalAngleSL[i].setTitle(hTitle);
			h1LocalAngleSL[i].setLineColor(2);
		}

	}

	// --------------------------- step# process the data: Read the files and fill all the histograms ------------
	// Sub-steps: processTBTracksAndCrosses, ProcessTBTracks
	public void processData()
	{
		int counter = 0;
		int icounter = 0;
		int ndf = -1;
		int fileCounter = 0;
		double chi2 = -1.0, Vtx0_z = -10000.0;

		for (String str : fileArray)
		{
			// Reading multiple hipo files.
			System.out.println("Ready to Open & read " + str);
			readerH.open(str);

			while (readerH.hasEvent())
			{
				icounter++;

				if (icounter % 2000 == 0)
				{
					System.out.println("Processed " + icounter + " events.");
				}

				// EvioDataEvent event = reader.getNextEvent();
				DataEvent event = readerH.getNextEvent();
				if (event == null)
					continue;

				if (!runNumberFound)
				{
					if (event.hasBank("RUN::config"))
					{
						runNumber = event.getBank("RUN::config").getInt("run", 0);
						if (runNumber != 0)
						{
							runNumberFound = true;
							System.out.println("Run number found from the data bank:" + runNumber);
						}
					}
				}

				

				if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")											// &&
						&& event.hasBank("TimeBasedTrkg::TBTracks"))
				{
						
                                    processTBhits(event);
                                }
			}
			readerH.close();
			++fileCounter;
		}
		System.out.println(
				"processed " + counter + " Events with TimeBasedTrkg::TBSegmentTrajectory entries from a total of "
						+ icounter + " events");
		saveNtuple();
	}

	public double SegLocAngle(double clusterLineFitSlope) {
            
            return Math.toDegrees(Math.atan(clusterLineFitSlope));
        }
	// ---------------------------- sub-step# : Process TB hits -----------------------------------
	private void processTBhits(DataEvent event)
	{
		double bFieldVal = 0.0;
		int sector = -1;
                int superlayer = -1;
                int polarity = (int)Math.signum(event.getBank("RUN::config").getFloat("torus",0));
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
                
                for (int j = 0; j < segsMatch.rows(); j++){
                    AlphaMapTBHits.put(segsMatch.getInt("Cluster_ID", j), (double) SegLocAngle(segsMatch.getFloat("fitSlope", j)));
                }
                
                
		for (int j = 0; j < bnkHits.rows(); j++){
                    h1timeRes.get(new Coordinate(bnkHits.getInt("sector", j) - 1, bnkHits.getInt("superlayer", j) - 1)).fill((double) bnkHits.getFloat("timeResidual", j));
                    h1fitRes.get(new Coordinate(bnkHits.getInt("sector", j) - 1, bnkHits.getInt("superlayer", j) - 1)).fill((double) bnkHits.getFloat("fitResidual", j));
                    if(bnkHits.getFloat("TProp", j)==0 || bnkHits.getFloat("TFlight", j)==0 
                        //    || (double) bnkHits.getFloat("beta", j) <0.99 || (double) bnkHits.getFloat("beta", j)>1.01
                             
                            )
                        
                        continue; // select hits with proper time
                    // new select on angles
                        //double alpha = AlphaMapTBHits.get(bnkHits.getInt("clusterID", j));
                        double alpha = bnkHits.getFloat("Alpha", j);
                        int region = (int) (bnkHits.getInt("superlayer", j) + 1) / 2;
                        //cut on region close to 0 and 30 deg.
                        if(Math.abs(alpha-30)>2 || Math.abs(alpha)>2)
                            continue;
                        
                        boolean passHit = false;
                        
                        if( ( region ==1 && alpha> -20.0 && alpha< -5.0)
                        || (region ==2 && alpha> -20.0 && alpha< 0.0)
                        || (region ==3 && alpha> -28.0 && alpha< 0.0) ) {
                            passHit = true;
                        }
                        
                        if(passHit == false )
                            continue;
                        superlayer = bnkHits.getInt("superlayer", j);
			layerMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("layer", j));
			wireMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("wire", j));
                        double calibTime = (double) (bnkHits.getInt("TDC", j) - bnkHits.getFloat("TProp", j)
					- bnkHits.getFloat("TFlight", j) - bnkHits.getFloat("TStart", j) 
                                        - bnkHits.getFloat("T0", j) -bnkHits.getFloat("tBeta", j));
                        /*double calibTime = DCTimeFunction.computeCalibTime(bnkHits.getInt("sector", j),
                                superlayer, 
                                bnkHits.getFloat("doca", j),
                                bnkHits.getInt("TDC", j) ,
                                bnkHits.getFloat("TProp", j),
				bnkHits.getFloat("TFlight", j), 
                                bnkHits.getFloat("TStart", j),
                                bnkHits.getFloat("T0", j),
                                bnkHits.getFloat("beta", j));*/
			timeMapTBHits.put(bnkHits.getInt("id", j), calibTime); 
			trkDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("trkDoca", j));
			calcDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("doca", j));
                        
			timeResMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("timeResidual", j));
			//timeResMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("fitResidual", j));
			TFlightMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("TFlight", j));
			TPropMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("TProp", j));
			bFieldVal = (double) bnkHits.getFloat("B", j);
			sector = bnkHits.getInt("sector", j);
			
			BMapTBHits.put(bnkHits.getInt("id", j), bFieldVal);
                        
                         //local angle correction
                        double theta0 = Math.toDegrees(Math.acos(1-0.02*bFieldVal));
                        
                        //double alpha = (double) this.getReducedAngle(bnkHits.getFloat("Alpha", j));                        
                        // correct alpha with theta0, the angle corresponding to the isochrone lines twist due to the electric field
                        alpha-=(double)polarity*theta0;
                        //reduce the corrected angle
                        
                        double Alph = (double) this.getReducedAngle(alpha);
                       
			BetaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("beta", j));
                        h1LocalAngleSL[superlayer - 1].fill(Alph);
                        
                        //h1ThSL.get(superlayer-1).fill(Alph);
			h1bFieldSL[superlayer - 1].fill(bFieldVal);
			if (superlayer == 3 || superlayer == 4)
			{
				h1bField.fill(bFieldVal); // for a quick look
			}
                        
                        ///
                        int tbinIdx = this.getAlphaBin(Alph);
                        h2timeVtrkDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), calibTime);
                        
                        
                        h2timeVcalcDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("doca", j)), calibTime);
                        
                        h1timeSlTh.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx)).fill(calibTime);
                        // Following two for all angle-bins combined (but for individual
                        // superlayers in each sector)
                        //h1timeRes.get(new Coordinate(sector - 1, superlayer - 1)).fill((double) bnkHits.getFloat("timeResidual", j));
                        h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), (double) bnkHits.getFloat("timeResidual", j));
                        // Following two for individual angular bins as well.
                        h1timeRes.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx)).fill((double) bnkHits.getFloat("timeResidual", j));
                        h1fitRes.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx)).fill((double) bnkHits.getFloat("fitResidual", j));
                        h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), (double) bnkHits.getFloat("timeResidual", j));
                        h2ResidualVsTrkDoca.fill((double) bnkHits.getFloat("trkDoca", j), (double) bnkHits.getFloat("timeResidual", j));
                        //-----------------------------------------
                        //Put averate over all sectors in sector 7:
                        //-----------------------------------------
                        h2timeVtrkDoca.get(new Coordinate(7 - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), calibTime);
                        
                        
                        h2timeVcalcDoca.get(new Coordinate(7 - 1, superlayer - 1, tbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("doca", j)), calibTime);
                        
                        
                        //
                        if ((double) bnkHits.getFloat("timeResidual", j) > 0.0)
                        {
                            h1trkDoca4PosRes.fill((double) bnkHits.getFloat("trkDoca", j));
                        }
                        else
                        {
                            h1trkDoca4NegRes.fill((double) bnkHits.getFloat("trkDoca", j));
                        }    
                        ///
                        // ------------- Fill histograms based on B-field bins here -----------------
                        if (superlayer == 3 || superlayer == 4)
                        {
                            int maxBinIdxB = TableLoader.BfieldValues.length-1;
                            DecimalFormat df = new DecimalFormat("#");
                            df.setRoundingMode(RoundingMode.CEILING);

                            int bbinIdx =0;
                            try{
                                bbinIdx = Integer.parseInt(df.format(bFieldVal*bFieldVal) ) -1; 
                            } catch (NumberFormatException e) {
                               
                            }
                            if(bbinIdx<0) {
                                bbinIdx = 0;
                            }
                            if(bbinIdx>maxBinIdxB)
                                bbinIdx = maxBinIdxB;
                            try {
                                //System.out.println(" Filling B bin "+bbinIdx);
                                h2timeVtrkDoca.get(new Coordinate(sector - 1, superlayer - 1, tbinIdx, bbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), calibTime);
                                h2timeVtrkDoca.get(new Coordinate(7 - 1, superlayer - 1, tbinIdx, bbinIdx))
                                        .fill(Math.abs((double) bnkHits.getFloat("trkDoca", j)), calibTime);
                               
                            } catch (Exception e) {

                            }

                        }
                        
                        
			int docaBin = (int) (((double) bnkHits.getFloat("trkDoca", j) - (-0.8)) / 0.2);
			if (bnkHits.getInt("sector", j) == 1 && (docaBin > -1 && docaBin < 8))
			{
				hArrWire.get(
						new Coordinate(bnkHits.getInt("superlayer", j) - 1, bnkHits.getInt("layer", j) - 1, docaBin))
						.fill(bnkHits.getInt("wire", j));
			}
		}
	}

	

	// --------------------------------- step# fit : The fitting part -----------------------------------------
	public void runFitterAndDrawPlots(JFrame frame,
			JTextArea textArea,
			int Sec,
			int SL,
			int xMeanErrorType,
			double xNormLow,
			double xNormHigh,
			boolean[] fixIt,
			boolean checkBoxFixAll,
			double[][] pLow,
			double[][] pInit,
			double[][] pHigh,
			double[][] pSteps,
			boolean[] selectedAngleBins)
	{
                
                System.out.println("................. RUNNING NEW FITTER......");
                
			
		System.out.println(String.format("%s %d %d %d %2.1f %2.1f",
				"Selected values of Sector Superlayer errorType xNorm(Min,Max) are:",
				Sec, SL, xMeanErrorType, xNormLow, xNormHigh));
		int iSL = SL - 1;
		System.out.println("parLow   parInit    parHigh    FixedStatus");
		for (int i = 0; i < 10; i++)
		{
			System.out.println(String.format("%5.4f    %5.4f   %5.4f   %b", pLow[iSL][i],
					pInit[iSL][i], pHigh[iSL][i], fixIt[i]));
		}

		try
		{
			runFitterNew(textArea, Sec, SL, 
                                xMeanErrorType, xNormLow, xNormHigh, fixIt, checkBoxFixAll, pLow,
					pInit, pHigh, pSteps, selectedAngleBins);
		}
		catch (IOException ex)
		{
			Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.out.println("Called runFitterNew(Sec, SL, fixIt, pLow, pInit, pHigh);");
		createFitLinesNew(Sec, SL);
		System.out.println("Fit lines are prepared.");
		drawFitLinesNew(frame, Sec, SL);
	}

	// ---------------------------------------- step# fit : The main fitting procedure ---------------------------------
	protected void runFitterNew(
			JTextArea textArea,
			int Sec,
			int SL,
			int xMeanErrorType,
			double xNormLow,
			double xNormHigh,
			boolean[] fixIt,
			boolean checkBoxFixAll,
			double[][] pLow,
			double[][] pInit,
			double[][] pHigh,
			double[][] pSteps,
			boolean[] selectedAngleBins) throws IOException
	{

		System.out.println("Inside runFitterNew(..) ");
		boolean minimization_status = false;
		int iSec = Sec - 1, iSL = SL - 1;

		boolean append_to_file = true;// First time the same file will be opened from FitControlUI
										// (here it will be appended & closed)
		FileOutputWriter file = null;
		String str = " ", pStr = " ";
		try
		{
			file = new FileOutputWriter(outFileForFitPars, append_to_file);
			// file.Write("#Sec SL v0 deltanm tMax distbeta delta_bfield_coefficient b1 b2 b3 b4");
		}
		catch (IOException ex)
		{
			Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
		}

		int nFreePars = 10;

		double[][][] pars2write = new double[nSectors+1][nSL][10];// nFitPars = 10

		Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();

		// ---------------------- Histograms to be used for fitting ------------------------------
		mapOfFitFunctions.put(new Coordinate(iSec, iSL),
				new DCFitFunction(h2timeVtrkDoca, iSec, iSL, 
                                        xMeanErrorType, xNormLow, xNormHigh, isPolynomialFit,
						selectedAngleBins));// Using map of H2F

		mapOfFitParameters.put(new Coordinate(iSec, iSL), new MnUserParameters());

		for (int p = 0; p < nFreePars; p++)
		{
			mapOfFitParameters.get(new Coordinate(iSec, iSL)).add(parName[p], pInit[iSL][p], pSteps[iSL][p],
					pLow[iSL][p], pHigh[iSL][p]);
			// mapOfFitParameters.get(new Coordinate(iSec, iSL)).add(parName[p], pInit[iSL][p],
			// parSteps[p], pLow[iSL][p], pHigh[iSL][p]);
			if (fixIt[p] == true)
			{
				mapOfFitParameters.get(new Coordinate(iSec, iSL)).fix(p);
			}
		}

		// Following is to ensure that initial values are written as output if all parameters are
		// fixed i.e. when checkBoxFixAll == true;
		for (int p = 0; p < nFreePars; p++)
		{ // Don't delete
			fPars[p] = pInit[iSL][p];
		}

		// If all the parameters are fixed, don't run Minuit
		if (checkBoxFixAll == false)
		{
			MnMigrad migrad = new MnMigrad(mapOfFitFunctions.get(new Coordinate(iSec, iSL)),
					mapOfFitParameters.get(new Coordinate(iSec, iSL)));
                        
                        FunctionMinimum min = migrad.minimize();
                        for (int p = 0; p < nFreePars; p++) {
                            migrad.removeLimits(p);
                        }
                        migrad.minimize();
			
			// ---------------- step# fit result : Collect the fit results ------------------------------------
			mapTmpUserFitParameters.put(new Coordinate(iSec, iSL), min.userParameters());
			for (int p = 0; p < nFreePars; p++)
			{
				fPars[p] = mapTmpUserFitParameters.get(new Coordinate(iSec, iSL)).value(parName[p]);
				fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(iSec, iSL)).error(parName[p]);
			}
			minimization_status = min.isValid();
		}

		pStr = "  ";
		for (int p = 0; p < nFreePars; p++)
		{
			pars2write[iSec][iSL][p] = fPars[p];
			pStr = String.format("%s  %5.4f ", pStr, fPars[p]);
		}
		pStr = String.format("%s  %5.4f ", pStr, mapOfFitFunctions.get(new Coordinate(iSec, iSL)).valueOf(fPars));
		str = String.format("%d   %d   %s", iSec + 1, iSL + 1, pStr);

		mapOfUserFitParameters.put(new Coordinate(iSec, iSL), fPars);

		textArea.append(str + "\n"); // Show the results in the text area of fitControlUI
		
		// Printing error in fit parameter
		System.out.println("\t====================================================================");
		System.out.println("\t\t\t The fit results with error for S: " + (iSec + 1) + " SL: " + (iSL + 1));
		System.out.println("\t====================================================================");
		System.out.println("\n\t------------->Minimization Status: " + minimization_status + "<----------------\n");
		for (int p = 0; p < nFreePars; p++)
		{
			System.out.println("\t" + parName[p] + " : " + fPars[p] + " +/- " + fErrs[p]);
		}
		System.out.println("\n\tChi Square : " + mapOfFitFunctions.get(new Coordinate(iSec, iSL)).valueOf(fPars));

		System.out.println("\t====================================================================");
		System.out.println("\t\t\t Note the beta is currently set to: " + Constants.beta);
		System.out.println("\t====================================================================");

		// If the fit is invalid don't update fit values
		if (!minimization_status)
		{
			for (int p = 0; p < nFreePars; p++)
			{
				fPars[p] = pInit[iSL][p];
			}
		}

		System.out.println("End of runFitterNew(..) ");
	}

	// -------- In use: Generate fit lines --------------
	private void createFitLinesNew(int Sec, int SL)
	{
		int iSec = Sec - 1, iSL = SL - 1;
		
		double dMax = 2 * wpdist[iSL];
		for (int k = 0; k < 6; k++)
		{   
			String title = "timeVsNormDoca Sec=" + (iSec + 1) + " SL=" + (iSL + 1) + " Th=" + k;
			
			title = "timeVsTrkDoca Sec=" + (iSec + 1) + " SL=" + (iSL + 1) + " Th=" + k;
                        dMax = 2 * wpdist[iSL] * Math.cos(Math.toRadians(30. - AlphaMid[k]));
			

			if (iSL == 2 || iSL == 3)
			{
				for (int bField = 0; bField<BMax; bField++) {
                                    mapOfFitLinesX.put(new Coordinate(iSec, iSL, k, (int) bField),
                                                    new DCFitDrawerForXDoca(title, 0.0, dMax, iSL, k, TableLoader.BfieldValues[bField], isPolynomialFit));
                                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, (int) bField)).setLineColor(1);// (colSimulFit);//(2);
                                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, (int) bField)).setLineWidth(3);
                                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, (int) bField)).setLineStyle(3);
                                    mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, (int) bField))
                                                    .setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL)));
                                }                
				
			}
			else
			{
				mapOfFitLinesX.put(new Coordinate(iSec, iSL, k),
						new DCFitDrawerForXDoca(title, 0.0, dMax, iSL, k, isPolynomialFit));
				mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineColor(1);// (colSimulFit);//(2);
				mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineWidth(3);
				mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineStyle(3);
				mapOfFitLinesX.get(new Coordinate(iSec, iSL, k))
						.setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL)));
			}
		}
	}

	// ----------- In Use: Draw fit lines -------------
	private void drawFitLinesNew(JFrame fitControlFrame, int Sec, int SL)
	{
		int iSec = Sec - 1, iSL = SL - 1;
		int nSkippedThBins = 0; // Skipping marginal  bins 
		String Title = "";
		GraphErrors profHist;

		JTabbedPane tabbedPane = new JTabbedPane();

		EmbeddedCanvas canvas = new EmbeddedCanvas();
		canvas.setSize(3 * 400, 3 * 400);
		canvas.divide(3, 3);
		for (int k = nSkippedThBins; k < 6 - nSkippedThBins; k++)
		{
			canvas.cd(k - nSkippedThBins);
			Title = "Sec=" + Sec + " SL=" + SL
					+ " thetaBin =(" + k + ")"
					+ " indvFitCol=" + colIndivFit;
			
			//if (iSL == 2 || iSL == 3)
			//{
                        //    canvas.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k, 0)));  //  B-field
                        //    for(int b =1; b<BMax; b++) {
                        // 	canvas.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k, b)), "same");  //  B-field
                        //    }
			//}
                        //else {
				canvas.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k)));
                                //canvas.draw(h2timeVcalcDoca.get(new Coordinate(iSec, iSL, k)), "same");
                        //}
			if (iSL == 2 || iSL == 3)
			{
                            for(int b =0; b<BMax; b++)
				canvas.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, b)), "same");
			}
			else
				canvas.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");

			canvas.getPad(k - nSkippedThBins).setTitle(Title);
			canvas.setPadTitlesX("trkDoca");
			canvas.setPadTitlesY("time (ns)");
			canvas.draw(vertLineDmax[iSL], "same");
		}
		tabbedPane.add(canvas, "t vs trkDoca");

		EmbeddedCanvas canvas2 = new EmbeddedCanvas();
		canvas2.setSize(3 * 400, 3 * 400);
		canvas2.divide(3, 3);
		for (int k = nSkippedThBins; k < 6 - nSkippedThBins; k++)
		{
			canvas2.cd(k - nSkippedThBins);
			Title = "Sec=" + Sec + " SL=" + SL
					+ " thetaBin =(" + k + ")"
					+ " indvFitCol=" + colIndivFit;

			if (iSL == 2 || iSL == 3)
			{   
                                profHist = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k, 0)).getProfileX();  // Minimum B-field
				profHist.getAttributes().setLineColor(1);
				profHist.getAttributes().setMarkerColor(1);
				canvas2.draw(profHist); 
                            for(int b = 1; b< BMax; b++) {
				profHist = h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k, b)).getProfileX();  // Minimum B-field
				profHist.getAttributes().setLineColor(1);
				profHist.getAttributes().setMarkerColor(1);
				canvas2.draw(profHist, "same"); 
                            }
				
			}
			else
				canvas2.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k)).getProfileX());

			if (iSL == 2 || iSL == 3)
			{
                            for(int b =0; b<BMax; b++) {
				canvas2.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, b)), "same");
                                //mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, b)).getAttributes().setLineColor(1+b);
                            }
			}
			else
				canvas2.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
			canvas2.getPad(k - nSkippedThBins).setTitle(Title);
			canvas2.setPadTitlesX("trkDoca");
			canvas2.setPadTitlesY("time (ns)");
			canvas2.draw(vertLineDmax[iSL], "same");
		}
		tabbedPane.add(canvas2, "X-profiles & fits");

		// Now Drawing a new tab, with the fit line and the time-vs-calcDoca
		EmbeddedCanvas canvas3 = new EmbeddedCanvas();
		canvas3.setSize(3 * 400, 3 * 400);
		canvas3.divide(3, 3);
		for (int k = nSkippedThBins; k < 6 - nSkippedThBins; k++)
		{
			canvas3.cd(k - nSkippedThBins);
			Title = "Sec=" + Sec + " SL=" + SL
					+ " thetaBin =(" + k + ")"
					+ " indvFitCol=" + colIndivFit;

			canvas3.draw(h2timeVcalcDoca.get(new Coordinate(iSec, iSL, k)));
			if (iSL == 2 || iSL == 3)
                            for(int b =0; b<BMax; b++)
				canvas3.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k, b)), "same");
			else
				canvas3.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
			canvas3.getPad(k - nSkippedThBins).setTitle(Title);
			canvas3.setPadTitlesX("trkDoca [cm]");
			canvas3.setPadTitlesY("time (ns)");
			canvas3.draw(vertLineDmax[iSL], "same");
			canvas3.draw(vertLineDmaxCos30[iSL], "same");
			canvas3.draw(vertLineDmaxCosTh[iSL][k], "same");
		}
		tabbedPane.add(canvas3, "t vs calcDoca");

		JFrame frame = new JFrame();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
		frame.setLocationRelativeTo(fitControlFrame);// centered w.r.t fitControlUI frame
		frame.add(tabbedPane);// (canvas);
		frame.setVisible(true);
	}

	public void showResidualDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh)
	{
		int iSec = Sec - 1, iSL = SL - 1;
		int nSkippedThBins = 0; // Skipping marginal  bins 
		String Title = "";

		JTabbedPane tabbedPane = new JTabbedPane();
		// Residual plots
		F1D[][] func = new F1D[nSectors][nSL];
		int iPad = 0;
		// Following two tabs are for overall residuals (1D & 2D) for the given sector and SL
		EmbeddedCanvas canvas4 = new EmbeddedCanvas(); // Will provide tab for 1D res.
		canvas4.setSize(3 * 400, 2 * 400);
		canvas4.divide(3, 2);
		for (int j = 0; j < nSL; j++)
		{
			Title = "Sec=" + (iSec + 1) + " SL=" + (j + 1);
			iPad = j;
			canvas4.cd(iPad);
			H1F h1 = h1timeRes.get(new Coordinate(iSec, j));
			
                        func[iSec][j] = new F1D("func", "[amp]*gaus(x,[mean],[sigma])+[amp]*gaus(x,[mean],[sigma2])", -0.5, 0.5);
			func[iSec][j].setLineColor(4);
			func[iSec][j].setLineStyle(1);
			func[iSec][j].setLineWidth(2);
			func[iSec][j].setParameter(0, 1000);
			func[iSec][j].setParameter(1, -0.0);
			func[iSec][j].setParameter(2, 0.05);
                        func[iSec][j].setParameter(3, 500);
			func[iSec][j].setParameter(4, 0.5);
			func[iSec][j].setOptStat(0001111110);
			h1.setOptStat(1110);
                        
			canvas4.draw(h1);
			canvas4.getPad(iPad).setTitle(Title);
			canvas4.setPadTitlesX("residual (cm)");// "Residual"
			canvas4.setPadTitlesY(" ");
		}
		tabbedPane.add(canvas4, "Residual (cm)");

		EmbeddedCanvas canvas5 = new EmbeddedCanvas();
		canvas5.setSize(3 * 400, 2 * 400);
		canvas5.divide(3, 2);
		for (int j = 0; j < nSL; j++)
		{
			Title = "Sec=" + (iSec + 1) + " SL=" + (j + 1);
			iPad = j;
			canvas5.cd(iPad);
			canvas5.getPad(iPad).getAxisZ().setLog(true);
			canvas5.draw(h2timeResVsTrkDoca.get(new Coordinate(iSec, j)));
			canvas5.getPad(iPad).setTitle(Title);
			canvas5.setPadTitlesX("|trkDoca| (cm)");
			canvas5.setPadTitlesY("residual (cm)");
		}
		canvas5.save(String.format(Constants.plotsOutputDir + "plots/residualVsTrkDocaSec%d.png", Sec + 1));
		tabbedPane.add(canvas5, "Residual vs trkDoca"); // Second tab in the resPanes

		// Following two tabs are for individual residuals (1D & 2D) for the given sec, SL & th-bin
		F1D[][][] funcTh = new F1D[nSectors][nSL][6];
		EmbeddedCanvas canvas6 = new EmbeddedCanvas(); // Will provide tab for 1D res.
		canvas6.setSize(3 * 400, 3 * 400);
		canvas6.divide(3, 3);
		for (int k = nSkippedThBins; k < 6 - nSkippedThBins; k++)
		{
			Title = "Sec=" + (iSec + 1) + " SL=" + (iSL + 1)
					+ " th(" + k + ")";
			iPad = k - nSkippedThBins; // = iSL;
			canvas6.cd(iPad);
			H1F h1 = h1timeRes.get(new Coordinate(iSec, iSL, k));
			funcTh[iSec][iSL][k] = new F1D("func", "[amp]*gaus(x,[mean],[sigma])+[amp2]*gaus(x,[mean],[sigma2])", -0.5, 0.5);
			funcTh[iSec][iSL][k].setLineColor(7);
			funcTh[iSec][iSL][k].setLineStyle(1);
			funcTh[iSec][iSL][k].setLineWidth(2);
			funcTh[iSec][iSL][k].setParameter(0, 1000);
			funcTh[iSec][iSL][k].setParameter(1, -0.0);
			funcTh[iSec][iSL][k].setParameter(2, 0.05);
                        funcTh[iSec][iSL][k].setParameter(3, 500);
			funcTh[iSec][iSL][k].setParameter(4, 0.5);
			//funcTh[iSec][iSL][k].setOptStat(1110);
			funcTh[iSec][iSL][k].show(); // Prints fit parameters
			DataFitter.fit(funcTh[iSec][iSL][k], h1, "E");
			funcTh[iSec][iSL][k].setOptStat(0001111110);// (1110000111);//(1110);
			h1.setOptStat(1110);// (1110001111);//(1110);

			canvas6.draw(h1);
			canvas6.getPad(iPad).setTitle(Title);
			canvas6.setPadTitlesX("residual (cm)");// "Residual vs trkDoca"
			canvas6.setPadTitlesY(" ");// "Residual vs trkDoca"
		}
		canvas4.save(String.format(Constants.plotsOutputDir + "plots/residualSec%d.png", Sec + 1));
		tabbedPane.add(canvas6, "residual (cm) (In ThBins)");

		EmbeddedCanvas canvas7 = new EmbeddedCanvas();
		canvas7.setSize(3 * 400, 3 * 400);
		canvas7.divide(3, 3);
		for (int k = nSkippedThBins; k < k - nSkippedThBins; k++)
		{
			Title = "Sec=" + (iSec + 1) + " SL=" + (iSL + 1)
					+ " thBin (" +k + ")";
			iPad = k - nSkippedThBins; // = iSL;
			canvas7.cd(iPad);
			canvas7.getPad(iPad).getAxisZ().setLog(true);
			canvas7.draw(h2timeResVsTrkDoca.get(new Coordinate(iSec, iSL, k)));
			canvas7.getPad(iPad).setTitle(Title);
			canvas7.setPadTitlesX("|trkDoca| (cm)");
			canvas7.setPadTitlesY("residual (cm)");
		}
		canvas5.save(String.format(Constants.plotsOutputDir + "plots/residualVsTrkDocaSec%d.png", Sec + 1));
		tabbedPane.add(canvas7, "Res. vs trkDoca (In ThBins)");

		JFrame frame = new JFrame();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
		// frame.setLocationRelativeTo(null); //Centers on the default screen
		// Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
		frame.setLocationRelativeTo(fitControlFrame);// centered w.r.t fitControlUI frame
		frame.add(tabbedPane);// (canvas);
		frame.setVisible(true);
	}

	public void showTimeDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh)
	{
		int iSec = Sec - 1, iSL = SL - 1;
		int nSkippedThBins = 0; // Skipping marginal 4 bins from both sides
		String Title = "";

		JTabbedPane tabbedPane = new JTabbedPane();
		// Residual plots
		F1D[][] func = new F1D[nSectors][nSL];
		int iPad = 0;

		EmbeddedCanvas canvas7 = new EmbeddedCanvas();
		canvas7.setSize(3 * 400, 3 * 400);
		canvas7.divide(3, 3);
		for (int k = nSkippedThBins; k < 6 - nSkippedThBins; k++)
		{
			// for (int j = 0; j < nSL; j++) {
			Title = "Sec=" + (iSec + 1) + " SL=" + (iSL + 1)
					+ " thBin(" + k + ")";
			iPad = k - nSkippedThBins; // = iSL;
			canvas7.cd(iPad);
			canvas7.getPad(iPad).getAxisZ().setLog(true);
			canvas7.draw(h1timeSlTh.get(new Coordinate(iSec, iSL, k)));
			canvas7.getPad(iPad).setTitle(Title);
			canvas7.setPadTitlesX("time (ns)");
		}
		tabbedPane.add(canvas7, "Time (In ThBins)");

		JFrame frame = new JFrame();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
		// frame.setLocationRelativeTo(null); //Centers on the default screen
		// Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
		frame.setLocationRelativeTo(fitControlFrame);// centered w.r.t fitControlUI frame
		frame.add(tabbedPane);// (canvas);
		frame.setVisible(true);
	}

	public void showLocalAngleDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh)
	{
		int iSec = Sec - 1, iSL = SL - 1;
		String Title = "";

		JTabbedPane tabbedPane = new JTabbedPane();
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		canvas.setSize(3 * 400, 2 * 400);
		canvas.divide(3, 2);
		for (int i = 0; i < nSL; i++)
		{
			canvas.cd(i);
			canvas.draw(h1LocalAngleSL[i]);
		}

		tabbedPane.add(canvas, "Local Angle (alpha) (In Degrees)");

		JFrame frame = new JFrame();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
		// frame.setLocationRelativeTo(null); //Centers on the default screen
		// Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
		frame.setLocationRelativeTo(fitControlFrame);// centered w.r.t fitControlUI frame
		frame.add(tabbedPane);// (canvas);
		frame.setVisible(true);
	}

	public void showBFieldDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh)
	{
		int iSec = Sec - 1, iSL = SL - 1;
		String Title = "";

		JTabbedPane tabbedPane = new JTabbedPane();
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		canvas.setSize(3 * 400, 2 * 400);
		canvas.divide(3, 2);
		for (int i = 0; i < nSL; i++)
		{
			canvas.cd(i);
			canvas.draw(h1bFieldSL[i]);
		}
		canvas.save(Constants.plotsOutputDir + "plots/test_bFieldAllSL.png");
		tabbedPane.add(canvas, "Time (In ThBins)");

		JFrame frame = new JFrame();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
		// frame.setLocationRelativeTo(null); //Centers on the default screen
		// Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
		frame.setLocationRelativeTo(fitControlFrame);// centered w.r.t fitControlUI frame
		frame.add(tabbedPane);// (canvas);
		frame.setVisible(true);
	}

	
	// ------------------ Three dimensional histogram for distance, angle and B-field bins ----------------------------
	// This is used for the fitting.
	// Cannot draw my SimpleH3D, so I want to check it by drawing its XT projections & errors
	// separately
	public void MakeAndDrawXTProjectionsOfXTBhists()
	{
		int nXbins, nTbins, nBbins;
		// Now drawing these projections onto Tabbed Panes:
		String Title = "";
		JFrame frame = new JFrame();

		JTabbedPane mainPane = new JTabbedPane();
		JTabbedPane sectorPanesDist = new JTabbedPane(); // Pane for the time vs trkDoca distribution
		JTabbedPane resolutionDist = new JTabbedPane(); // Pane for the resolution distribution
		JTabbedPane bBinnedPanes_sec = new JTabbedPane(); // Pane for time vs trkDoca based on B-filed bins

		for (int i = iSecMin; i < iSecMax; i++)
		{
			JTabbedPane anglePanes = new JTabbedPane();
			JTabbedPane resPanes = new JTabbedPane();
			for (int k = 0; k < 6; k++)
			{
				EmbeddedCanvas canvas = new EmbeddedCanvas();
				canvas.setSize(3 * 400, 2 * 400);
				canvas.divide(3, 2);
				for (int j = 0; j < nSL; j++)
				{
					canvas.cd(j);
					Title = "Sec=" + (i + 1) + " SL=" + (j + 1)
							+ " thetaBin=(" + k + ")"
							+ " indvFitCol=" + colIndivFit;
					//// canvas.draw(h3BTXmap.get(new Coordinate(i, j, k)).getXYProj());
					canvas.draw(h2timeVtrkDoca.get(new Coordinate(i, j, k)));
					canvas.getPad(j).setTitle(Title);
					canvas.setPadTitlesX("trkDoca");
					canvas.setPadTitlesY("time (ns)");
				}
				anglePanes.add(canvas, "ThBin" + (k + 1));
			}
			sectorPanesDist.add(anglePanes, "Sector " + (i + 1));

			EmbeddedCanvas canvasRes = new EmbeddedCanvas();
			canvasRes.setSize(3 * 400, 2 * 400);
			canvasRes.divide(3, 2);

			for (int k = 0; k < nSL; ++k)
			{
				canvasRes.cd(k);
				H1F h1 = h1timeRes.get(new Coordinate(i, k));
				
                                F1D gausFunc = new F1D("gausFunc", "[amp]*gaus(x,[mean],[sigma])+[amp2]*gaus(x,[mean],[sigma2])", -0.5, 0.5);
                                gausFunc.setLineColor(4);
                                gausFunc.setLineStyle(1);
                                gausFunc.setLineWidth(2);
                                gausFunc.setParameter(0, h1.getMax());
                                gausFunc.setParameter(1, -0.0);
                                gausFunc.setParameter(2, 0.05);
                                gausFunc.setParameter(3, h1.getMax()/2.);
                                gausFunc.setParameter(4, 0.5);
                                gausFunc.setOptStat(1110);
                                h1.setOptStat(111);
				DataFitter.fit(gausFunc, h1, "Q");
				gausFunc.setOptStat(00001111110);
				canvasRes.draw(h1);
			}
			resolutionDist.add("Sec " + (i + 1), canvasRes);
		}

		for (int i = iSecMin; i < iSecMax; i++)
		{
			JTabbedPane bBinnedPanes_sl = new JTabbedPane();
			for (int j = 2; j <= 3; j++)
			{
				JTabbedPane bBinnedPanes_th = new JTabbedPane();
				for (int k = 0; k < 6; k++)
				{
					EmbeddedCanvas canvasBbins = new EmbeddedCanvas();
					canvasBbins.setSize(3 * 400, 2 * 400);
					canvasBbins.divide(5, 2);
					for (int bbin = 0; bbin < BMax; bbin++)
					{
						canvasBbins.cd(bbin);
						//Title = "B-bin = " + bbin + "( " + bbin * 0.2 + 0.1 + " )";
						canvasBbins.draw(h2timeVtrkDoca.get(new Coordinate(i, j, k, bbin)));
						//canvasBbins.getPad(j).setTitle(Title);
						canvasBbins.setPadTitlesX("trkDoca");
						canvasBbins.setPadTitlesY("time (ns)");
					}
					bBinnedPanes_th.add(canvasBbins, "Theta " + (k + 1));
				}
				bBinnedPanes_sl.add(bBinnedPanes_th, "SL " + (j + 1));
			}
			bBinnedPanes_sec.add(bBinnedPanes_sl, "Sector " + (i + 1));
		}

		mainPane.add("time vs trkDoca Dist", sectorPanesDist);
		mainPane.add("B-field Bins", bBinnedPanes_sec);
		mainPane.add("Resolution", resolutionDist);

		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
		frame.setLocationRelativeTo(null);
		frame.add(mainPane);
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		OAInstance.buttonstatus(e);
		acceptorder = OAInstance.isorderOk();
		JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
		if (acceptorder)
		{
			JOptionPane.showMessageDialog(frame, "Click OK to start processing the time to distance fitting...");
			processData();
		}
		else
		{
			System.out.println("I am red and it is not my turn now ;( ");
		}
	}

	public void OpenFitControlUI(TimeToDistanceFitter fitter)
	{

		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (ClassNotFoundException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (InstantiationException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (IllegalAccessException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				new FitControlUI(fitter).setVisible(true); // Defined in FitControlUI.java
			}
		});
	}

	public void SliceViewer(TimeToDistanceFitter fitter)
	{
		// Create a frame and show it through SwingUtilities
		// It doesn't require related methods and variables to be of static type
		SwingUtilities.invokeLater(() ->
		{
			new SliceViewer("Slice Viewer").create(fitter);
		});
	}

	// ----------------------------- step# starting point : The routine starts from here ----------------------------
	@Override
	public void run()
	{
            OpenFitControlUI(this);
		processData();

		System.out.println("Called drawQuickTestPlots();");

		MakeAndDrawXTProjectionsOfXTBhists();
		System.out.println("Called MakeAndDrawXTProjectionsOfXTBhists();");

		// SliceViewer(this); //Now can be opened with a button in FitControlUI

		//OpenFitControlUI(this);
		// drawHistograms(); //Disabled 4/3/17 - to control it by clicks in FitConrolUI.
	}

	private void saveNtuple()
	{
		// nTupletimeVtrkDocaVZ.write("src/files/pionTest.evio");
	}

	public static void main(String[] args)
	{
		ArrayList<String> fileArray = new ArrayList<String>();
		TimeToDistanceFitter rd = new TimeToDistanceFitter(fileArray, true);

		rd.processData();
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
    
    public int getAlphaBin(double Alpha) {
        int bin = 0;
        for(int b =0; b<6; b++) {
            if(Alpha>=AlphaBounds[b][0] && Alpha<=AlphaBounds[b][1] )
                bin = b;
        }
        return bin;
    }
    
    
}
