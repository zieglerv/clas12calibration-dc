/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.myanal;

import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author ziegler
 */
public class DoubleSwim  extends Swim {
    private double _x01;
    private double _y01;
    private double _z01;
    private double _px01;
    private double _py01;
    private double _pz01;
    private int _charge1;
    private double _x02;
    private double _y02;
    private double _z02;
    private double _px02;
    private double _py02;
    private double _pz02;
    private int _charge2;
    public double Z;
    private Swim swim1;
    private Swim swim2;
    
    private double r = 99999;
   
    public DoubleSwim(double x01, double y01, double z01, double px01, double py01, double pz01, int charge1,
            double x02, double y02, double z02, double px02, double py02, double pz02, int charge2) {
        _x01 = x01;
        _y01 = y01;
        _z01 = z01;
        _px01 = px01;
        _py01 = py01;
        _pz01 = pz01;
        _charge1 = charge1;
        _x02 = x02;
        _y02 = y02;
        _z02 = z02;
        _px02 = px02;
        _py02 = py02;
        _pz02 = pz02;
        _charge2 = charge2;
        
        swim1 = new Swim();
        swim2 = new Swim();
        
    }
    
    //double accuracy = 20e-6; // 20 microns
    //double stepSize = 5.00 * 1.e-4; // 500 microns
    
    
    
    public double[] InterpBetTrks()  {

        double[] value = new double[13];
        
        swim1.SetSwimParameters(_x01, _y01, _z01, _px01, _py01, _pz01, _charge1);
        swim2.SetSwimParameters(_x02, _y02, _z02, _px02, _py02, _pz02, _charge2);
        
        Point3D X1 = new Point3D(_x01, _y01, _z01);
        Point3D X2 = new Point3D(_x02, _y02, _z02);
        Vector3D P1 = new Vector3D(_px01, _py01, _pz01);
        Vector3D P2 = new Vector3D(_px02, _py02, _pz02);
        double p1 = P1.mag();
        double p2 = P2.mag();
        
        double[] tr1 = swim1.SwimToZ(100., 1); // get outmost traj point instead
        double[] tr2 = swim2.SwimToZ(100., 1);
        
        _x01 = tr1[0];
        _y01 = tr1[1];
        _z01 = tr1[2];
        _px01 = tr1[3];
        _py01 = tr1[4];
        _pz01 = tr1[5];
        _x02 = tr2[0];
        _y02 = tr2[1];
        _z02 = tr2[2];
        _px02 = tr2[3];
        _py02 = tr2[4];
        _pz02 = tr2[5];
        swim1.SetSwimParameters(_x01, _y01, _z01, -_px01, -_py01, -_pz01, -_charge1);
        swim2.SetSwimParameters(_x02, _y02, _z02, -_px02, -_py02, -_pz02, -_charge2);
        swim1.stepSize= 500.00* 1.e-6; // 500 microns
        swim2.stepSize= 500.00* 1.e-6; // 500 microns
        swim1.distanceBetweenSaves=500.00 * 1.e-6; // 500 microns
        swim2.distanceBetweenSaves=500.00 * 1.e-6; // 500 microns
        
        tr1 = swim1.SwimToZ(-30., -1);
        tr2 = swim2.SwimToZ(-30., -1);
        _x01 = tr1[0];
        _y01 = tr1[1];
        _z01 = tr1[2];
        _px01 = -tr1[3];
        _py01 = -tr1[4];
        _pz01 = -tr1[5];
        _x02 = tr2[0];
        _y02 = tr2[1];
        _z02 = tr2[2];
        _px02 = -tr2[3];
        _py02 = -tr2[4];
        _pz02 = -tr2[5];
        
        double[][] t1;
        double[][] t2;
        
        
        
        Point3D X1CA = new Point3D(_x01, _y01, _z01);
        Point3D X2CA = new Point3D(_x02, _y02, _z02);
        Point3D P1CA = new Point3D(_px01, _py01, _pz01);
        Point3D P2CA = new Point3D(_px02, _py02, _pz02);
        
        
        double doca = 99999;
        int ref_i;
        int ref_j;
        if(swim1.getSwimTraj()!=null && swim2.getSwimTraj()!=null) {
            t1 = new double[swim1.getSwimTraj().size()][6];
            t2 = new double[swim2.getSwimTraj().size()][6];
            for(int i = 0; i < swim1.getSwimTraj().size(); i++) {
                t1[i] = swim1.getSwimTraj().get(i);
            } 
            for(int i = 0; i < swim2.getSwimTraj().size(); i++) {
                t2[i] = swim2.getSwimTraj().get(i);
            } 
            
            for(int i = 0; i < swim1.getSwimTraj().size(); i++) {
                X1.set(t1[i][0]*100,t1[i][1]*100,t1[i][2]*100);
                for(int j = 0; j < swim2.getSwimTraj().size(); j++) {
                    if(Math.abs(t2[j][2]-t1[i][2])>swim1.distanceBetweenSaves)
                        continue;
                    X2.set(t2[j][0]*100,t2[j][1]*100,t2[j][2]*100);
                    double newDoca = Math.sqrt((X1.x()-X2.x())*(X1.x()-X2.x())
                            +(X1.y()-X2.y())*(X1.y()-X2.y()));
                    
                    if(newDoca < doca) {
                        ref_i =i;
                        ref_j =j;
                        
                        doca = newDoca;
                        //X1CA.set(t1[i][0]*100,t1[i][1]*100,t1[i][2]*100);
                        //P1CA.set(t1[i][3]*p1,t1[i][4]*p1,t1[i][5]*p1);
                        //X2CA.set(t2[j][0]*100,t2[j][1]*100,t2[j][2]*100);
                        //P2CA.set(t2[j][3]*p2,t2[j][4]*p2,t2[j][5]*p2);
                        //get the i for the 3 closest points - make an arc and get the doca to those arcs
                    }
                }
            }
            
        }
        
        value[0] = X1CA.x();
        value[1] = X1CA.y();
        value[2] = X1CA.z();
        value[3] = P1CA.x();
        value[4] = P1CA.y();
        value[5] = P1CA.z();
        value[6] = X2CA.x();
        value[7] = X2CA.y();
        value[8] = X2CA.z();
        value[9] = P2CA.x();
        value[10] = P2CA.y();
        value[11] = P2CA.z();
        value[12] = doca;
        //System.out.println(" D "+X1CA.toString()+" "+X2CA.toString()+" pass "+swim1.distanceBetweenSaves*100);
        return value;

    }
    
    
}
