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

import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.base.DataEvent;

public interface iProcessable
{
	public void processEvent(DataEvent event);
}
