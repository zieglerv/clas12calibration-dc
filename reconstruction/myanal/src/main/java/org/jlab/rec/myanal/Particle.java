/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.myanal;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Vector3;
import org.jlab.clas.reactions.DecayKinematics;
import org.jlab.geom.prim.Point3D;
import org.jlab.service.myanal.MyProcessor;

/**
 *
 * @author ziegler
 */
public class Particle {

    /**
     * @return the recParticle
     */
    public org.jlab.clas.physics.Particle getRecParticle() {
        return recParticle;
    }

    /**
     * @param recParticle the recParticle to set
     */
    public void setRecParticle(org.jlab.clas.physics.Particle recParticle) {
        this.recParticle = recParticle;
    }
    public boolean isUsed = false;
    private double _mass ;
    private double _recmass ;
    private double _E;
    private double _massConstrE;
    private double _px;
    private double _py;
    private double _pz;
    private double _pxcm;
    private double _pycm;
    private double _pzcm;
    private double _vx;
    private double _vy;
    private double _vz;
    private double _r; // distance of closest approach between 2 track helices
    private double _p;
    private double _pt;
    private double _beta;
    private double _calcBeta;
    private int _charge;
    private int _status;
    private int _Idx;
    private int _pid;
    public int vIndex=-1; 
    
    private List<Particle> _daughters;
    
    private org.jlab.clas.physics.Particle recParticle ;
    
    
    public Particle() {
        _daughters = new ArrayList<Particle>();
    }
    
    public void setParticle(org.jlab.clas.physics.Particle recPart) {
        _daughters = new ArrayList<Particle>();
        recParticle = recPart; 
        if(recParticle.pid()!=0 && org.jlab.clas.pdg.PDGDatabase.getParticleById(Math.abs(recParticle.pid()))!=null){
            if(recParticle.pid()<0) {
                this.setMass(PDGDatabase.getParticleById(-recParticle.pid()).mass());
            } else {
                this.setMass(PDGDatabase.getParticleById(recParticle.pid()).mass());
            }
        }
        this.setBeta(recParticle.getProperty("beta"));
        this.setCalcBeta(recParticle.getProperty("calcbeta"));
        this.setCharge(recParticle.charge());
        double massConstE = Math.sqrt(recParticle.p()*recParticle.p()
               +this.getMass()*this.getMass());
        
        this.setMassConstrE(massConstE);
        this.setRecMass(recParticle.getProperty("mass"));
        double E = Math.sqrt(recParticle.p()*recParticle.p()
               +this.getRecMass()*this.getRecMass());
        this.setE(E);
        this.setVx(recParticle.vx());
        this.setVy(recParticle.vy());
        this.setVz(recParticle.vz());
        this.setPx(recParticle.px());
        this.setPy(recParticle.py());
        this.setPz(recParticle.pz());
        this.setPt(Math.sqrt(recParticle.px()*recParticle.px()
                   +recParticle.py()*recParticle.py()));
        this.setP(Math.sqrt(recParticle.px()*recParticle.px()
                   +recParticle.py()*recParticle.py()
                   +recParticle.pz()*recParticle.pz()));
        this.setStatus((int) recParticle.getProperty("status"));
    }
    
    public Particle(org.jlab.clas.physics.Particle recPart) {
        this.setParticle(recPart);
        
    }

    public Particle(int idx, int pid, double e, double px, double py, double pz, 
            double ecm, double pxcm, double pycm, double pzcm,
            double vx, double vy, double vz, 
            int charge,double mass,
            int ndau, int dau1idx, int dau2idx, int dau3idx) {
        _daughters = new ArrayList<Particle>();
        this._Idx = idx;
        this._pid = pid;
        this._massConstrE = e;
        this._px = px;
        this._py = py;
        this._pz = pz;
        this._vx = vx;
        this._vy = vy;
        this._vz = vz;
        this._pxcm = pxcm;
        this._pycm = pycm;
        this._pzcm = pzcm;
        this._charge = charge;
        this._mass = mass;
        this._daughters = new ArrayList<Particle>();
        this.setP(Math.sqrt(_px*_px+_py*_py+_pz*_pz));
        this.setPt(Math.sqrt(_px*_px+_py*_py));
        for(int i =0; i< ndau; i++) {
            this._daughters.add(new Particle());
        }
        if(ndau>1) {
            this._daughters.get(0).setIdx(dau1idx);
            this._daughters.get(1).setIdx(dau2idx);
            if(ndau==3) {
                this._daughters.get(2).setIdx(dau3idx);
            } 
        }
    }
    
