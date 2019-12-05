
package org.clas.detector.clas12calibration.dc.ui;

import org.clas.detector.clas12calibration.dc.init.Configure;

public class Application
{
	public static void main(String[] args)
	{
		CalibStyle.setStyle();
		Configure.setConfig();
		DC_Calibration DcCalib = new DC_Calibration();
		DcCalib.Initialize();
	}
}
