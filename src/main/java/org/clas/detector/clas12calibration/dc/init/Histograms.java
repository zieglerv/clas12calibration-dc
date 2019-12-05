/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel, kpadhikari
 *  `------'
*/
package org.clas.detector.clas12calibration.dc.init;

import java.util.HashMap;
import java.util.Map;

import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

public class Histograms 
{
	public static Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
	public static Map<Coordinate, H1F> htrkDoca = new HashMap<Coordinate, H1F>();

	public static Map<Coordinate, H1F> h1ThSL = new HashMap<Coordinate, H1F>();
	public static Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();
	// Histograms to get ineff. as fn of trkDoca (NtrkDoca = trkDoca/docaMax)
	public static Map<Coordinate, H1F> h1trkDoca2Dar = new HashMap<Coordinate, H1F>(); // #############################################################
	public static Map<Coordinate, H1F> h1NtrkDoca2Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
	public static Map<Coordinate, H1F> h1NtrkDocaP2Dar = new HashMap<Coordinate, H1F>();// ############################################################
	public static Map<Coordinate, H1F> h1trkDoca3Dar = new HashMap<Coordinate, H1F>(); // ############################################################
	public static Map<Coordinate, H1F> h1NtrkDoca3Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
	public static Map<Coordinate, H1F> h1NtrkDocaP3Dar = new HashMap<Coordinate, H1F>();// ############################################################
	public static Map<Coordinate, H1F> h1trkDoca4Dar = new HashMap<Coordinate, H1F>();
	public static Map<Coordinate, H1F> h1wire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
	public static Map<Coordinate, H1F> h1avgWire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
	public static Map<Coordinate, H1F> h1fitChisqProbSeg4Dar = new HashMap<Coordinate, H1F>();
	public static Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
	public static Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();

	private Histograms() {}

}
