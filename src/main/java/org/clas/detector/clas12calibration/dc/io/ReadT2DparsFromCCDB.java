/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.io;

/**
 *
 * @author KPAdhikari, Latif < jlab.org/~latif > 
 */
import org.jlab.ccdb.*;
import org.jlab.ccdb.JDBCProvider;

import static org.clas.detector.clas12calibration.dc.constants.Constants.nFitPars;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSL;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSectors;
import static org.clas.detector.clas12calibration.dc.constants.Constants.ccdb_variation;

import java.util.Vector;

public class ReadT2DparsFromCCDB
{
	// private int superlayer;
	public Vector<Double> Sector, Superlayer, Component;
	public Vector<Double> v0, vmid, tmax, distbeta;
	public Vector<Double> delta_bfield_coefficient, b1, b2, b3, b4, r;
	public static double[][][] parsFromCCDB = new double[nSectors+1][nSL][nFitPars];// nFitPars = 10
	String ccdbVariation = ccdb_variation;
	int run_number = -1;
	
	/**
	 * 
	 * @param ccdbVariation
	 * @param runNumber : A negative run number will discard setting run number preference
	 */
	public ReadT2DparsFromCCDB(String ccdbVariation, int runNumber)
	{
		this.ccdbVariation = ccdbVariation;
		this.run_number = runNumber;
	}
	
	/**
	 * Load the parameters
	 */
	public void LoadCCDB()
	{	
		System.out.println("=========================================================================================");
		System.out.println("\t\tAttempting to load CCDB params ... ...");

		// JDBCProvider provider = CcdbPackage.createProvider("mysql://localhost") ;
		JDBCProvider provider = CcdbPackage.createProvider("mysql://clas12reader@clasdb.jlab.org/clas12");
		provider.connect();

		// to check the table exists
		System.out.println("\t\t---->/calibration/dc/time_to_distance/time2dist exists? - "
				+ provider.isTypeTableAvailable("/calibration/dc/time_to_distance/time2dist"));
		System.out.println("\t\t---->CCDB variation name is: " + ccdbVariation);
		System.out.println("\t\t---->CCDB run number: " + run_number);
		System.out.println("=========================================================================================");
		
		
		// provider.setDefaultVariation("dc_test1");
		// provider.setDefaultVariation("default");
		provider.setDefaultVariation(ccdbVariation);
		if(run_number<=0)
			provider.setDefaultRun(1000);
		else
			provider.setDefaultRun(run_number);

		Assignment asgmt = provider.getData("/calibration/dc/time_to_distance/time2dist");
		for (Vector<Double> row : asgmt.getTableDouble())
		{
			for (Double cell : row)
			{
				System.out.print(cell + " ");
			}
			System.out.println(); // next line after a row
		}
		System.out.println("----------------------------------------------------------------------------------------");
		Vector<Double> doubleValues; // System.out.println(doubleValues);

		doubleValues = asgmt.getColumnValuesDouble(0); // First column values
		//System.out.println("First 2 in Sector column:" + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
		doubleValues = asgmt.getColumnValuesDouble(1); // Second column values
		//System.out.println(
		//		"First 2 in Superlayer column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
		doubleValues = asgmt.getColumnValuesDouble(2); // Third column values
		//System.out.println("First 2 in v0 column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));

		// Now put all the columns in the corresponding Vector members.
		Sector = asgmt.getColumnValuesDouble(0);
		Superlayer = asgmt.getColumnValuesDouble(1);
		Component = asgmt.getColumnValuesDouble(2);
		v0 = asgmt.getColumnValuesDouble(3);
		vmid = asgmt.getColumnValuesDouble(14);
		tmax = asgmt.getColumnValuesDouble(5);
		distbeta = asgmt.getColumnValuesDouble(6);
		delta_bfield_coefficient = asgmt.getColumnValuesDouble(7);
		b1 = asgmt.getColumnValuesDouble(8);
		b2 = asgmt.getColumnValuesDouble(9);
		b3 = asgmt.getColumnValuesDouble(10);
		b4 = asgmt.getColumnValuesDouble(11);
		r = asgmt.getColumnValuesDouble(13);

		for (int i = 0; i < nSectors; i++)
		{
			for (int j = 0; j < nSL; j++)
			{
				parsFromCCDB[i][j][0] = v0.elementAt(6 * i + j);
				parsFromCCDB[i][j][1] = vmid.elementAt(6 * i + j);
				parsFromCCDB[i][j][2] = tmax.elementAt(6 * i + j);
				parsFromCCDB[i][j][3] = distbeta.elementAt(6 * i + j);
				parsFromCCDB[i][j][4] = delta_bfield_coefficient.elementAt(6 * i + j);
				parsFromCCDB[i][j][5] = b1.elementAt(6 * i + j);
				parsFromCCDB[i][j][6] = b2.elementAt(6 * i + j);
				parsFromCCDB[i][j][7] = b3.elementAt(6 * i + j);
				parsFromCCDB[i][j][8] = b4.elementAt(6 * i + j);
				parsFromCCDB[i][j][9] = r.elementAt(6 * i + j);
			}
		}
                //Fill for average over all sector
                for (int j = 0; j < nSL; j++)
                {
                        parsFromCCDB[6][j][0] = v0.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][1] = vmid.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][2] = tmax.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][3] = distbeta.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][4] = delta_bfield_coefficient.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][5] = b1.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][6] = b2.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][7] = b3.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][8] = b4.elementAt(6 * 0 + j);
                        parsFromCCDB[6][j][9] = r.elementAt(6 * 0 + j);
                }
	}
	
	public void printPars()
	{
		System.out.println("----------------------------------------------------------------------------------------------------------");
		System.out.println("S \t SL \t v0 \t vmid \t tmax \t distbeta \t delta_B_coeff \t b1 \t b2 \t b3 \t b4 \t r");
		for (int i = 0; i < nSectors; i++)
		{
			for (int j = 0; j < nSL; j++)
			{
				System.out.println((i + 1) + "\t" + (j + 1) + "\t" + parsFromCCDB[i][j][0] + "\t" + parsFromCCDB[i][j][1] + "\t" + parsFromCCDB[i][j][2] + "\t" + parsFromCCDB[i][j][3] + "\t" + parsFromCCDB[i][j][4] + "\t"
			    + parsFromCCDB[i][j][5] + "\t" + parsFromCCDB[i][j][6] + "\t" + parsFromCCDB[i][j][7] + "\t" + parsFromCCDB[i][j][8] + "\t" + parsFromCCDB[i][j][9]);
			}
		}
		System.out.println("----------------------------------------------------------------------------------------------------------");
	}
	/**
	 * Unit test
	 * @param args
	 */
	public static void main(String[] args)
	{
		ReadT2DparsFromCCDB read_ccdb = new ReadT2DparsFromCCDB("calib", 3050);
		read_ccdb.LoadCCDB();
		read_ccdb.printPars();
	}
}
