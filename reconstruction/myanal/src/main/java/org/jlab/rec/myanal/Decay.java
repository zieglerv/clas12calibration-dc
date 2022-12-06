/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.myanal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.rec.vtx.TrackParsHelix;
import org.jlab.rec.vtx.Vertex;
import org.jlab.rec.vtx.VertexFinder;
import org.jlab.service.myanal.MyProcessor;

/**
 *
 * @author ziegler
 */
public class Decay extends Particle {
    
    private int _parPID;
    private int _dau1PID;
    private int _dau2PID;
    private int _dau3PID;
    
    private double _loMassCut;
    private double _hiMassCut;
    
    private List<Particle> _daughters;
    private List<Particle> _particles;
    
    private Map<Integer, List<Particle>> listsByPID = new HashMap<>();
    private static DataBank vertBank;
    
    public Decay(int parPID, int dau1PID, int dau2PID, int dau3PID, double loMassCut, double hiMassCut,
            List<Particle> daughters, double xb, double yb, Swim swimmer) {
        this.setSwimmer(swimmer);
        _parPID = parPID;
        _dau1PID = dau1PID;
        _dau2PID = dau2PID;
        _dau3PID = dau3PID;
        _loMassCut = loMassCut;
        _hiMassCut = hiMassCut;
        
        _daughters = daughters;
        
        this.setxB(xb);
        this.setyB(yb);
        
        this.sortByPID(daughters, dau1PID, dau2PID, dau3PID);
        List<Particle> list1 = new ArrayList<>();
        List<Particle> list2 = new ArrayList<>();
        List<Particle> list3 = new ArrayList<>();
        
        if(listsByPID!=null) { 
            if(listsByPID.containsKey(dau1PID) && dau1PID!=0)
                list1.addAll(listsByPID.get(dau1PID));
            if(listsByPID.containsKey(dau2PID) && dau2PID!=0)
                list2.addAll(listsByPID.get(dau2PID));
            if(listsByPID.containsKey(dau3PID) && dau3PID!=0)
                list3.addAll(listsByPID.get(dau3PID));
            if(!list1.isEmpty() && !list2.isEmpty()) {
                _particles = new ArrayList<>();
                if(list3.isEmpty()) {
                    for(Particle part1 : list1) {
                        for(Particle part2 : list2) { 
                            Particle part = new Particle();
                            if(part1.getCharge()!=0 && part2.getCharge()!=0 ) {
                                    if(this.checkVertex(part1,part2)==false)
                                        continue;
                            }
                            if(part.combine(part1, part2, parPID, loMassCut, hiMassCut)) { 
                                if(part1.getCharge()!=0 && part2.getCharge()==0) {
                                    vx = part1.getVx();
                                    vy = part1.getVy();
                                    vz = part1.getVz();
                                } else if(part2.getCharge()!=0 && part1.getCharge()==0) {
                                    vx = part2.getVx();
                                    vy = part2.getVy();
                                    vz = part2.getVz();
                                } else if(part2.getCharge()!=0 && part1.getCharge()==0) {
                                    vx = 0;
                                    vy = 0;
                                    vz = 0;
                                }
                                part1.isUsed = true;
                                part2.isUsed = true;
                                part.setVx(vx);
                                part.setVy(vy);
                                part.setVz(vz);
                                part.setR(r);
                                _particles.add(part);
                            }
                        }
                    }
                    //this.resolveCombinatorials2();
                } else {
                    for(Particle part1 : list1) {
                        for(Particle part2 : list2) {
                            for(Particle part3 : list3) {
                                Particle part = new Particle();
                            //if(part.combine(part1, part2, part3, parPID, hiMassCut, hiMassCut))
                            //    _particles.add(part);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sortByPID(List<Particle> daughters, int dau1PID, int dau2PID, int dau3PID) {
        listsByPID.clear();
        for(Particle par : daughters) { 
            if(par.getPid()==dau1PID || par.getPid()==dau2PID || par.getPid()==dau3PID) {
                if(listsByPID.containsKey(par.getPid())) {
                    listsByPID.get(par.getPid()).add(par);
                } else {
                    listsByPID.put(par.getPid(), new ArrayList<Particle>());
                    listsByPID.get(par.getPid()).add(par);
                }
            }
        }
    }

    /**
     * @return the _particles
     */
    public List<Particle> getParticles() {
        return _particles;
    }

    /**
     * @param _particles the _particles to set
     */
    public void setParticles(List<Particle> _particles) {
        this._particles = _particles;
    }

    private void resolveCombinatorials2() {
        if(r==999) 
            return;

        int j = 99;
        for(Particle p : _particles) {
            p.setIdx(j++);
        }
        List<Particle> overl = new ArrayList<>();
        for(Particle p1 : _particles) {
            for(Particle p2 : _particles) {
                if(p1.getIdx()==p2.getIdx())
                    continue;
                if(p1.getDaughters().get(0).getIdx()==p2.getDaughters().get(0).getIdx() || 
                        p1.getDaughters().get(1).getIdx()==p2.getDaughters().get(1).getIdx() ) {
                    overl.add(this.reject(p1,p2));
                }
            }
        }
        _particles.removeAll(overl);
        
    }

    private Particle reject(Particle p1, Particle p2) {
        if(p1.getR()<=p2.getR()) {
            return p1;
        } else {
            return p2;
        }
    }

    /**
     * @return the vertBank
     */
    public DataBank getVertBank() {
        return vertBank;
    }

    /**
     * @param vertBank the vertBank to set
     */
    public static void setVertBank(DataBank vertBank) {
        Decay.vertBank = vertBank;
    }
    private Swim swimmer;
    private double xB;
    private double yB;
    
    private double r = 999;
    private double vx =999;
    private double vy =999;
    private double vz =999;
    
    private boolean checkVertex(Particle p1, Particle p2) {
        if(getVertBank()!=null && MyProcessor.useVtxBank) {
            int nrows2 = getVertBank().rows();
            for(int loop2 = 0; loop2 < nrows2; loop2++){
                int index1 = (int) getVertBank().getShort("index1", loop2);
                int index2 = (int) getVertBank().getShort("index2", loop2);

                if(p1.getIdx()-1==index1 || p1.getIdx()-1==index2)
                    p1.vIndex=loop2;
                if(p2.getIdx()-1==index1 || p2.getIdx()-1==index2)
                    p2.vIndex=loop2;
                if(p1.vIndex==p2.vIndex) 
                    loop2 = nrows2;
            }
            if(p1.vIndex==-1 || p2.vIndex==-1 || p1.vIndex!=p2.vIndex) 
                return false;
            r =  (double) getVertBank().getFloat("r", p1.vIndex);
            vx = (double) getVertBank().getFloat("x", p1.vIndex);
            vy = (double) getVertBank().getFloat("y", p1.vIndex);
            vz = (double) getVertBank().getFloat("z", p1.vIndex);
            
            return true;
        } else { 
            this.computeVertex(p1, p2); 
            if(r<0.5) { 
                p1.vIndex = 999;
                p2.vIndex = 999;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @return the swimmer
     */
    public Swim getSwimmer() {
        return swimmer;
    }

    /**
     * @param swimmer the swimmer to set
     */
    public void setSwimmer(Swim swimmer) {
        this.swimmer = swimmer;
    }

    /**
     * @return the xB
     */
    public double getxB() {
        return xB;
    }

    /**
     * @param xB the xB to set
     */
    public void setxB(double xB) {
        this.xB = xB;
    }

    /**
     * @return the yB
     */
    public double getyB() {
        return yB;
    }

    /**
     * @param yB the yB to set
     */
    public void setyB(double yB) {
        this.yB = yB;
    }
    
    private void computeVertex(Particle p1, Particle p2) {
        if(Reader.useMCTruth) {
            Point3D vx1= new Point3D(p1.getVx(),  p1.getVy(), p1.getVz());
            Point3D vx2= new Point3D(p2.getVx(),  p2.getVy(), p2.getVz());
           
            r = vx1.distance(vx2);
            System.out.println("VTX "+vx1.toString()+ " "+vx2.toString()+" r "+r);
            vx = 0.5*(vx1.x()+vx2.x());
            vy = 0.5*(vx1.y()+vx2.y());
            vz = 0.5*(vx1.z()+vx2.z());
            return;
        }
        //this.computeVertexH(p1, p2);
        
       // double x10 = p1.getVx();
        //double y10 = p1.getVy();
        //double x20 = p2.getVx();
        //double y20 = p2.getVy();
        
        
        //double R1 = Math.sqrt(x10*x10+y10*y10);
        //double R2 = Math.sqrt(x20*x20+y20*y20);
        
        
        DoubleSwim ds = new DoubleSwim(p1.getVx(),  p1.getVy(), p1.getVz(),
                                        p1.getPx(), p1.getPy(), p1.getPz(), p1.getCharge(),
                                        p2.getVx(),  p2.getVy(), p2.getVz(),
                                        p2.getPx(), p2.getPy(), p2.getPz(), p2.getCharge());
        ds.Z = -30;
        
        double[] result = ds.InterpBetTrks();
        
        r = result[12];
        
        p1.setVx(result[0]);
        p1.setVy(result[1]);
        p1.setVz(result[2]);
        p1.setPx(result[3]);
        p1.setPy(result[4]);
        p1.setPz(result[5]);
        p2.setVx(result[6]);
        p2.setVy(result[7]);
        p2.setVz(result[8]);
        p2.setPx(result[9]);
        p2.setPy(result[10]);
        p2.setPz(result[11]); 
        
        double massConstE1 = Math.sqrt(p1.getPx()*p1.getPx()+p1.getPy()*p1.getPy()+p1.getPz()*p1.getPz()
               +p1.getMass()*p1.getMass()); 
        double massConstE2 = Math.sqrt(p2.getPx()*p2.getPx()+p2.getPy()*p2.getPy()+p2.getPz()*p2.getPz()
               +p2.getMass()*p2.getMass());
        
        p1.setMassConstrE(massConstE1);
        p2.setMassConstrE(massConstE2);
        
        vx = 0.5*(result[0]+result[6]);
        vy = 0.5*(result[1]+result[7]);
        vz = 0.5*(result[2]+result[8]);
        
    }
    
    VertexFinder vertexFinder = new VertexFinder();
    float b[] = new float[3];
        
    private void computeVertexH(Particle p1, Particle p2) {
        
        this.getSwimmer().BfieldLab(p1.getVx(),  p1.getVy(),  p1.getVz(), b);
        double B = Math.sqrt(b[0]*b[0]+b[1]*b[1]+b[2]*b[2]);
        TrackParsHelix th1 = new TrackParsHelix(0, p1.getVx(),  p1.getVy(),  p1.getVz(),
                                        p1.getPx(), p1.getPy(), p1.getPz(), 
                                       p1.getCharge(), B, getxB(), getyB());
        this.getSwimmer().BfieldLab(p2.getVx(),  p2.getVy(),  p2.getVz(), b);
        B = Math.sqrt(b[0]*b[0]+b[1]*b[1]+b[2]*b[2]);
        TrackParsHelix th2 = new TrackParsHelix(0, p2.getVx(),  p2.getVy(),  p2.getVz(),
                                        p2.getPx(), p2.getPy(), p2.getPz(), 
                                       p2.getCharge(), B, getxB(), getyB());
        
        ArrayList<TrackParsHelix> helixPair  = new ArrayList<>();
        
        helixPair.add(th1);
	helixPair.add(th2);
        Vertex v = vertexFinder.FindVertex(helixPair);
        if(v!=null) {
            r = v.getDoca();
            Point3D vt = v.get_Vertex();
            vx = vt.x();
            vy = vt.y();
            vz = vt.z();
            
            Point3D v_1 = v.getTrack1POCA();
            Point3D v_2 = v.getTrack2POCA();
           
            p1.setVx(v_1.x());
            p1.setVy(v_1.y());
            p1.setVz(v_1.z());
            
            p2.setVx(v_2.x());
            p2.setVy(v_2.y());
            p2.setVz(v_2.z());
            
            Vector3D p_1 = v.getTrack1POCADir().asUnit();
            p_1.scale(p1.getP());
            Vector3D p_2 = v.getTrack2POCADir().asUnit();
            p_2.scale(p2.getP());
           
            p1.setPx(p_1.x());
            p1.setPy(p_1.y());
            p1.setPz(p_1.z());
            
            p2.setPx(p_2.x());
            p2.setPy(p_2.y());
            p2.setPz(p_2.z());
            
        }
        
    }
}
