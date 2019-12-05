/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.init;

/**
 *
 * @author kpadhikari
 */
import java.util.Random;
import javax.swing.JFrame;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;

public class SimpleH3D
{
	private double[][][] array3D;
	private double[][][] array3DError;
	public double[] axisMarginsX;
	public double[] axisMarginsY;
	public double[] axisMarginsZ;
	private int nBinsX, nBinsY, nBinsZ;

	private double minValX, maxValX;
	private double minValY, maxValY;
	private double minValZ, maxValZ;
	private double binWidthX, binWidthY, binWidthZ;

	/**
	 * Creates a 3D Histogram (like container) class with the specified parameters.
	 *
	 * @param name
	 *            the name of the histogram
	 * @param nx
	 *            the number of x axis bins
	 * @param xmin
	 *            the minimum x axis value
	 * @param xmax
	 *            the maximum x axis value
	 * @param ny
	 *            the number of y axis bins
	 * @param ymin
	 *            the minimum y axis value
	 * @param ymax
	 *            the maximum y axis value
	 */
	public SimpleH3D(int nx, double xmin, double xmax, int ny,
			double ymin, double ymax, int nz, double zmin, double zmax)
	{
		nBinsX = nx;
		nBinsY = ny;
		nBinsZ = nz;

		if (xmin <= xmax)
		{
			minValX = xmin;
			maxValX = xmax;
		}
		else
		{
			minValX = xmax;
			maxValX = xmin;
		}

		if (ymin <= ymax)
		{
			minValY = ymin;
			maxValY = ymax;
		}
		else
		{
			minValY = ymax;
			maxValY = ymin;
		}

		if (zmin <= zmax)
		{
			minValZ = zmin;
			maxValZ = zmax;
		}
		else
		{
			minValZ = zmax;
			maxValZ = zmin;
		}

		binWidthX = (maxValX - minValX) / nBinsX;
		axisMarginsX = new double[nBinsX + 1];
		for (int i = 0; i <= nBinsX; i++)
		{
			axisMarginsX[i] = minValX + i * binWidthX;
		}

		binWidthY = (maxValY - minValY) / nBinsY;
		axisMarginsY = new double[nBinsY + 1];
		for (int i = 0; i <= nBinsY; i++)
		{
			axisMarginsY[i] = minValY + i * binWidthY;
		}

		binWidthZ = (maxValZ - minValZ) / nBinsZ;
		axisMarginsZ = new double[nBinsZ + 1];
		for (int i = 0; i <= nBinsZ; i++)
		{
			axisMarginsZ[i] = minValZ + i * binWidthZ;
		}

		array3D = new double[nBinsX][nBinsY][nBinsZ];
		array3DError = new double[nBinsX][nBinsY][nBinsZ];
	}

	public void fill(double x, double y, double z)
	{
		int binX = (int) ((x - minValX) / binWidthX);
		int binY = (int) ((y - minValY) / binWidthY);
		int binZ = (int) ((z - minValZ) / binWidthZ);
		double error = 0.0;
		/*
		 * System.out.println("bins: " + binX + " " + binY + " " + binZ + " nBins: "+ nBinsX + " " +
		 * nBinsY + " " + nBinsZ );
		 */
		if (binX >= 0 && binY >= 0 && binZ >= 0 && binX < nBinsX && binY < nBinsY && binZ < nBinsZ)
		{
			array3D[binX][binY][binZ] = array3D[binX][binY][binZ] + 1.0;
			error = Math.sqrt(Math.abs(array3D[binX][binY][binZ]));
			/*
			 * System.out.println("bins: " + binX + " " + binY + " " + binZ +
			 * " Val="+array3D[binX][binY][binZ] + " Err=" + error);
			 */
			array3DError[binX][binY][binZ] = error;
		}
	}

	public double getBinContent(int binx, int biny, int binz)
	{
		if (binx >= 0 && binx < nBinsX && biny >= 0 && biny < nBinsY && binz >= 0 && binz < nBinsZ)
		{

			return array3D[binx][biny][binz];
		}
		return 0.0; // No need for else here, it will have the same effect
	}

	public double getBinError(int binx, int biny, int binz)
	{
		if (binx >= 0 && binx < nBinsX && biny >= 0 && biny < nBinsY && binz >= 0 && binz < nBinsZ)
		{

			return array3DError[binx][biny][binz];
		}
		return 0.0; // No need for else here, it will have the same effect
	}

