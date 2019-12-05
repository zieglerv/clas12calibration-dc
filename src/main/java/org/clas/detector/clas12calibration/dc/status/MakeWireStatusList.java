/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.status;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author ziegler
 */
public class MakeWireStatusList {
    
    public static class Wire implements Comparable<Wire>{
        private int _sector;
        private int _layer;
        private int _component;
        private int _status;
        
        public Wire(int sector, int layer, int component, int status) {
            _sector     = sector;
            _layer      = layer;
            _component  = component;
            _status     = status;
        }

        /**
         * @return the _sector
         */
        public int getSector() {
            return _sector;
        }

        /**
         * @param _sector the _sector to set
         */
        public void setSector(int _sector) {
            this._sector = _sector;
        }

        /**
         * @return the _layer
         */
        public int getLayer() {
            return _layer;
        }

        /**
         * @param _layer the _layer to set
         */
        public void setLayer(int _layer) {
            this._layer = _layer;
        }

        /**
         * @return the _component
         */
        public int getComponent() {
            return _component;
        }

        /**
         * @param _component the _component to set
         */
        public void setComponent(int _component) {
            this._component = _component;
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

        @Override
        public int compareTo(Wire arg) {
        // Sort by sector, layer, component, status
        int return_val = 0;
        int CompSec = this.getSector() < arg.getSector()? -1 : this.getSector() == arg.getSector() ? 0 : 1;
        int CompLay = this.getLayer() < arg.getLayer() ? -1 : this.getLayer() == arg.getLayer() ? 0 : 1;
        int CompComp = this.getComponent() < arg.getComponent() ? -1 : this.getComponent() == arg.getComponent() ? 0 : 1;
        int CompStat = this.getStatus() < arg.getStatus() ? -1 : this.getStatus() == arg.getStatus() ? 0 : 1;

        int return_val2 = ((CompComp == 0) ? CompStat : CompComp);
        int return_val1 = ((CompLay == 0) ? return_val2 : CompLay);
        return_val = ((CompSec == 0) ? return_val1 : CompSec);

        return return_val;
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        //Read the options:
        OptionParser parser = new OptionParser("wirestatus-filemaker");
        parser.addOption("-o"    ,"wirestatuslist.txt", "file name");
        parser.addOption("-p"    ,  "", "path to the input files");
        parser.addOption("-i"    ,  "", "set input file containing list of bad wires");
        parser.parse(args);
        
        List<String> arguments = new ArrayList<String>();
        for(String item : args){ arguments.add(item); }
        
        String wirestatuslistFileName = null;
        if(parser.hasOption("-o")==true) wirestatuslistFileName = parser.getOption("-o").stringValue();
        
        String inputFileName = null;
        if(parser.hasOption("-i")==true) inputFileName = parser.getOption("-i").stringValue();
            
        String pathString = null;
        if(parser.hasOption("-p")==true) pathString = parser.getOption("-p").stringValue();
            
        
        List<Wire> listOfWires = new ArrayList<Wire>();
        List<Wire> listOfReplWires = new ArrayList<Wire>();
        PrintWriter pw ;
        BufferedReader reader;
        String[] splited ;
        try {
            reader = new BufferedReader(new FileReader("/Users/ziegler/Desktop/Base/CodeDevel/Calibration/good-wires.txt"));
            try {
                String line = reader.readLine();
                while(line != null) {
                    splited = line.split("\\s+");
                    listOfWires.add(new Wire(Integer.parseInt(splited[0]),Integer.parseInt(splited[1]),
                            Integer.parseInt(splited[2]),Integer.parseInt(splited[3])));
                    line = reader.readLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(MakeWireStatusList.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MakeWireStatusList.class.getName()).log(Level.SEVERE, null, ex);
        }
        //read the bad set of wires
        try {
            reader = new BufferedReader(new FileReader("/Users/ziegler/Desktop/Base/CodeDevel/Calibration/bad-wires-run-5036.txt"));
            try {
                String line = reader.readLine();
                while(line != null) {
                    splited = line.split("\\s+");
                    listOfWires.add(new Wire(Integer.parseInt(splited[0]),Integer.parseInt(splited[1]),
                            Integer.parseInt(splited[2]),Integer.parseInt(splited[3])));
                    line = reader.readLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(MakeWireStatusList.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MakeWireStatusList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Collections.sort(listOfWires);
        for(int i = 1; i < listOfWires.size(); i++) {
            if(listOfWires.get(i-1)._sector==listOfWires.get(i)._sector && 
                    listOfWires.get(i-1)._layer==listOfWires.get(i)._layer &&
                    listOfWires.get(i-1)._component==listOfWires.get(i)._component) {
                listOfReplWires.add(listOfWires.get(i-1));
            }
        }
        listOfWires.removeAll(listOfReplWires);
        pw = new PrintWriter("/Users/ziegler/Desktop/Base/CodeDevel/Calibration/status-wires.txt");
        //System.out.println("\n Wire status file name: " + fileName + "\n");
            
        for(int i = 0; i < listOfWires.size(); i++) {
            System.out.printf("%d\t%d\t%d\t%d\n",
                    listOfWires.get(i)._sector, listOfWires.get(i)._layer, 
                    listOfWires.get(i)._component, listOfWires.get(i)._status);
            pw.printf("%d\t%d\t%d\t%d\n",
                    listOfWires.get(i)._sector, listOfWires.get(i)._layer, 
                    listOfWires.get(i)._component, listOfWires.get(i)._status);
        }
        pw.close();
    }
}
