/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.io;

/**
 *
 * @author KPAdhikari
 */
import org.jlab.ccdb.*;
import org.jlab.ccdb.JDBCProvider;

import static org.clas.detector.clas12calibration.dc.constants.Constants.nChannels;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nComponents;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nCrates;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nFitPars;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nLayers0to35;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSL;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSectors;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSlots;

import java.util.Vector;

public class GetDCTranslationTableFromCCDB
{
	// private int superlayer;
	public Vector<Double> vCrate, vSlot, vChan, vSector, vLayer, vComp, vOrder;
	public int[][][] Crates = new int[nSectors][nLayers0to35][nComponents];
	public int[][][] Slots = new int[nSectors][nLayers0to35][nComponents];
	public int[][][] Channels = new int[nSectors][nLayers0to35][nComponents];
	public int[][][] Sectors = new int[nCrates][nSlots][nChannels];
	public int[][][] Layers = new int[nCrates][nSlots][nChannels];
	public int[][][] Components = new int[nCrates][nSlots][nChannels];

	public GetDCTranslationTableFromCCDB()
	{
		System.out.println("Hi ... from GetDCTranslationTableFromCCDB() constructor ");
		// JDBCProvider provider = CcdbPackage.createProvider("mysql://localhost") ;
		JDBCProvider provider = CcdbPackage.createProvider("mysql://clas12reader@clasdb.jlab.org/clas12");
		provider.connect();

		// to check the table exists
		System.out.println("/daq/tt/dc exists? - "
				+ provider.isTypeTableAvailable("/daq/tt/dc"));

		provider.setDefaultVariation("default");// for default variation, this line not needed?
		Assignment asgmt = provider.getData("/daq/tt/dc");
		for (Vector<Double> row : asgmt.getTableDouble())
		{
			for (Double cell : row)
			{
				System.out.print(cell + " ");
			}
			System.out.println(); // next line after a row
		}
		Vector<Double> doubleValues; // System.out.println(doubleValues);

		doubleValues = asgmt.getColumnValuesDouble(0); // First column values
		System.out.println("First 2 in Sector column:" + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
		doubleValues = asgmt.getColumnValuesDouble(1); // Second column values
		System.out.println(
				"First 2 in Superlayer column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
		doubleValues = asgmt.getColumnValuesDouble(2); // Third column values
		System.out.println("First 2 in v0 column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));

		// Now put all the columns in the corresponding Vector members.
		vCrate = asgmt.getColumnValuesDouble(0);
		vSlot = asgmt.getColumnValuesDouble(1);
		vChan = asgmt.getColumnValuesDouble(2);
		vSector = asgmt.getColumnValuesDouble(3);
		vLayer = asgmt.getColumnValuesDouble(4);
		vComp = asgmt.getColumnValuesDouble(5);
		vOrder = asgmt.getColumnValuesDouble(6);

		int crate, slot, chan, sec, lay, comp;
		System.out.println("vector size = " + vCrate.size());

		for (int i = 0; i < vCrate.size(); i++)
		{
			crate = vCrate.elementAt(i).intValue();
			slot = vSlot.elementAt(i).intValue();
			chan = vChan.elementAt(i).intValue();
			sec = vSector.elementAt(i).intValue();
			lay = vLayer.elementAt(i).intValue();
			comp = vComp.elementAt(i).intValue();
			Crates[sec - 1][lay - 1][comp - 1] = vCrate.elementAt(i).intValue();
			Slots[sec - 1][lay - 1][comp - 1] = vSlot.elementAt(i).intValue();
			Channels[sec - 1][lay - 1][comp - 1] = vChan.elementAt(i).intValue();
			Sectors[crate - 41][slot - 1][chan] = vSector.elementAt(i).intValue(); // Crate # starts from 41 &
																		// goes upto 58
			Layers[crate - 41][slot - 1][chan] = vLayer.elementAt(i).intValue();
			Components[crate - 41][slot - 1][chan] = vComp.elementAt(i).intValue();
		}

		System.out.println("Debug ..");
	}
}
