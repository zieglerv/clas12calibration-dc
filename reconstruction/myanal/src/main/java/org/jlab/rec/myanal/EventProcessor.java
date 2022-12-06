/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.myanal;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class EventProcessor {
     
    public List<Particle> el          = new ArrayList();
    public List<Particle> piplus      = new ArrayList();
    public List<Particle> piminus     = new ArrayList();
    public List<Particle> Kplus       = new ArrayList();
    public List<Particle> Kminus      = new ArrayList();
    public List<Particle> gammas      = new ArrayList();
    public List<Particle> protons     = new ArrayList();
    public List<Particle> antiprotons = new ArrayList();
    public List<Particle> lambdas     = new ArrayList();
    public List<Particle> antilambdas = new ArrayList();
    
    public void readEvent(DataEvent event) {
        el.clear();
        Kplus.clear();
        Kminus.clear();
        piplus.clear();
        piminus.clear();
        gammas.clear();
        protons.clear();
        antiprotons.clear();
        lambdas.clear();
        antilambdas.clear();
        
        ArrayList<org.jlab.clas.physics.Particle> partGamma = new ArrayList();
        
        DataBank runConf = null; 
        DataBank recBankEB = null;
        DataBank recDeteEB = null; 
        if(event.hasBank("REC::Particle")) recBankEB = event.getBank("REC::Particle");
        if(event.hasBank("REC::Calorimeter")) recDeteEB = event.getBank("REC::Calorimeter");
        if(event.hasBank("RUN::config"))    runConf   = event.getBank("REC::Event");
        
        if(recBankEB!=null) {
            int nrows = recBankEB.rows();
            for(int loop = 0; loop < nrows; loop++){
                int pidCode = 0;
                if(recBankEB.getInt("pid", loop)!=0) pidCode = recBankEB.getInt("pid", loop);
                if(pidCode==0) continue;
                org.jlab.clas.physics.Particle recParticle = new org.jlab.clas.physics.Particle(
                                                pidCode,
                                                recBankEB.getFloat("px", loop),
                                                recBankEB.getFloat("py", loop),
                                                recBankEB.getFloat("pz", loop),
                                                recBankEB.getFloat("vx", loop),
                                                recBankEB.getFloat("vy", loop),
                                                recBankEB.getFloat("vz", loop));
                if(pidCode==22 && recDeteEB!=null) {
                    double energy1=0;
                    double energy4=0;
                    double energy7=0;
                    int    sector =0;
                    int  detector =0;
                    for(int j=0; j<recDeteEB.rows(); j++) {
                        if(recDeteEB.getShort("pindex",j)==loop && recDeteEB.getByte("detector",j)==DetectorType.ECAL.getDetectorId()) {
                            detector = recDeteEB.getByte("detector",j);
                            if(energy1 >= 0 && recDeteEB.getByte("layer",j) == 1) {
                                energy1 += recDeteEB.getFloat("energy",j);
                                sector = recDeteEB.getByte("sector",j);
                            }
                            if(energy4 >= 0 && recDeteEB.getByte("layer",j) == 4) energy4 += recDeteEB.getFloat("energy",j);
                            if(energy7 >= 0 && recDeteEB.getByte("layer",j) == 7) energy7 += recDeteEB.getFloat("energy",j);
                        }
                    }
                    recParticle.setProperty("energy1",energy1);
                    recParticle.setProperty("energy4",energy4);
                    recParticle.setProperty("energy7",energy7);
                    recParticle.setProperty("sector",sector*1.0);
                    recParticle.setProperty("detector",detector*1.0);
                    if(recParticle.charge()==0 
                            && recParticle.getProperty("detector")==DetectorType.ECAL.getDetectorId()) {
                        if(energy1>0.05 && energy4>0.0) {
                            double energy=(energy1+energy4+energy7)/0.245;
                            recParticle.setProperty("energy", energy);

                            gammas.add(new Particle(recParticle)); 
                        }
                    }
                }
                if(Math.abs(pidCode)==211 || Math.abs(pidCode)==321  || Math.abs(pidCode)==2212 || pidCode==11) {
                    
                    double beta = (double)recBankEB.getFloat("beta", loop);
                    double calcBeta = recParticle.p()/Math.sqrt(recParticle.p()*recParticle.p()
                            +recParticle.mass()*recParticle.mass());
                    double mass2   = Math.pow(recParticle.p()/beta, 2)-recParticle.p()*recParticle.p();
                   
                    int status = (int) Math.abs(recBankEB.getShort("status", loop));
                    double chi2pid = (double) Math.abs(recBankEB.getFloat("chi2pid", loop));
                    recParticle.setProperty("status", (double) status);
                    recParticle.setProperty("chi2pid", (double) chi2pid);
                    recParticle.setProperty("beta", (double) beta);
                    recParticle.setProperty("calcbeta", (double) calcBeta);
                    if(mass2<0) continue;
                    if(pidCode==11) {
                        recParticle.setProperty("mass", recParticle.mass()); }
                    else {
                        recParticle.setProperty("mass", (double) Math.sqrt(mass2));
                    } 
                    
                    if(Math.abs(beta-calcBeta)<0.20 && chi2pid<7 && Math.abs(Math.sqrt(mass2)-recParticle.mass())<0.0500) {
                        
                        Particle part = new Particle(recParticle);
                        
                        if(pidCode==11 && Math.abs(status)<4000)  { 
                            el.add(part); 
                        }
                        if(part.getCharge()==1 && 
                                pidCode==2212 )  {
                            protons.add(part);
                        }
                        if(part.getCharge()==-1 && 
                                pidCode==-2212 )  {
                            antiprotons.add(part);
                        }
                        if(part.getCharge()==1 && 
                                pidCode==211)  {
                            piplus.add(part);  
                        }
                        if(part.getCharge()==-1 && 
                                pidCode==-211)  {
                            piminus.add(part);
                        }
                        if(part.getCharge()==1 && 
                                pidCode==321)  {
                            Kplus.add(part);
                        }
                        if(part.getCharge()==-1 && 
                                pidCode==-321)  {
                            Kminus.add(part);
                        }
                    }
                }
            }
        }
    }
    
    
    boolean isFD(int status) {
        return Math.abs(status)/1000==2;
    }

    boolean isCD(int status) {
        return Math.abs(status)/1000==4;
    }

    public DataBank FillHeader(DataBank recRun) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