	public double[] getBinCenter(int binx, int biny, int binz)
	{
		double[] xyz = new double[3];

		xyz[0] = minValX + (binx * 1.0 + 0.5) * binWidthX;
		xyz[1] = minValY + (biny * 1.0 + 0.5) * binWidthY;
		xyz[2] = minValZ + (binz * 1.0 + 0.5) * binWidthZ;

		return xyz; // Return the **reference** (location) of the array
	}

	public double getBinCenterX(int binx)
	{
		return minValX + (binx * 1.0 + 0.5) * binWidthX;
	}

	public double getBinCenterY(int biny)
	{
		return minValY + (biny * 1.0 + 0.5) * binWidthY;
	}

	public double getBinCenterZ(int binz)
	{
		return minValZ + (binz * 1.0 + 0.5) * binWidthZ;
	}

	public double getXBinProj(int binx)
	{
		double sum = 0;
		for (int j = 0; j < nBinsY; j++)
		{
			for (int k = 0; k < nBinsZ; k++)
			{
				sum = sum + array3D[binx][j][k];
			}
		}
		return sum;
	}

	public double getYBinProj(int biny)
	{
		double sum = 0;
		for (int j = 0; j < nBinsX; j++)
		{
			for (int k = 0; k < nBinsZ; k++)
			{
				sum = sum + array3D[j][biny][k];
			}
		}
		return sum;
	}

	public double getZBinProj(int binz)
	{
		double sum = 0;
		for (int j = 0; j < nBinsX; j++)
		{
			for (int k = 0; k < nBinsY; k++)
			{
				sum = sum + array3D[j][k][binz];
			}
		}
		return sum;
	}

	public double getXYBinProj(int binx, int biny)
	{
		double sum = 0;
		for (int k = 0; k < nBinsZ; k++)
		{
			sum = sum + array3D[binx][biny][k];

		}
		return sum;
	}

	public double getYZBinProj(int biny, int binz)
	{
		double sum = 0;
		for (int j = 0; j < nBinsX; j++)
		{
			sum = sum + array3D[j][biny][binz];
		}
		return sum;
	}

	public double getXZBinProj(int binx, int binz)
	{
		double sum = 0;
		for (int k = 0; k < nBinsY; k++)
		{
			sum = sum + array3D[binx][k][binz];
		}
		return sum;
	}

	public H2F getXYProj()
	{
		H2F projXY = new H2F("hist2D", nBinsX, minValX, maxValX, nBinsY, minValY, maxValY);
		double sum = 0;
		for (int i = 0; i < nBinsX; i++)
		{
			for (int j = 0; j < nBinsY; j++)
			{
				sum = 0;
				for (int k = 0; k < nBinsZ; k++)
				{
					sum = sum + array3D[i][j][k];
				}
				projXY.setBinContent(i, j, sum);
			}
		}
		return projXY;
	}

	public H2F getYZProj()
	{
		H2F projYZ = new H2F("hist2D", nBinsY, minValY, maxValY, nBinsZ, minValZ, maxValZ);
		double sum = 0;
		for (int j = 0; j < nBinsY; j++)
		{
			for (int k = 0; k < nBinsZ; k++)
			{
				sum = 0;
				for (int i = 0; i < nBinsX; i++)
				{
					sum = sum + array3D[i][j][k];
				}
				projYZ.setBinContent(j, k, sum);
			}
		}
		return projYZ;
	}

	public H2F getZXProj()
	{
		H2F projZX = new H2F("hist2D", nBinsZ, minValZ, maxValZ, nBinsX, minValX, maxValX);
		double sum = 0;
		for (int k = 0; k < nBinsZ; k++)
		{
			for (int i = 0; i < nBinsX; i++)
			{
				sum = 0;
				for (int j = 0; j < nBinsY; j++)
				{
					sum = sum + array3D[i][j][k];
				}
				projZX.setBinContent(k, i, sum);
			}
		}
		return projZX;
	}

	/**
	 * Creates a 2-D Histogram slice of the specified x Bin
	 *
	 * @param xBin
	 *            the bin on the x axis to create a slice of
	 * @return a slice of the 3D histogram at the specified x bin as a 2-D histogram in y & z
	 */
	public H2F getSliceX(int xBin)
	{
		H2F sliceX = new H2F("hist2D", nBinsY, minValY, maxValY, nBinsZ, minValZ, maxValZ);
		for (int i = 0; i < nBinsY; i++)
		{
			for (int j = 0; j < nBinsZ; j++)
			{
				sliceX.setBinContent(i, j, this.getBinContent(xBin, i, j));
			}
		}
		return sliceX;
	}

