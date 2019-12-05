/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel, kpdhikari
 *  `------'
*/
package org.clas.detector.clas12calibration.dc.constants;

import org.clas.detector.clas12calibration.dc.init.Configure;

public final class Constants
{
	public static final String ccdb_variation = "dc_team_rga_fall2018"; //"dc_team";
	public static final double rad2deg = 180.0 / Math.PI;
	public static final double deg2rad = Math.PI / 180.0;

	public static final double cos30 = Math.cos(30.0 / rad2deg);
	public static final double beta = 1.0; //0.0;
	public static final double beta_max = 1.02;
	public static final double beta_min = 0.98;
	public static final double v0Averaged = 0.007;
	
	public static final double[] wpdist = // Wire to plane distance:  X_max = 2 x Wire to plane distance
	{ 0.386160, 0.404220, 0.621906, 0.658597, 0.935140, 0.977982 };
        
	public static final int nSL = 6;  // Number of super layers
	public static final int nSectors = 6;  //  Number of sectors
	
	public static final int iSecMin = 0, iSecMax = 6;

	public static final int nLayer = 6;  //  Number of layers
	public static final double[] docaBins =
	{ -0.8, -0.6, -0.4, -0.2, -0.0, 0.2, 0.4, 0.6, 0.8 };
	public static final int nHists = 8;

	public static final int nFitPars = 10;// 9;//v0, deltamn, tmax, distbeta, a, b, c
											// delta_bfield_coefficient, b1, b2, b3, b4, deltaT0;
	// public static final String parName[] = { "v0", "deltamn", "tmax1", "tmax2", "distbeta" };
	public static final double parSteps[] =
	{ 0.00001, 0.00001, 0.001, 0.0001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.00001 };
	public static final String parName[] =
	{ "v0", "vm", "tmax", "distbeta", "delta_bfield_coefficient", "b1", "b2", "b3", "b4", "r", "a", "b", "c" };
	// public static final String parName[] = { "v0", "deltamn", "tmax", "distbeta" };
	// public static final double prevFitPars[] = { 62.92e-04, 1.35, 137.67, 148.02, 0.055 };
	public static final double prevFitPars[] =
	{ 50e-04, 1.5, 137.67, 0.060 };

	// time could get -ve due to T0 over-correction.
	public static final double tMin = -50.0; // Initially I was using 0.0 for all time or t-vs-x
												// histograms
	// public static final double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 };
	public static final double tMaxSL[] =
	{ 155.0, 165.0, 300.0, 320.0, 600.0, 650.0 };
	// public static final double timeAxisMax[] = {300.0, 300.0, 650.0, 650.0, 650.0, 650.0};
	public static final double timeAxisMax[] =
	{ 300.0, 300.0, 650.0, 650.0, 800.0, 850.0 };

	public static final int nCrates = 18;// Goes from 41 to 58 (one per chamber)
	public static final int nSlots = 20; // Total slots in each crate (only 14 used)
	public static final int nChannels = 96;// Total channels per Slot (one channel per wire)
	public static final int nLayers0to35 = 36;// Layers in each sector (0th is closest to CLAS
												// center), 6 in each SL
	public static final int nWires = 112; // Wires in each layer of a chamber
	public static final int nComponents = 112; // == nWires (translation table in CCDB uses
												// components instead of wire)
	public static final int nRegions = 3;
	public static final int nCables = 84;
	public static final int nCables6 = 6; // # of Cables per DCRB or STB.
	public static final int nSlots7 = 7; // # of STBs or occupied DCRB slots per SL.
	public static final double[] tLow4T0Fits  = {180.0, 180.0, 180.0, 280.0, 480.0, 480.0};//For  KPP runs
												
	//=
	//{ 000.0, 000.0, 2200.0, 2200.0, 2200.0, 2200.0 };// For Cosmic runs
	public static final double[] tHigh4T0Fits  = {380.0, 380.0, 680.0, 780.0, 1080.0, 1080.0}; //For KPP runs												 
	//= { 4000.0, 4000.0, 2700.0, 2700.0, 3000.0, 3000.0 }; // For Cosmic runs
	public static final double[] tLow4TmaxFits = { 180.0, 180.0, 180.0, 280.0, 480.0, 480.0 };
	public static final double[] tHigh = { 380.0, 380.0, 680.0, 780.0, 1080.0, 1080.0 };

	// Following constants are for temporary use (I plan to remove its use later) - Krishna
	// =================== =================== ===================
	// histTypeToUseInFitting = 0; // for using X-profiles from h2timeVtrkDocaVZ
	// = 1; // for using 2D hist itself from h2timeVtrkDocaVZ
	// = 2; // for using XY-proj from h3BTXmap
	// = 3; // for using B-field also from h3BTXmap

    public static final int histTypeToUseInFitting = Configure.HistType; //3; //1; //3; //1;// 2;//1;//2;

	public static final double calcDocaCut = 2.0; ////5.0; // 1.0 //0.85
	public static final String configFilePath = Configure.jarFilePath + "/config/";
	public static final String dataOutputDir = Configure.jarFilePath + "/../data/";
	public static final String plotsOutputDir = Configure.jarFilePath + "/../plots/";
	public static final String outFileForFitPars = dataOutputDir + "fitParameters_run";//"src/files/fitParameters.txt";

	// ================ Binning parameters various histograms ==================
	// make the following controllable from the GUI (as we may have different Max for B-field
	// depending on expt.
	public static final int nBFieldBins = 10; //20;
	public static final double[] BFieldBins = {0.0, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
	public static final double bFieldMin = 0.0, bFieldMax = 2.0;  // <--------- Changed the limit for B-field
	//public static final double bFieldMin = 0.0, bFieldMax = 1.5;
	public static final int binForTestPlotTemp = 15;//nBFieldBins / 2; // for temp purpose
	public static final int nLocalAngleBins = 160;
	public static final double localAngleMin = -80.0, localAngleMax = 80.0;

	private Constants()
	{
	}
}
