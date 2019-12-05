//Filename: Config.java
//Description: Read calibration configuration from the config.cfg file
//Author: Latif Kabir < latif@jlab.org >
//Created: Sun Dec 24 00:16:14 2017 (-0500)
//URL: jlab.org/~latif

package org.clas.detector.clas12calibration.dc.init;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
public class Configure
{
	Properties configFile;
	public static int Sector;
	public static String BField;
	public static int HistType;
	public static double TStart;
	public static String jarFilePath;
	
	public Configure(String config_file)
	{
		configFile = new java.util.Properties();
		try
		{
			File jarPath = new File(Configure.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			jarFilePath = jarPath.getParent();
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		try
		{
			// configFile.load(this.getClass().getClassLoader().getResourceAsStream(config_file));
			configFile.load(new FileInputStream(config_file));
		}
		catch (Exception eta)
		{
			eta.printStackTrace();
		}
	}

	public String getProperty(String key)
	{
		String value = configFile.getProperty(key);
		return value;
	}

	public static void setConfig()	
	{
		//Configure config = new Configure("/Users/ziegler/Desktop/Base/CodeDevel/Calibration/clas12dc/Calibration/config/config.cfg");
		//Configure config = new Configure("/config/config.cfg");
		System.out.println("Reading the calibration configuration ... ...");
		//Sector = Integer.parseInt(config.getProperty("Sector").replaceAll("\\s", ""));
		//BField = config.getProperty("BField");
		//HistType = Integer.parseInt(config.getProperty("HistType").replaceAll("\\s", ""));
		//TStart = (double)Integer.parseInt(config.getProperty("TStart").replaceAll("\\s", ""));
		//System.out.println(" Sector: " + Configure.Sector  + " HistType: " + Configure.HistType);
		//System.out.println("Jar file dir: " + Configure.jarFilePath);
	}
		
	public static void main(String[] args)
	{
		Configure.setConfig();
		System.out.println(" Sector: " + Configure.Sector  + " BField: " + Configure.BField);
	}
}
