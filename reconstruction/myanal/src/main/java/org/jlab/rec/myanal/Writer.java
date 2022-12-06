/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.myanal;

import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class Writer {
    
    public static DataBank fillBank(DataEvent event, List<Particle> partlist, String bankName) {
        if (partlist == null || partlist.isEmpty()) return null;
        
        int bankSize = 0;
        for(Particle p : partlist) {
            bankSize++;
            int nDau = p.getDaughters().size();
            bankSize+=nDau;
        }
        bankName = "ANAL::Particle";
        DataBank partBank = event.createBank(bankName, bankSize);
        int ev = event.getBank("RUN::config").getInt("event", 0); 
        int i = -1;
        for (int ii = 0; ii < partlist.size(); ii++) {
            i++;
            partBank.setInt("event", i, ev);
            partBank.setShort("idx",i, (short) (100+ii));
            partBank.setInt("pid", i, partlist.get(ii).getPid());
            partBank.setFloat("e",i, (float) partlist.get(ii).getMassConstrE());
            partBank.setFloat("px",i, (float) partlist.get(ii).getPx());
            partBank.setFloat("py",i, (float) partlist.get(ii).getPy());
            partBank.setFloat("pz",i, (float) partlist.get(ii).getPz());
            partBank.setFloat("ecm",i, (float) partlist.get(ii).getECM());
            partBank.setFloat("pxcm",i, (float) partlist.get(ii).getPxcm());
            partBank.setFloat("pycm",i, (float) partlist.get(ii).getPycm());
            partBank.setFloat("pzcm",i, (float) partlist.get(ii).getPzcm());
            partBank.setFloat("vx",i, (float) partlist.get(ii).getVx());
            partBank.setFloat("vy",i, (float) partlist.get(ii).getVy());
            partBank.setFloat("vz",i, (float) partlist.get(ii).getVz());
            partBank.setFloat("r",i, (float) partlist.get(ii).getR());
            partBank.setByte("charge", i, (byte) partlist.get(ii).getCharge());
            partBank.setFloat("mass",i, (float) partlist.get(ii).getRecMass());
            partBank.setByte("ndau",i, (byte) partlist.get(ii).getDaughters().size());
            if(partlist.get(ii).getDaughters().size()>0) {
                String ds;
                for(int j =0; j<partlist.get(ii).getDaughters().size(); j++) {
                    ds = "dau";
                    ds+=j+1;
                    ds+="idx";
                    partBank.setShort(ds,i, (short) partlist.get(ii).getDaughters().get(j).getIdx());
                }
                for(int j =0; j<partlist.get(ii).getDaughters().size(); j++) {
                    i++;
                    partBank.setInt("event", i, ev);
                    partBank.setShort("idx",i, (short) partlist.get(ii).getDaughters().get(j).getIdx());
                    partBank.setInt("pid", i, partlist.get(ii).getDaughters().get(j).getPid());
                    partBank.setFloat("e",i, (float) partlist.get(ii).getDaughters().get(j).getMassConstrE());
                    partBank.setFloat("px",i, (float) partlist.get(ii).getDaughters().get(j).getPx());
                    partBank.setFloat("py",i, (float) partlist.get(ii).getDaughters().get(j).getPy());
                    partBank.setFloat("pz",i, (float) partlist.get(ii).getDaughters().get(j).getPz());
                    partBank.setFloat("ecm",i, (float) partlist.get(ii).getDaughters().get(j).getECM());
                    partBank.setFloat("pxcm",i, (float) partlist.get(ii).getDaughters().get(j).getPxcm());
                    partBank.setFloat("pycm",i, (float) partlist.get(ii).getDaughters().get(j).getPycm());
                    partBank.setFloat("pzcm",i, (float) partlist.get(ii).getDaughters().get(j).getPzcm());
                    partBank.setFloat("vx",i, (float) partlist.get(ii).getDaughters().get(j).getVx());
                    partBank.setFloat("vy",i, (float) partlist.get(ii).getDaughters().get(j).getVy());
                    partBank.setFloat("vz",i, (float) partlist.get(ii).getDaughters().get(j).getVz());
                    partBank.setByte("charge", i, (byte) partlist.get(ii).getDaughters().get(j).getCharge());
                    partBank.setFloat("mass",i, (float) partlist.get(ii).getDaughters().get(j).getRecMass());
                    partBank.setByte("ndau",i, (byte) partlist.get(ii).getDaughters().get(j).getDaughters().size());
                }
            }  
        }
        partBank.show();
        return partBank;

    }
    
    public static DataBank fillBankDebug(DataEvent event, List<Particle> partlist, String bankName) {
        if (partlist == null || partlist.isEmpty()) return null;
        
        int bankSize = 0;
        for(Particle p : partlist) {
            bankSize++;
            int nDau = p.getDaughters().size();
            bankSize+=nDau;
        }
        bankName = "ANAL::Particle";
        DataBank partBank = event.createBank(bankName, bankSize);
        int ev = event.getBank("RUN::config").getInt("event", 0); 
        int i = 0;
        for (int ii = 0; ii < partlist.size(); ii++) {
            
            partBank.setInt("event", i, ev);
            partBank.setShort("idx",i, (short) (100+ii));
            partBank.setInt("pid", i, partlist.get(ii).getPid());
            partBank.setFloat("e",i, (float) partlist.get(ii).getMassConstrE());
            partBank.setFloat("px",i, (float) partlist.get(ii).getPx());
            partBank.setFloat("py",i, (float) partlist.get(ii).getPy());
            partBank.setFloat("pz",i, (float) partlist.get(ii).getPz());
            partBank.setFloat("ecm",i, (float) partlist.get(ii).getECM());
            partBank.setFloat("pxcm",i, (float) partlist.get(ii).getPxcm());
            partBank.setFloat("pycm",i, (float) partlist.get(ii).getPycm());
            partBank.setFloat("pzcm",i, (float) partlist.get(ii).getPzcm());
            partBank.setFloat("vx",i, (float) partlist.get(ii).getVx());
            partBank.setFloat("vy",i, (float) partlist.get(ii).getVy());
            partBank.setFloat("vz",i, (float) partlist.get(ii).getVz());
            partBank.setFloat("r",i, (float) partlist.get(ii).getR());
            partBank.setByte("charge", i, (byte) partlist.get(ii).getCharge());
            partBank.setFloat("mass",i, (float) partlist.get(ii).getRecMass());
            partBank.setByte("ndau",i, (byte) partlist.get(ii).getDaughters().size());
            if(partlist.get(ii).getDaughters().size()>0) {
                String ds;
                for(int j =0; j<partlist.get(ii).getDaughters().size(); j++) {
                    ds = "dau";
                    ds+=j+1;
                    ds+="idx";
                    partBank.setShort(ds,i, (short) partlist.get(ii).getDaughters().get(j).getIdx());
                }
                for(int j =0; j<partlist.get(ii).getDaughters().size(); j++) {
                    i++;
                    partBank.setInt("event", i, ev);
                    partBank.setShort("idx",i, (short) partlist.get(ii).getDaughters().get(j).getIdx());
                    partBank.setInt("pid", i, partlist.get(ii).getDaughters().get(j).getPid());
                    partBank.setFloat("e",i, (float) partlist.get(ii).getDaughters().get(j).getMassConstrE());
                    partBank.setFloat("px",i, (float) partlist.get(ii).getDaughters().get(j).getPx());
                    partBank.setFloat("py",i, (float) partlist.get(ii).getDaughters().get(j).getPy());
                    partBank.setFloat("pz",i, (float) partlist.get(ii).getDaughters().get(j).getPz());
                    partBank.setFloat("ecm",i, (float) partlist.get(ii).getDaughters().get(j).getECM());
                    partBank.setFloat("pxcm",i, (float) partlist.get(ii).getDaughters().get(j).getPxcm());
                    partBank.setFloat("pycm",i, (float) partlist.get(ii).getDaughters().get(j).getPycm());
                    partBank.setFloat("pzcm",i, (float) partlist.get(ii).getDaughters().get(j).getPzcm());
                    partBank.setFloat("vx",i, (float) partlist.get(ii).getDaughters().get(j).getVx());
                    partBank.setFloat("vy",i, (float) partlist.get(ii).getDaughters().get(j).getVy());
                    partBank.setFloat("vz",i, (float) partlist.get(ii).getDaughters().get(j).getVz());
                    partBank.setByte("charge", i, (byte) partlist.get(ii).getDaughters().get(j).getCharge());
                    partBank.setFloat("mass",i, (float) partlist.get(ii).getDaughters().get(j).getRecMass());
                    partBank.setByte("ndau",i, (byte) partlist.get(ii).getDaughters().get(j).getDaughters().size());
                }
            }  
        }
        //partBank.show();
        return partBank;

    }

    
}
