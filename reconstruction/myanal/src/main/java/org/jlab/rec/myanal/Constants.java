/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.myanal;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ziegler
 */
public class Constants {
    
     // private constructor for a singleton
    private Constants() {
    }
    
    // singleton
    private static Constants instance = null;
    
    /**
     * public access to the singleton
     * 
     * @return the constants singleton
     */
    public static Constants getInstance() {
            if (instance == null) {
                    instance = new Constants();
            }
            return instance;
    }
    
    
    //e.g. 3122:2212:-211:0:1.0:1.5;-3122:-2212:211:0:1.0:1.5
    public static List<Integer> parent;
    public static List<Integer> dau1;
    public static List<Integer> dau2;
    public static List<Integer> dau3;
    public static List<Double> lowBound;
    public static List<Double> highBound;
    
    public static void setDecays(String decays) {
        parent      = new ArrayList<>();
        dau1        = new ArrayList<>();
        dau2        = new ArrayList<>();
        dau3        = new ArrayList<>();
        lowBound    = new ArrayList<>();
        highBound   = new ArrayList<>();
        
        if(decays!=null) {
            String[] chain = decays.split(";");
            for(int i =0; i< chain.length; i++) {
                String[] decprod = chain[i].split(":");
                System.out.println(decprod[0]+" --> "+decprod[1]+" + "+decprod[2]);
                parent.add(Integer.parseInt(decprod[0]));
                dau1.add(Integer.parseInt(decprod[1]));
                dau2.add(Integer.parseInt(decprod[2]));
                dau3.add(Integer.parseInt(decprod[3]));
                lowBound.add(Double.parseDouble(decprod[4]));
                highBound.add(Double.parseDouble(decprod[5]));
                
            }
        }
    }
}