    public boolean combine(Particle part1, Particle part2, int pid) {
        double mLo= Double.NEGATIVE_INFINITY;
        double mHi= Double.POSITIVE_INFINITY;
        
        return this.combine(part1, part2, pid, mLo, mHi);
    }
    
    public boolean combine(Particle part1, Particle part2, int pid, double mLo, double mHi) {
        
        double Vx = 0.5*(part1.getVx()+part2.getVx());
        double Vy = 0.5*(part1.getVy()+part2.getVy());
        double Vz = 0.5*(part1.getVz()+part2.getVz());
        
        double Px = part1.getPx()+part2.getPx();
        double Py = part1.getPy()+part2.getPy();
        double Pz = part1.getPz()+part2.getPz();
        
        double mass = this.calcMass(part1, part2);
        
        if(mass < mLo || mass > mHi) 
            return false;
        
        org.jlab.clas.physics.Particle recParticle = new org.jlab.clas.physics.Particle(
                                                pid,
                                                Px,
                                                Py,
                                                Pz,
                                                Vx,
                                                Vy,
                                                Vz);
        double calcBeta = recParticle.p()/Math.sqrt(recParticle.p()*recParticle.p()
                            +recParticle.mass()*recParticle.mass());
       
        recParticle.setProperty("status", (double) 0);
        recParticle.setProperty("chi2pid", (double) 0);
        recParticle.setProperty("beta", (double) calcBeta);
        recParticle.setProperty("calcbeta", (double) calcBeta);
        recParticle.setProperty("mass", (double) mass);
        recParticle.setProperty("pid", pid);
        
        this.setRecParticle(recParticle);
        
        this.boost2CMFrm();
        part1.boost2CMFrm();
        part2.boost2CMFrm();
        
        this.setParticle(recParticle);
        this.setPid(pid);
        this.setCharge(part1.getCharge()+part2.getCharge());
        this._daughters.add(part1);
        this._daughters.add(part2);
        
        return true;
    }
    public void boost2CMFrm(org.jlab.clas.physics.Particle B, LorentzVector Bb) {
        Vector3 boost = Bb.boostVector();
        Vector3 boostm = new Vector3(-boost.x(), -boost.y(), -boost.z());

        LorentzVector  BP1 = new LorentzVector();
        BP1.copy(new LorentzVector(this.getPx(), this.getPy(), this.getPz(), this.getMassConstrE())); 
        BP1.boost(boostm);
      
        Vector3 partBoosted1 = new Vector3();
        partBoosted1.copy(BP1.vect());
        Vector3 partInFrame1 = DecayKinematics.vectorToFrame(B.vector().vect(), partBoosted1);
        
        this.setPxcm(partInFrame1.x());
        this.setPycm(partInFrame1.y());
        this.setPzcm(partInFrame1.z());
    }
    
    public void boost2CMFrm() {
        
        org.jlab.clas.physics.Particle B= new org.jlab.clas.physics.Particle(11, 0,0,MyProcessor.beamE+0.938, 0,0,0);
        LorentzVector  Bb = new LorentzVector();
        Bb.copy(B.vector());
        Bb.setPxPyPzE(0.,0., MyProcessor.beamE, MyProcessor.beamE+0.938);
        this.boost2CMFrm(B, Bb);
        
    }
    public double getECM() {
        double m = this.getMass();
        double px = this.getPxcm();
        double py = this.getPycm();
        double pz = this.getPzcm();
        
        return Math.sqrt(m*m+pz*px+py*py+pz*pz);
    }
    
    /**
     * @return the _Idx
     */
    public int getIdx() {
        return _Idx;
    }

    /**
     * @param _Idx the _Idx to set
     */
    public void setIdx(int _Idx) {
        this._Idx = _Idx;
    }
    /**
     * @return the _mass
     */
    public double getMass() {
        return _mass;
    }

    /**
     * @param _mass the _mass to set
     */
    public void setMass(double _mass) {
        this._mass = _mass;
    }
    
    /**
     * @return the _mass
     */
    public double getRecMass() {
        return _recmass;
    }

    /**
     * @param _recmass the _mass to set
     */
    public void setRecMass(double _mass) {
        this._recmass = _mass;
    }

    /**
     * @return the _pid
     */
    public int getPid() {
        return _pid;
    }

    /**
     * @param _pid the _pid to set
     */
    public void setPid(int _pid) {
        this._pid = _pid;
    }

    /**
     * @return the _E
     */
    public double getE() {
        return _E;
    }

    /**
     * @param _E the _E to set
     */
    public void setE(double _E) {
        this._E = _E;
    }

    /**
     * @return the _massConstrE
     */
    public double getMassConstrE() {
        return _massConstrE;
    }

