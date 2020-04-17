/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.calt0;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 * 
 * @author ziegler
 */


public class ReadTT {
    
    static boolean LOADED = false;
    public static synchronized void Load(int run, String variation) {
        if (LOADED) return;
        System.out.println(" TT TABLE FILLED..... for Run "+run+" with VARIATION "+variation);
        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(run, variation);
        dbprovider.loadTable("/daq/tt/dc");
        //disconnect from database. Important to do this after loading tables.
        dbprovider.disconnect();
        
        
        for (int i = 0; i < dbprovider.length("/daq/tt/dc/crate"); i++) {
            int crate = dbprovider.getInteger("/daq/tt/dc/crate", i);
            int slot = dbprovider.getInteger("/daq/tt/dc/slot", i);
            int chan = dbprovider.getInteger("/daq/tt/dc/chan", i);
            int sec = dbprovider.getInteger("/daq/tt/dc/sector", i);
            int lay = dbprovider.getInteger("/daq/tt/dc/layer", i);
            int comp = dbprovider.getInteger("/daq/tt/dc/component", i);
            
            Crates[sec - 1][lay - 1][comp - 1] = crate;
            Slots[sec - 1][lay - 1][comp - 1] = slot;
            Channels[sec - 1][lay - 1][comp - 1] = chan;
            Sectors[crate - 41][slot - 1][chan] = sec; // Crate # starts from 41 &
            Layers[crate - 41][slot - 1][chan] = lay;
            Components[crate - 41][slot - 1][chan] = comp;
		
        }
        LOADED = true;
    }
    public static final int nCrates = 18;// Goes from 41 to 58 (one per chamber)
    public static final int nSlots = 20; // Total slots in each crate (only 14 used)
    public static final int nChannels = 96;// Total channels per Slot (one channel per wire)
    public static final int nLayers0to35 = 36;// Layers in each sector (0th is closest to CLAS
    public static final int nComponents = 112; // == nWires (translation table in CCDB uses
    public static final int nCables = 84;
    public static final int nCables6 = 6; // # of Cables per DCRB or STB.
    public static final int nSlots7 = 7; // # of STBs or occupied DCRB slots per SL.
    public static final int nSectors = 6;
    
    public static int[][][] Crates = new int[nSectors][nLayers0to35][nComponents];
    public static int[][][] Slots = new int[nSectors][nLayers0to35][nComponents];
    public static int[][][] Channels = new int[nSectors][nLayers0to35][nComponents];
    public static int[][][] Sectors = new int[nCrates][nSlots][nChannels];
    public static int[][][] Layers = new int[nCrates][nSlots][nChannels];
    public static int[][][] Components = new int[nCrates][nSlots][nChannels];

    public static final int[][] CableID = {
            //[nLayer][nLocWire] => nLocWire=16, 7 groups of 16 wires in each layer
            {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 1
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 2
            {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 3
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 4
            {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 5
            {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 6
            //===> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
            // (Local wire ID: 0 for 1st, 16th, 32th, 48th, 64th, 80th, 96th wires)
    };
}
        
        
        