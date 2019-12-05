/*  @author m.c.kunkel
 *  @author KPAdhikari
 */
package org.clas.detector.clas12calibration.dc.fit;


//import static org.clas.detector.clas12calibration.dc.domain.Constants.*;
import java.util.ArrayList;

import org.jlab.groot.math.Func1D;
import org.jlab.rec.dc.timetodistance.TableLoader;

public class DCFitDrawerForXDoca extends Func1D
{
	private int superlayer;
	private int alphaBin;
	private boolean isPolyFit;
	private double[] fPars;
	private DCTimeFunction timeFunc;
	private double bField = 0.0;

	public DCFitDrawerForXDoca()
	{
		super("calibFnToDraw", 0.0, 1.0);
		this.initParameters();
	}

	public DCFitDrawerForXDoca(String name, double xmin, double xmax, int superlayer, int thetaBin, boolean isPolyFit)
	{
		super(name, xmin, xmax);
		this.initParameters();
		this.superlayer = superlayer;
		this.alphaBin = thetaBin;
		this.isPolyFit = isPolyFit;
	}

	public DCFitDrawerForXDoca(String name, double xmin, double xmax, int superlayer, int thetaBin, double bField,
			boolean isPolyFit)
	{
		super(name, xmin, xmax);
		this.initParameters();
		this.superlayer = superlayer;
		this.alphaBin = thetaBin;
		this.isPolyFit = isPolyFit;
		this.bField = bField;
	}

	private void initParameters()
	{
		ArrayList<String> pars = new ArrayList<String>();
		pars.add("v0");
                pars.add("vm");
		pars.add("tmax");
		pars.add("distbeta");
		pars.add("delta_bfield_coefficient");
		pars.add("b1");
		pars.add("b2");
		pars.add("b3");
		pars.add("b4");
		pars.add("r");
                pars.add("a");
		pars.add("b");
		pars.add("c");
		for (int loop = 0; loop < pars.size(); loop++)
		{
			this.addParameter(pars.get(loop));
		}
		
	}

	private void setParmLength(int i)
	{
		this.fPars = new double[i + 1];
	}

	@Override
	public void setParameters(double[] params)
	{
		setParmLength(params.length);
		for (int i = 0; i < params.length; i++)
		{
			this.setParameter(i, params[i]);
			fPars[i] = params[i];
		}
	}

	@Override
	public double evaluate(double xDoca)
	{
		timeFunc = new DCTimeFunction(superlayer, TableLoader.AlphaMid[alphaBin], bField, fPars);
		double calcTime = isPolyFit ? timeFunc.polynFit(xDoca) : timeFunc.polynFit2(xDoca);
		return calcTime;
	}

}