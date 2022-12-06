/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.myanal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class Reader {
    private Map<Integer, Particle> parts = new HashMap<>();
    private Map<Integer, Particle> daus = new HashMap<>();
    public static boolean useMCTruth = true;
    public boolean iseDetected = false;
   
    public void readDataBanks(DataEvent event, double zTar) {
        getParts().clear();
        getDaus().clear();
        
        DataBank runConf = null; 
        DataBank recBankEB = null;
        DataBank recDeteEB = null; 
        DataBank vertBankEB = null;
        DataBank mcBank = null;
        
        int ev = 0;
        if(event.hasBank("REC::Particle")) recBankEB = event.getBank("REC::Particle");
        if(event.hasBank("REC::Calorimeter")) recDeteEB = event.getBank("REC::Calorimeter");
        if(event.hasBank("REC::VertDoca")) vertBankEB = event.getBank("REC::VertDoca");
        if(event.hasBank("MC::Particle")) mcBank = event.getBank("MC::Lund");
        Decay.setVertBank(vertBankEB);        
        
        if(event.hasBank("RUN::config")) {   
            runConf   = event.getBank("RUN::config"); 
            ev = runConf.getInt("event", 0);
        }
        
        
        
        
        
        double vzmc1 = 0;
        double pxmc1 = 0;
        double pymc1 = 0;
        double pzmc1 = 0;
        double pmc1 = 0;
        double phimc1 = 0;
        double thetamc1 = 0;
        
        double vzmc2 = 0;
        double pxmc2 = 0;
        double pymc2 = 0;
        double pzmc2 = 0;
        double pmc2 = 0;
        double phimc2 = 0;
        double thetamc2 = 0;
        
        for(int loopm = 0; loopm < mcBank.rows()-1; loopm++){
            double vz = (double) mcBank.getFloat("vz", loopm);
            int mcpid = mcBank.getInt("pid", loopm);
            double vz1 = (double) mcBank.getFloat("vz", loopm+1);
            int mcpid1 = mcBank.getInt("pid", loopm+1);
            int parIdx = mcBank.getInt("parent", loopm);
            int parIdx1 = mcBank.getInt("parent", loopm);
            if(vz!=0 && vz==vz1 && mcpid==2212 && mcpid1==-211 && parIdx==parIdx1 &&
                    mcBank.getInt("pid", parIdx-1)==3122) {
                vzmc1 = (double) mcBank.getFloat("vz", loopm)+zTar; System.out.println(" vzmc1 "+vzmc1);
                pxmc1 = mcBank.getFloat("px", loopm);
                pymc1 = mcBank.getFloat("py", loopm);
                pzmc1 = mcBank.getFloat("pz", loopm);
                pmc1 = Math.sqrt(pxmc1*pxmc1+pymc1*pymc1+pzmc1*pzmc1);
                phimc1 = Math.atan2(pymc1,pxmc1);
                thetamc1 = Math.acos(pzmc1/pmc1);
                
                vzmc2 = (double) mcBank.getFloat("vz", loopm+1)+zTar; System.out.println(" vzmc2 "+vzmc1);
                pxmc2 = mcBank.getFloat("px", loopm+1);
                pymc2 = mcBank.getFloat("py", loopm+1);
                pzmc2 = mcBank.getFloat("pz", loopm+1);
                pmc2 = Math.sqrt(pxmc2*pxmc2+pymc2*pymc2+pzmc2*pzmc2);
                phimc2 = Math.atan2(pymc2,pxmc2+1);
                thetamc2 = Math.acos(pzmc2/pmc2+1);
            }
        }
        
        
        if(recBankEB!=null) {
            int nrows = recBankEB.rows();
            for(int loop = 0; loop < nrows; loop++){
                int pidCode = 0;
                if(recBankEB.getInt("pid", loop)!=0) pidCode = this.getPidCode(mcBank, recBankEB, loop);
                
                if(this.passEvent==false) return;
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
                            getDaus().put(loop+1, new Particle(recParticle)); 
                            getDaus().get(loop+1).setIdx(loop+1);
                            getDaus().get(loop+1).setPid(pidCode);
                        }
                    }
                }
                if(recBankEB.getInt("pid", loop)==11)
                    this.iseDetected = true;
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
                    
                    //if(Math.abs(beta-calcBeta)<0.20 && chi2pid<7 && Math.abs(Math.sqrt(mass2)-recParticle.mass())<0.0500) {
                    if(chi2pid<7 ) {    
                        
                        Particle part = new Particle(recParticle);
                        
                        if(pidCode==11 && Math.abs(status)<4000)  { 
                            getDaus().put(loop+1, new Particle(recParticle)); 
                            getDaus().get(loop+1).setIdx(loop+1);
                            getDaus().get(loop+1).setPid(pidCode);
                        }
                        if(Math.abs(part.getCharge())==1 && 
                               ( Math.abs(pidCode)==2212 || Math.abs(pidCode)==211 || Math.abs(pidCode)==321 )  ) {
                            getDaus().put(loop+1, new Particle(recParticle)); 
                            getDaus().get(loop+1).setIdx(loop+1);
                            getDaus().get(loop+1).setPid(pidCode);
                            System.out.println("PID "+pidCode);
                            
                            if(mcBank!=null && useMCTruth) {
                                if(getDaus().get(loop+1)!=null) { System.out.println("PID check "+pidCode);
                                    Swim swim1 = new Swim();    
                                    int q = getDaus().get(loop+1).getCharge();
                                    double px = recBankEB.getFloat("px", loop);
                                    double py = recBankEB.getFloat("py", loop);
                                    double pz = recBankEB.getFloat("pz", loop);
                                    double vx = recBankEB.getFloat("vx", loop);
                                    double vy = recBankEB.getFloat("vy", loop);
                                    double vz = recBankEB.getFloat("vz", loop);

                                    double p = Math.sqrt(px*px+py*py+pz*pz);
                                    double phi = Math.atan2(py,px);
                                    double theta = Math.acos(pz/p);
                                    swim1.SetSwimParameters(vx, vy, vz, px, py, pz, q);
                                    double[] tr1 = swim1.SwimRho(20., 1); // get outmost traj point instead
                                    swim1.SetSwimParameters(tr1[0], tr1[1], tr1[2], -tr1[3], -tr1[4], -tr1[5], -q);
                                    swim1.stepSize= 50.00* 1.e-6; // 500 microns

                                    
                                    if(getDaus().get(loop+1).getPid() ==2212   ) {
                                        System.out.println(getDaus().get(loop+1).getPid()+"] swim to  "+vzmc1);
                                        tr1 = swim1.SwimToPlaneLab(vzmc1);
                                        getDaus().get(loop+1).setVx(tr1[0]);
                                        getDaus().get(loop+1).setVy(tr1[1]);
                                        getDaus().get(loop+1).setVz(tr1[2]);
                                        getDaus().get(loop+1).setPx(-tr1[3]);
                                        getDaus().get(loop+1).setPy(-tr1[4]);
                                        getDaus().get(loop+1).setPz(-tr1[5]);
            //                                    getDaus().get(loop+1).setVx(mcBank.getFloat("vx", loopm));
            //                                    getDaus().get(loop+1).setVy(mcBank.getFloat("vy", loopm));
            //                                    getDaus().get(loop+1).setVz(mcBank.getFloat("vz", loopm));
            //                                    getDaus().get(loop+1).setPx(mcBank.getFloat("px", loopm));
            //                                    getDaus().get(loop+1).setPy(mcBank.getFloat("py", loopm));
            //                                    getDaus().get(loop+1).setPz(mcBank.getFloat("pz", loopm));
            //                                    getDaus().get(loop+1).setP(Math.sqrt(getDaus().get(loop+1).getPx()*getDaus().get(loop+1).getPx()
            //                                            +getDaus().get(loop+1).getPy()*getDaus().get(loop+1).getPy()
            //                                            +getDaus().get(loop+1).getPz()*getDaus().get(loop+1).getPz()));
            //                                    
                                        double massConstE = Math.sqrt(getDaus().get(loop+1).getP()*getDaus().get(loop+1).getP()+
                                                getDaus().get(loop+1).getMass()*getDaus().get(loop+1).getMass());
                                        getDaus().get(loop+1).setMassConstrE(massConstE);

                                    }
                                    if(getDaus().get(loop+1).getPid() ==-211    ) {
                                        System.out.println(getDaus().get(loop+1).getPid()+"] swim to  "+vzmc1);
                                        tr1 = swim1.SwimToPlaneLab(vzmc1);
                                        getDaus().get(loop+1).setVx(tr1[0]);
                                        getDaus().get(loop+1).setVy(tr1[1]);
                                        getDaus().get(loop+1).setVz(tr1[2]);
                                        getDaus().get(loop+1).setPx(-tr1[3]);
                                        getDaus().get(loop+1).setPy(-tr1[4]);
                                        getDaus().get(loop+1).setPz(-tr1[5]);
            //                                    getDaus().get(loop+1).setVx(mcBank.getFloat("vx", loopm));
            //                                    getDaus().get(loop+1).setVy(mcBank.getFloat("vy", loopm));
            //                                    getDaus().get(loop+1).setVz(mcBank.getFloat("vz", loopm));
            //                                    getDaus().get(loop+1).setPx(mcBank.getFloat("px", loopm));
            //                                    getDaus().get(loop+1).setPy(mcBank.getFloat("py", loopm));
            //                                    getDaus().get(loop+1).setPz(mcBank.getFloat("pz", loopm));
            //                                    getDaus().get(loop+1).setP(Math.sqrt(getDaus().get(loop+1).getPx()*getDaus().get(loop+1).getPx()
            //                                            +getDaus().get(loop+1).getPy()*getDaus().get(loop+1).getPy()
            //                                            +getDaus().get(loop+1).getPz()*getDaus().get(loop+1).getPz()));
            //                                    
                                        double massConstE = Math.sqrt(getDaus().get(loop+1).getP()*getDaus().get(loop+1).getP()+
                                                getDaus().get(loop+1).getMass()*getDaus().get(loop+1).getMass());
                                        getDaus().get(loop+1).setMassConstrE(massConstE);

                                    }
                                }
                            }
                        }
                    }
                }
                
            }
        }
        
    }
   
    public void readAnalBanks(DataEvent de) {
        getParts().clear();
        getDaus().clear();
        DataBank partBank = null;
        if(de.hasBank("ANAL::Particle")) partBank = de.getBank("ANAL::Particle");
        if(partBank == null) 
            return;
       
        
        for(int i=0; i<partBank.rows(); i++) {
            int ev = partBank.getInt("event", i);
            int idx = partBank.getShort("idx",i);
            int pid = partBank.getInt("pid",i);
            double e = partBank.getFloat("e",i);
            double px = partBank.getFloat("px",i);
            double py = partBank.getFloat("py",i);
            double pz = partBank.getFloat("pz",i);
            double ecm = partBank.getFloat("ecm",i);
            double pxcm = partBank.getFloat("pxcm",i);
            double pycm = partBank.getFloat("pycm",i);
            double pzcm = partBank.getFloat("pzcm",i);
            double vx = partBank.getFloat("vx",i);
            double vy = partBank.getFloat("vy",i);
            double vz = partBank.getFloat("vz",i);
            double r = partBank.getFloat("r",i);
            int charge = partBank.getByte("charge",i);
            double mass = partBank.getFloat("mass",i);
            int ndau = partBank.getByte("ndau",i);
            int dau1idx = partBank.getShort("dau1idx",i);
            int dau2idx = partBank.getShort("dau2idx",i);
            int dau3idx = partBank.getShort("dau3idx",i);
            
            Particle part = new Particle( idx,  pid,  e,  px,  py,  pz, 
             ecm,  pxcm,  pycm,  pzcm,
             vx,  vy,  vz, 
             charge, mass,
             ndau,  dau1idx,  dau2idx,  dau3idx);
            if(ndau==0)
                getDaus().put(idx, part);
            if(ndau>1)
                getParts().put(idx, part);
        }
        Set entrySet = getParts().entrySet();
        Iterator it = entrySet.iterator();

        while(it.hasNext()){
           Map.Entry me = (Map.Entry)it.next();
           Particle hpart = (Particle) me.getValue();
           int nd = hpart.getDaughters().size();
           int d1x = hpart.getDaughters().get(0).getIdx();
           int d2x = hpart.getDaughters().get(1).getIdx();
           int d3x = -1;
           if(nd == 3) 
               d3x = hpart.getDaughters().get(2).getIdx();
           hpart.getDaughters().clear();
           hpart.getDaughters().add(getDaus().get(d1x));
           hpart.getDaughters().add(getDaus().get(d2x));
           if(nd == 3) 
               hpart.getDaughters().add(getDaus().get(d3x));
           
        }
    }

    /**
     * @return the parts
     */
    public Map<Integer, Particle> getParts() {
        return parts;
    }

    /**
     * @param parts the parts to set
     */
    public void setParts(Map<Integer, Particle> parts) {
        this.parts = parts;
    }

    /**
     * @return the daus
     */
    public Map<Integer, Particle> getDaus() {
        return daus;
    }

    /**
     * @param daus the daus to set
     */
    public void setDaus(Map<Integer, Particle> daus) {
        this.daus = daus;
    }
    boolean passEvent = false;
    private int getPidCode(DataBank mcBank, DataBank recBankEB, int loop) {
        int pid = recBankEB.getInt("pid", loop);
        if(mcBank!=null) {
            for(int loopm = 0; loopm < mcBank.rows(); loopm++){
                if(mcBank.getInt("pid", loopm) == 3122)
                    passEvent = true;
            }
        }
//        if(mcBank!=null) {
//            double px = recBankEB.getFloat("px", loop);
//            double py = recBankEB.getFloat("py", loop);
//            double pz = recBankEB.getFloat("pz", loop);
//            double p = Math.sqrt(px*px+py*py+pz*pz);
//            double phi = Math.atan2(py,px);
//            double theta = Math.acos(pz/p);
//            for(int loopm = 0; loopm < mcBank.rows(); loopm++){
//                if(getDaus().get(loop+1)!=null) {
//                    double pxmc = mcBank.getFloat("px", loopm);
//                    double pymc = mcBank.getFloat("py", loopm);
//                    double pzmc = mcBank.getFloat("pz", loopm);
//                    double pmc = Math.sqrt(pxmc*pxmc+pymc*pymc+pzmc*pzmc);
//                    double phimc = Math.atan2(pymc,pxmc);
//                    double thetamc = Math.acos(pzmc/pmc);
//                    if(Math.abs(pmc-p)/pmc<5*0.05 
//                            && Math.abs(phimc-phi)<5*0.005
//                            && Math.abs(thetamc-theta)<5*0.0010) {
//                        pid = (int) mcBank.getInt("pid", loopm);
//                    }
//                }  
//            }
//        }
        return pid;
    }
    
}
