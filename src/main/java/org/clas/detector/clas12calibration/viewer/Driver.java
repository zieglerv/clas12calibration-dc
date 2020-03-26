/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 *
 * @author ziegler
 */
public class Driver {
    
      
    public static void main(String[] args) throws FileNotFoundException {
        JFrame    frame    = new JFrame();
        JButton   T2DButton = null;
        JButton   T0Button = null;
        JButton   TDCButton = null;
        JPanel panel = new JPanel(new GridLayout(1, 3)); 
        frame.setSize(350, 300); 
        frame.setTitle("DC Calibration");
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        
        T2DButton = new JButton("T2D");
        T2DButton.setUI(new MetalButtonUI());
        T2DButton.setBackground(Color.CYAN);
        T2DButton.setContentAreaFilled(false);
        T2DButton.setOpaque(true);
        T2DButton.setFont(new Font("Arial", Font.BOLD, 18));
        T2DButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("DC Calibration");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Viewer viewer = null;
                try {
                    viewer = new Viewer();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
                }
                frame.add(viewer.mainPanel);
                frame.setJMenuBar(viewer.menuBar);
                frame.setSize(1400, 800);
                frame.setVisible(true);
                viewer.configFrame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                viewer.configure();
                return;
            }
        });
        panel.add(T2DButton);
        
        frame.add(panel, BorderLayout.CENTER);
        frame.add(T2DButton, BorderLayout.PAGE_END);
        

    }
}
