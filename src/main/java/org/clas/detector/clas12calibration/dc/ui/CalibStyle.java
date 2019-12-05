/*
  @author Latif Kabir < jlab.org/~latif >
*/
package org.clas.detector.clas12calibration.dc.ui;

import org.jlab.groot.base.GStyle;

public class CalibStyle
{
	/**
	 * Empty Constructor
	 */
	public CalibStyle()
	{

	}

	/**
	 * Set custom style for plotting
	 */
	public static void setStyle()
	{
		GStyle.getGraphErrorsAttributes().setMarkerStyle(0);
		GStyle.getGraphErrorsAttributes().setMarkerColor(3);
		GStyle.getGraphErrorsAttributes().setMarkerSize(7);
		GStyle.getGraphErrorsAttributes().setLineColor(3);
		GStyle.getGraphErrorsAttributes().setLineWidth(3);

		GStyle.getAxisAttributesX().setTitleFontSize(24);
		GStyle.getAxisAttributesX().setLabelFontSize(18);
		GStyle.getAxisAttributesY().setTitleFontSize(24);
		GStyle.getAxisAttributesY().setLabelFontSize(18);

		GStyle.getH1FAttributes().setLineWidth(3);
		GStyle.getH1FAttributes().setLineColor(21);
		// GStyle.getH1FAttributes().setFillColor(34);
		// GStyle.getH1FAttributes().setOptStat("10");
		GStyle.getH1FAttributes().setOptStat("111111");

		GStyle.getFunctionAttributes().setLineWidth(2);
		GStyle.getFunctionAttributes().setLineColor(32);
		GStyle.getAxisAttributesZ().setLog(true);

		//GStyle.setPalette(PaletteName.kRainBow); // Will work after updating to new Groot
	}
}