    /**
     * @param _massConstrE the _massConstrE to set
     */
    public void setMassConstrE(double _massConstrE) {
        this._massConstrE = _massConstrE;
    }

    /**
     * @return the _px
     */
    public double getPx() {
        return _px;
    }

    /**
     * @param _px the _px to set
     */
    public void setPx(double _px) {
        this._px = _px;
    }

    /**
     * @return the _py
     */
    public double getPy() {
        return _py;
    }

    /**
     * @param _py the _py to set
     */
    public void setPy(double _py) {
        this._py = _py;
    }

    /**
     * @return the _pz
     */
    public double getPz() {
        return _pz;
    }

    /**
     * @param _pz the _pz to set
     */
    public void setPz(double _pz) {
        this._pz = _pz;
    }

    /**
     * @return the _vx
     */
    public double getVx() {
        return _vx;
    }

    /**
     * @param _vx the _vx to set
     */
    public void setVx(double _vx) {
        this._vx = _vx;
    }

    /**
     * @return the _vy
     */
    public double getVy() {
        return _vy;
    }

    /**
     * @param _vy the _vy to set
     */
    public void setVy(double _vy) {
        this._vy = _vy;
    }

    /**
     * @return the _vz
     */
    public double getVz() {
        return _vz;
    }

    /**
     * @param _vz the _vz to set
     */
    public void setVz(double _vz) {
        this._vz = _vz;
    }

    /**
     * @return the _r
     */
    public double getR() {
        return _r;
    }

    /**
     * @param _r the _r to set
     */
    public void setR(double _r) {
        this._r = _r;
    }

    /**
     * @return the _p
     */
    public double getP() {
        return _p;
    }

    /**
     * @param _p the _p to set
     */
    public void setP(double _p) {
        this._p = _p;
    }

    /**
     * @return the _pt
     */
    public double getPt() {
        return _pt;
    }

    /**
     * @param _pt the _pt to set
     */
    public void setPt(double _pt) {
        this._pt = _pt;
    }

    /**
     * @return the _pxcm
     */
    public double getPxcm() {
        return _pxcm;
    }

    /**
     * @param _pxcm the _pxcm to set
     */
    public void setPxcm(double _pxcm) {
        this._pxcm = _pxcm;
    }

    /**
     * @return the _pycm
     */
    public double getPycm() {
        return _pycm;
    }

    /**
     * @param _pycm the _pycm to set
     */
    public void setPycm(double _pycm) {
        this._pycm = _pycm;
    }

    /**
     * @return the _pzcm
     */
    public double getPzcm() {
        return _pzcm;
    }

    /**
     * @param _pzcm the _pzcm to set
     */
    public void setPzcm(double _pzcm) {
        this._pzcm = _pzcm;
    }

    /**
     * @return the _beta
     */
    public double getBeta() {
        return _beta;
    }

    /**
     * @param _beta the _beta to set
     */
    public void setBeta(double _beta) {
        this._beta = _beta;
    }

    /**
     * @return the _calcBeta
     */
    public double getCalcBeta() {
        return _calcBeta;
    }

    /**
     * @param _calcBeta the _calcBeta to set
     */
    public void setCalcBeta(double _calcBeta) {
        this._calcBeta = _calcBeta;
    }

    /**
     * @return the _charge
     */
    public int getCharge() {
        return _charge;
    }

    /**
     * @param _charge the _charge to set
     */
    public void setCharge(int _charge) {
        this._charge = _charge;
    }

    /**
     * @return the _status
     */
    public int getStatus() {
        return _status;
    }

    /**
     * @param _status the _status to set
     */
    public void setStatus(int _status) {
        this._status = _status;
    }

    /**
     * @return the _daughters
     */
    public List<Particle> getDaughters() {
        return _daughters;
    }

    /**
     * @param _daughters the _daughters to set
     */
    public void setDaughters(List<Particle> _daughters) {
        this._daughters = _daughters;
    }
    
    public boolean isFT() {
        return Math.abs(this.getStatus())/1000==1;
    }

    public boolean isFD() {
        return Math.abs(this.getStatus())/1000==2;
    }

    public boolean isCD() {
        return Math.abs(this.getStatus())/1000==4;
    }

    private double calcMass(Particle part1, Particle part2) {
        double E1 = part1.getMassConstrE();
        double E2 = part2.getMassConstrE();
        //double E1 = part1.getE();
        //double E2 = part2.getE();
        double Px = part1.getPx()+part2.getPx();
        double Py = part1.getPy()+part2.getPy();
        double Pz = part1.getPz()+part2.getPz();
        
        double E = E1+E2;
        return Math.sqrt(E*E - Px*Px-Py*Py-Pz*Pz);
        
    }

}
