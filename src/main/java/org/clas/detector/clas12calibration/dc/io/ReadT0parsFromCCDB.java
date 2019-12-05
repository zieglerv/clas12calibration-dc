/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.io;

import static org.clas.detector.clas12calibration.dc.constants.Constants.nCables6;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nFitPars;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSL;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSectors;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSlots7;
import static org.clas.detector.clas12calibration.dc.constants.Constants.ccdb_variation;

/**
 *
 * @author KPAdhikari
 */
import java.io.IOException;

import org.clas.detector.clas12calibration.dc.constants.Constants;
import org.clas.detector.clas12calibration.dc.core.*;
import org.clas.detector.clas12calibration.dc.fit.TimeToDistanceFitter;
import org.jlab.ccdb.*;
import org.jlab.ccdb.JDBCProvider;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadT0parsFromCCDB
{
	public Vector<Double> Sector, Superlayer, Slot, Cable;
	public Vector<Double> T0Correction, T0Error;
	String ccdbVariation = ccdb_variation;
	int run_number = -1;

	public ReadT0parsFromCCDB(String ccdbVariation, int runNumber)
	{
		this.ccdbVariation = ccdbVariation;
		this.run_number = runNumber;

		System.out.println("Connecting to CCDB ... ");
		// JDBCProvider provider = CcdbPackage.createProvider("mysql://localhost") ;
		JDBCProvider provider = CcdbPackage.createProvider("mysql://clas12reader@clasdb.jlab.org/clas12");
		provider.connect();

		// to check the table exists
		System.out.println("/calibration/dc/time_corrections/T0Corrections exists? - "
				+ provider.isTypeTableAvailable("/calibration/dc/time_corrections/T0Corrections"));
		System.out.println("CCDB variation name is " + ccdbVariation);
		// provider.setDefaultVariation("dc_test1"); //("default");
		provider.setDefaultVariation(ccdbVariation);
		if(run_number<=0)
			provider.setDefaultRun(1000);
		else
			provider.setDefaultRun(run_number);

		Assignment asgmt = provider.getData("/calibration/dc/time_corrections/T0Corrections");

		// Now put all the columns in the corresponding Vector members.
		Sector = asgmt.getColumnValuesDouble(0);
		Superlayer = asgmt.getColumnValuesDouble(1);
		Slot = asgmt.getColumnValuesDouble(2);
		Cable = asgmt.getColumnValuesDouble(3);
		T0Correction = asgmt.getColumnValuesDouble(4);
		T0Error = asgmt.getColumnValuesDouble(5);
	}

	public void printCurrentT0s()
	{
		for (int i = 0; i < Sector.size(); i++)
		{
			//System.out.println(String.format("%d  %d  %d  %d  %4.3f  %4.3f", Sector.elementAt(i), Superlayer.get(i),
			//		Slot.get(i), Cable.get(i), T0Correction.get(i), T0Error.get(i)));
			System.out.println( Sector.elementAt(i) + "\t" + Superlayer.get(i) + "\t" + Slot.get(i) + "\t" + Cable.get(i) + "\t" + T0Correction.get(i) + "\t" + T0Error.get(i));
		}
	}

	/**
	 * Prints new values of T0s (current-T0 + deltaT0) for a given sector & superlayer
	 *
	 * @param superlayer
	 *            superlayer (1,2, ..,6)
	 * @param deltaT0
	 *            further correction to T0 (determined from the time-to-distance fits)
	 */
	public void printModifiedT0s(int sector, int superlayer, double deltaT0)
	{
		double sec = 0, sl = 0;
		double newT0 = 0.0;
		for (int i = 0; i < Sector.size(); i++)
		{
			sec = Sector.elementAt(i);
			sl = Superlayer.get(i);
			newT0 = T0Correction.get(i) - deltaT0;

			if (sec == sector && sl == superlayer)
			{
//				System.out.println(String.format("%d  %d  %d  %d  %4.3f  %4.3f",
//						sec, sl, Slot.get(i), Cable.get(i), newT0, T0Error.get(i)));
				System.out.println( sec + "\t" + sl + "\t" + Slot.get(i) + "\t" + Cable.get(i) + "\t" + newT0 + "\t" + T0Error.get(i));
			}
		}
	}

	/**
	 * Writes out new values of T0s (current-T0 + deltaT0) to a file for a given sector & superlayer
	 *
	 * @param superlayer
	 *            superlayer (1,2, ..,6)
	 * @param deltaT0
	 *            further correction to T0 (determined from the time-to-distance fits)
	 */
	public void writeOutModifiedT0s(int sector, int superlayer, double deltaT0)
	{
		boolean append_to_file = false;
		FileOutputWriter file = null;
		try
		{
			file = new FileOutputWriter(String.format(Constants.dataOutputDir + "T0plus_deltaT0_Sec%dSL%d.txt",
					sector, superlayer), append_to_file);
			file.Write("#Sector  Superlayer  Slot  Cable  T0Correction  T0Error");
		}
		catch (IOException ex)
		{
			Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
		}

		double sec = 0, sl = 0;
		double newT0 = 0.0;
		for (int i = 0; i < Sector.size(); i++)
		{
			sec = Sector.elementAt(i);
			sl = Superlayer.get(i);
			newT0 = T0Correction.get(i) - deltaT0;
			String str = " ";

			if (sec == sector && sl == superlayer)
			{
				//str = String.format("%d  %d  %d  %d  %4.3f  %4.3f",
				//		sec, sl, Slot.get(i), Cable.get(i), newT0, T0Error.get(i));
				str = sec + "\t" + sl + "\t" + Slot.get(i) + "\t" + Cable.get(i) + "\t" + newT0 + "\t" + T0Error.get(i);

				if (!(file == null))
				{
					file.Write(str);
					// file.Close();
				}
			}
		}

		try
		{
			file.Close();
		}
		catch (IOException ex)
		{
			Logger.getLogger(ReadT0parsFromCCDB.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args)
	{
		ReadT0parsFromCCDB readT0 = new ReadT0parsFromCCDB("calib", 3050);
		readT0.printCurrentT0s();
		//readT0.printModifiedT0s(6, 6, 10.0);
		//readT0.writeOutModifiedT0s(6, 6, 10.0);
	}
}