	/**
	 * Creates a 2-D Histogram slice of the specified y Bin
	 *
	 * @param yBin
	 *            the bin on the y axis to create a slice of
	 * @return a slice of the 3D histogram at the specified y bin as a 2-D * histogram in x & z
	 */
	public H2F getSliceY(int yBin)
	{
		H2F sliceY = new H2F("hist2D", nBinsX, minValX, maxValX, nBinsZ, minValZ, maxValZ);
		for (int i = 0; i < nBinsX; i++)
		{
			for (int j = 0; j < nBinsZ; j++)
			{
				sliceY.setBinContent(i, j, this.getBinContent(i, yBin, j));
			}
		}
		return sliceY;
	}

	/**
	 * Creates a 2-D Histogram slice of the specified z Bin
	 *
	 * @param zBin
	 *            the bin on the z axis to create a slice of
	 * @return a slice of the 3D histogram at the specified z bin as a 2-D * histogram in x & y
	 */
	public H2F getSliceZ(int zBin)
	{
		H2F sliceZ = new H2F("hist2D", nBinsX, minValX, maxValX, nBinsY, minValY, maxValY);
		for (int i = 0; i < nBinsX; i++)
		{
			for (int j = 0; j < nBinsY; j++)
			{
				sliceZ.setBinContent(i, j, this.getBinContent(i, j, zBin));
			}
		}
		return sliceZ;
	}

	public int getNBinsX()
	{
		return this.nBinsX;
	}

	public int getNBinsY()
	{
		return this.nBinsY;
	}

	public int getNBinsZ()
	{
		return this.nBinsZ;
	}

	public double getBinWidthX()
	{
		return this.binWidthX;
	}

	public double getBinWidthY()
	{
		return this.binWidthY;
	}

	public double getBinWidthZ()
	{
		return this.binWidthZ;
	}

	// Here I will fill both a H2F object and a SimpleH3D object and will compare corresponding data
	// Following bin-by-bin comparison showed that there were slight differences in some bins where
	// the bin content was very big. for example, in bin(8,7) h2 and h3.projXY gave 1386 & 1383
	// values for the bin contents respectively. Likewise, in the next bin (8,8) it was 1242 and
	// 1239
	// respectively. I am guessing that it may be due to some rounding off effect while calculating
	// the bin numbers and events with (x,y) values falling extremely close to the bin margins may
	// fall in different bins based on whether we're using H2F or SimpleH3D class.
	public static void main(String[] args)
	{
		int nX = 20, nY = 15, nZ = 10;
		double minX = -5.0, maxX = 5.0, minY = 0.4, maxY = 7.6, minZ = -1.0, maxZ = 5.0;
		Random randomGenerator = new Random();
		double xx = 0.0, yy = 0.0, zz = 0.0;
		// H2F hist2d = new H2F("hist2d", 100, minX, maxX, 200, minY, maxY);
		H2F hist2d = new H2F("hist2d", nX, minX, maxX, nY, minY, maxY);
		SimpleH3D h3d = new SimpleH3D(nX, minX, maxX, nY, minY, maxY, nZ, minZ, maxZ);

		for (int i = 0; i < 50000; i++)
		{
			xx = randomGenerator.nextGaussian();
			yy = 4.0 + randomGenerator.nextGaussian();
			zz = 2.0 + randomGenerator.nextGaussian();
			hist2d.fill(xx, yy);
			h3d.fill(xx, yy, zz);
		}

		for (int i = 0; i < nX; i++)
		{
			for (int j = 0; j < nY; j++)
			{
				System.out.println(String.format("bin(%d,%d) h2=%2.1f  h3.projXY=%2.1f", i, j,
						hist2d.getBinContent(i, j), h3d.getXYProj().getBinContent(i, j)));
			}
		}

		JFrame frame = new JFrame("Basic GROOT Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		frame.setSize(1200, 1200);
		canvas.divide(2, 2);
		canvas.cd(0);
		hist2d.setTitleX("Randomly Generated Function");
		hist2d.setTitleY("Randomly Generated Function");
		canvas.getPad(0).setTitle("Histogram2D Demo");
		canvas.draw(hist2d);
		canvas.setFont("HanziPen TC");
		canvas.setTitleSize(32);
		canvas.setAxisTitleSize(24);
		canvas.setAxisLabelSize(18);
		canvas.setStatBoxFontSize(18);

		canvas.cd(1);
		canvas.draw(h3d.getXYProj());
		canvas.cd(2);
		canvas.draw(h3d.getYZProj());
		canvas.cd(3);
		canvas.draw(h3d.getZXProj());

		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
