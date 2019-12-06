/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detector.clas12calibration.dc.ui;

//import static org.clas.detector.clas12calibration.dc.constants.Constants.nFitPars;
import static org.clas.detector.clas12calibration.dc.constants.Constants.nSectors;
import static org.clas.detector.clas12calibration.dc.constants.Constants.outFileForFitPars;
//import static org.clas.detector.clas12calibration.dc.constants.Constants.parName;
import static org.clas.detector.clas12calibration.dc.constants.Constants.parSteps;
import static org.clas.detector.clas12calibration.dc.constants.Constants.ccdb_variation;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.clas.detector.clas12calibration.dc.constants.Constants;

import org.clas.detector.clas12calibration.dc.fit.TimeToDistanceFitter;
import org.clas.detector.clas12calibration.dc.init.Coordinate;
import org.clas.detector.clas12calibration.dc.io.FileOutputWriter;
import org.clas.detector.clas12calibration.dc.io.ReadT2DparsFromCCDB;

/**
 *
 * @author kpadhikari
 */
public class FitControlUI extends javax.swing.JFrame
{
        public String parName[] =
	{ "v0", "vmid", "tmax", "distbeta", "dB         ", "b1", "b2", "b3", "b4", "r"};
        public int nFitPars= parName.length;
	public static int counterForConsole;
	private final int nSL = 6;
	// private final int nPars = nFitPars; //9;
	private int gSector = 7;
	private int gSuperlayer = 1;
	private String ccdbVariation = ccdb_variation;
	private int xMeanErrorType = 2; // 0: RMS, 1=RMS/sqrt(N), 2 = 1.0 (giving equal weight to all
									// profile means)
	private boolean[] checkboxVal =
	{ false, false, false, false, false, false, false, false, false, false, false };
	private boolean checkBoxFixAll = false;
	public boolean[] selectedAngleBins // = new boolean[nThBinsVz];
	=
	{ false, false, false, false, true, true, true, false, false, false, true, true, true, false, false, false, false };
	
   
	private double[][] resetFitPars = new double[nSL][nFitPars];
	private double[][] resetFitParsLow = new double[nSL][nFitPars];
	private double[][] resetFitParsHigh = new double[nSL][nFitPars];
	private double[][] resetFitParSteps = new double[nSL][nFitPars];
	private double[][][] parsFromCCDB_default = new double[nSectors+1][nSL][nFitPars];// nFitPars = 10
	private double[][][] parsFromCCDB_calib = new double[nSectors+1][nSL][nFitPars];// nFitPars = 10
	private double xNormLow = 0.0, xNormHigh = 0.8;
	TimeToDistanceFitter fitter;
	FitControlBinSelectionUI binSelector;
	//FileOutputWriter file;

	/**
	 * Creates new form FitControlUI
	 */
	public FitControlUI(TimeToDistanceFitter fitter)
	{
		initComponents();
		getParametersFromCCDB();
		addJPopupMenuToJTextArea1();
		this.fitter = fitter;

		int sector = Integer.parseInt(jComboBox1.getSelectedItem().toString());
		int superlayer = Integer.parseInt(jComboBox2.getSelectedItem().toString());
		ccdbVariation = jComboBox4.getSelectedItem().toString(); // 0 for default, 1 for dc_test1
		
		putCCDBvaluesToResetArrays(sector, ccdbVariation); // Initializing reset arrays for par,
															// parLow, & parHigh
		putStepSizeFromConstantsToResetArrays(sector); // Initializing reset array for stepSizes
		assignParValuesToTextFields(sector, superlayer); // Make the numbers in reset arrays show up
															// in the text fields
		openFileToWriteFitParameters();
	}

	public void openFileToWriteFitParameters()
	{
		boolean append_to_file = false;
		FileOutputWriter file = null;
		try
		{
			file = new FileOutputWriter(outFileForFitPars + TimeToDistanceFitter.runNumber + ".txt", append_to_file);
			file.Write("#Sec  SL  component  v0   vmid   tMax   distbeta   delta_bfield_coefficient  b1   b2   b3   b4  r");
			file.Close();
		}
		catch (IOException ex)
		{
			Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void addJPopupMenuToJTextArea1()
	{
		JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem(new DefaultEditorKit.CutAction());
		item.setText("Cut");
		popup.add(item);
		item = new JMenuItem(new DefaultEditorKit.CopyAction());
		item.setText("Copy");
		popup.add(item);
		item = new JMenuItem(new DefaultEditorKit.PasteAction());
		item.setText("Paste");
		popup.add(item);
		jTextArea1.setComponentPopupMenu(popup);
	}

	private void getParametersFromCCDB()
	{
		// Instead of reading the two tables again and again whenever we select the item from
		// the corresponding jComboBox4, it's better to read both once at the beginning,
		// keep them stored in two different array variables and use those arrays later.
		ReadT2DparsFromCCDB rdTable = new ReadT2DparsFromCCDB(ccdbVariation,TimeToDistanceFitter.runNumber);
		rdTable.LoadCCDB();
		parsFromCCDB_calib = rdTable.parsFromCCDB;

		ReadT2DparsFromCCDB rdTable2 = new ReadT2DparsFromCCDB("default",TimeToDistanceFitter.runNumber);
		rdTable2.LoadCCDB();
		parsFromCCDB_default = rdTable2.parsFromCCDB;
	}

	private void putCCDBvaluesToResetArrays(int sector, String ccdbVariation_)
	{
		for (int i = 0; i < nSL; i++)
		{
			for (int j = 0; j < nFitPars; j++)
			{
				// Get the init values from CCDB
				if (ccdbVariation_ == ccdb_variation)
				{
					resetFitPars[i][j] = parsFromCCDB_calib[sector - 1][i][j];
				}
				else if (ccdbVariation == "default")
				{
					resetFitPars[i][j] = parsFromCCDB_default[sector - 1][i][j];
				}

				// Calculate and assign lower and upper limits based on sign and values of the
				// init-values
				if (resetFitPars[i][j] < 0.0)
				{
					resetFitParsLow[i][j] = 2.0 * resetFitPars[i][j];
					resetFitParsHigh[i][j] = 0.2 * resetFitPars[i][j];
				}
				else if(resetFitPars[i][j] > 0.0)
				{
					resetFitParsLow[i][j] = 0.2 * resetFitPars[i][j];
					resetFitParsHigh[i][j] = 2.0 * resetFitPars[i][j];
				}
				else if(resetFitPars[i][j] == 0.0)
				{
					resetFitParsLow[i][j] = 0.2 * resetFitPars[i][j] - 0.001;
					resetFitParsHigh[i][j] = 2.0 * resetFitPars[i][j] + 0.001;
				}
			}
			
		}
	}

	private void putStepSizeFromConstantsToResetArrays(int sector)
	{
		for (int i = 0; i < nSL; i++)
		{
			for (int j = 0; j < nFitPars; j++)
			{
				resetFitParSteps[i][j] = parSteps[j];
			}
		}
	}

	private void updateFitValuesToResetArrays(int sector)
	{
		for (int i = 0; i < nSL; i++)
		{
			for (int j = 0; j < nFitPars; j++)
			{
				resetFitPars[i][j] = fitter.fPars[j];

				// Calculate and assign lower and upper limits based on sign and values of the
				// init-values
				if (resetFitPars[i][j] < 0.0)
				{
					resetFitParsLow[i][j] = 2.0 * resetFitPars[i][j];
					resetFitParsHigh[i][j] = 0.2 * resetFitPars[i][j];
				}
				else if(resetFitPars[i][j] > 0.0)
				{
					resetFitParsLow[i][j] = 0.2 * resetFitPars[i][j];
					resetFitParsHigh[i][j] = 2.0 * resetFitPars[i][j];
				}
				else if(resetFitPars[i][j] == 0.0)
				{
					resetFitParsLow[i][j] = 0.2 * resetFitPars[i][j] - 0.001;
					resetFitParsHigh[i][j] = 2.0 * resetFitPars[i][j] + 0.001;
				}
			}
			
		}
	}

	
	
	

	private void assignParValuesToTextFields(int sector, int superlayer)
	{
		int iSL = superlayer - 1;

		// Setting the first column of text-fields to 0.4 times the previous values of fit-pars
		resetParLowTextField.setText(String.format("%5.4f", resetFitParsLow[iSL][0]));
		resetParLowTextField1.setText(String.format("%5.4f", resetFitParsLow[iSL][1]));
		resetParLowTextField2.setText(String.format("%5.4f", resetFitParsLow[iSL][2]));
		resetParLowTextField3.setText(String.format("%5.4f", resetFitParsLow[iSL][3]));
		resetParLowTextField4.setText(String.format("%5.4f", resetFitParsLow[iSL][4]));
		resetParLowTextField5.setText(String.format("%5.4f", resetFitParsLow[iSL][5]));
		resetParLowTextField6.setText(String.format("%5.4f", resetFitParsLow[iSL][6]));
		resetParLowTextField7.setText(String.format("%5.4f", resetFitParsLow[iSL][7]));
		resetParLowTextField8.setText(String.format("%5.4f", resetFitParsLow[iSL][8]));
		resetParLowTextField9.setText(String.format("%5.4f", resetFitParsLow[iSL][9]));

		// Setting the second column of text-fields to previous values of fit-pars
		// resetParTextField.setText(String.valueOf(resetFitPars[iSL][0]));//works but string format is
		// ugly
		resetParTextField.setText(String.format("%5.4f", resetFitPars[iSL][0]));
		resetParTextField1.setText(String.format("%5.4f", resetFitPars[iSL][1]));
		resetParTextField2.setText(String.format("%5.4f", resetFitPars[iSL][2]));
		resetParTextField3.setText(String.format("%5.4f", resetFitPars[iSL][3]));
		resetParTextField4.setText(String.format("%5.4f", resetFitPars[iSL][4]));
		resetParTextField5.setText(String.format("%5.4f", resetFitPars[iSL][5]));
		resetParTextField6.setText(String.format("%5.4f", resetFitPars[iSL][6]));
		resetParTextField7.setText(String.format("%5.4f", resetFitPars[iSL][7]));
		resetParTextField8.setText(String.format("%5.4f", resetFitPars[iSL][8]));
		resetParTextField9.setText(String.format("%5.4f", resetFitPars[iSL][9]));

		// Setting the third column of text-fields to 2.0 times the previous values of fit-pars
		resetParHighTextField.setText(String.format("%5.4f", resetFitParsHigh[iSL][0]));
		resetParHighTextField1.setText(String.format("%5.4f", resetFitParsHigh[iSL][1]));
		resetParHighTextField2.setText(String.format("%5.4f", resetFitParsHigh[iSL][2]));
		resetParHighTextField3.setText(String.format("%5.4f", resetFitParsHigh[iSL][3]));
		resetParHighTextField4.setText(String.format("%5.4f", resetFitParsHigh[iSL][4]));
		resetParHighTextField5.setText(String.format("%5.4f", resetFitParsHigh[iSL][5]));
		resetParHighTextField6.setText(String.format("%5.4f", resetFitParsHigh[iSL][6]));
		resetParHighTextField7.setText(String.format("%5.4f", resetFitParsHigh[iSL][7]));
		resetParHighTextField8.setText(String.format("%5.4f", resetFitParsHigh[iSL][8]));
		resetParHighTextField9.setText(String.format("%5.4f", resetFitParsHigh[iSL][9]));

		// Now setting the fourth column (for step sizes) in terms of parSteps array (see
		// Constants.java)
		// double parSteps[] = {0.00001, 0.001, 0.01, 0.0001, 0.001, 0.001, 0.001, 0.001, 0.001,
		// 0.001};
		resetParStepTextField.setText(String.format("%6.5f", resetFitParSteps[iSL][0]));
		resetParStepTextField1.setText(String.format("%6.5f", resetFitParSteps[iSL][1]));
		resetParStepTextField2.setText(String.format("%6.5f", resetFitParSteps[iSL][2]));
		resetParStepTextField3.setText(String.format("%6.5f", resetFitParSteps[iSL][3]));
		resetParStepTextField4.setText(String.format("%6.5f", resetFitParSteps[iSL][4]));
		resetParStepTextField5.setText(String.format("%6.5f", resetFitParSteps[iSL][5]));
		resetParStepTextField6.setText(String.format("%6.5f", resetFitParSteps[iSL][6]));
		resetParStepTextField7.setText(String.format("%6.5f", resetFitParSteps[iSL][7]));
		resetParStepTextField8.setText(String.format("%6.5f", resetFitParSteps[iSL][8]));
		resetParStepTextField9.setText(String.format("%6.5f", resetFitParSteps[iSL][9]));

		xLowTextField.setText(String.format("%5.4f", xNormLow));
		xHighTextField.setText(String.format("%5.4f", xNormHigh));
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT
	 * modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{
		jPanel1 = new javax.swing.JPanel();
		paramLabel = new javax.swing.JLabel();
		lowLimLabel = new javax.swing.JLabel();
		initValLabel = new javax.swing.JLabel();
		uppLimLabel = new javax.swing.JLabel();
		FitItLabel = new javax.swing.JLabel();
		resetParLowTextField = new javax.swing.JTextField();
		resetParLowTextField1 = new javax.swing.JTextField();
		resetParLowTextField2 = new javax.swing.JTextField();
		resetParLowTextField3 = new javax.swing.JTextField();
		resetParLowTextField4 = new javax.swing.JTextField();
		resetParLowTextField5 = new javax.swing.JTextField();
		resetParLowTextField6 = new javax.swing.JTextField();
		resetParLowTextField7 = new javax.swing.JTextField();
		resetParLowTextField8 = new javax.swing.JTextField();
		resetParTextField = new javax.swing.JTextField();
		resetParTextField1 = new javax.swing.JTextField();
		resetParTextField2 = new javax.swing.JTextField();
		resetParTextField3 = new javax.swing.JTextField();
		resetParTextField4 = new javax.swing.JTextField();
		resetParTextField5 = new javax.swing.JTextField();
		resetParTextField6 = new javax.swing.JTextField();
		resetParTextField7 = new javax.swing.JTextField();
		resetParTextField8 = new javax.swing.JTextField();
		resetParHighTextField = new javax.swing.JTextField();
		resetParHighTextField1 = new javax.swing.JTextField();
		resetParHighTextField2 = new javax.swing.JTextField();
		resetParHighTextField3 = new javax.swing.JTextField();
		resetParHighTextField4 = new javax.swing.JTextField();
		resetParHighTextField5 = new javax.swing.JTextField();
		resetParHighTextField6 = new javax.swing.JTextField();
		resetParHighTextField7 = new javax.swing.JTextField();
		resetParHighTextField8 = new javax.swing.JTextField();
		jCheckBox1 = new javax.swing.JCheckBox();
		jCheckBox2 = new javax.swing.JCheckBox();
		jCheckBox3 = new javax.swing.JCheckBox();
		jCheckBox4 = new javax.swing.JCheckBox();
		jCheckBox5 = new javax.swing.JCheckBox();
		jCheckBox6 = new javax.swing.JCheckBox();
		jCheckBox7 = new javax.swing.JCheckBox();
		jCheckBox8 = new javax.swing.JCheckBox();
		jCheckBox9 = new javax.swing.JCheckBox();
		jComboBox1 = new javax.swing.JComboBox<>();
		jLabel15 = new javax.swing.JLabel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jComboBox2 = new javax.swing.JComboBox<>();
		jLabel17 = new javax.swing.JLabel();
		jComboBox3 = new javax.swing.JComboBox<>();
		jLabel18 = new javax.swing.JLabel();
		xLowTextField = new javax.swing.JTextField();
		xHighTextField = new javax.swing.JTextField();
		jLabel19 = new javax.swing.JLabel();
		jLabel20 = new javax.swing.JLabel();
		jCheckBoxFixAll = new javax.swing.JCheckBox();
		jComboBox4 = new javax.swing.JComboBox<>();
		jLabel21 = new javax.swing.JLabel();
		resetParLowTextField9 = new javax.swing.JTextField();
		resetParTextField9 = new javax.swing.JTextField();
		resetParHighTextField9 = new javax.swing.JTextField();
		jCheckBox10 = new javax.swing.JCheckBox();
		jLabel22 = new javax.swing.JLabel();
		jLabelChiSq = new javax.swing.JLabel();
		jLabel23 = new javax.swing.JLabel();
		resetParStepTextField = new javax.swing.JTextField();
		resetParStepTextField1 = new javax.swing.JTextField();
		resetParStepTextField2 = new javax.swing.JTextField();
		resetParStepTextField3 = new javax.swing.JTextField();
		resetParStepTextField4 = new javax.swing.JTextField();
		resetParStepTextField5 = new javax.swing.JTextField();
		resetParStepTextField6 = new javax.swing.JTextField();
		resetParStepTextField7 = new javax.swing.JTextField();
		resetParStepTextField8 = new javax.swing.JTextField();
		resetParStepTextField9 = new javax.swing.JTextField();
		jButton8 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextArea1 = new javax.swing.JTextArea();
		jLabel16 = new javax.swing.JLabel();
		jButton4 = new javax.swing.JButton();
		jButton5 = new javax.swing.JButton();
		jButton6 = new javax.swing.JButton();
		jButton7 = new javax.swing.JButton();
		jButton9 = new javax.swing.JButton();
		jButtonSave = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Fit Control");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Set Parameters",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 14), new java.awt.Color(0, 102, 0))); // NOI18N

		paramLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		paramLabel.setText("Parameter");

		lowLimLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		lowLimLabel.setText("Lower Limit");

		initValLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		initValLabel.setText("Initial Value");

		uppLimLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		uppLimLabel.setText("Upper Limit");

		FitItLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		FitItLabel.setText("   Fix it?");
                
                paramsLabel = new javax.swing.JLabel[nFitPars];
                for(int l = 0; l < nFitPars; l++) {
                    javax.swing.JLabel pL = new javax.swing.JLabel();
                    pL.setForeground(new java.awt.Color(0, 102, 0));
                    pL.setText(parName[l]);
                    paramsLabel[l] = pL;
                }


		resetParLowTextField.setText("jTextField1");
		resetParLowTextField.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField1ActionPerformed(evt);
			}
		});

		resetParLowTextField1.setText("jTextField2");
		resetParLowTextField1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField2ActionPerformed(evt);
			}
		});

		resetParLowTextField2.setText("jTextField3");
		resetParLowTextField2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField3ActionPerformed(evt);
			}
		});

		resetParLowTextField3.setText("jTextField4");
		resetParLowTextField3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField4ActionPerformed(evt);
			}
		});

		resetParLowTextField4.setText("jTextField5");
		resetParLowTextField4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField5ActionPerformed(evt);
			}
		});

		resetParLowTextField5.setText("jTextField6");
		resetParLowTextField5.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField6ActionPerformed(evt);
			}
		});

		resetParLowTextField6.setText("jTextField7");
		resetParLowTextField6.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField7ActionPerformed(evt);
			}
		});

		resetParLowTextField7.setText("jTextField8");
		resetParLowTextField7.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField8ActionPerformed(evt);
			}
		});

		resetParLowTextField8.setText("jTextField9");
		resetParLowTextField8.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField9ActionPerformed(evt);
			}
		});

		resetParTextField.setText("jTextField10");
		resetParTextField.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField10ActionPerformed(evt);
			}
		});

		resetParTextField1.setText("jTextField11");
		resetParTextField1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField11ActionPerformed(evt);
			}
		});

		resetParTextField2.setText("jTextField12");
		resetParTextField2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField12ActionPerformed(evt);
			}
		});

		resetParTextField3.setText("jTextField13");
		resetParTextField3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField13ActionPerformed(evt);
			}
		});

		resetParTextField4.setText("jTextField14");
		resetParTextField4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField14ActionPerformed(evt);
			}
		});

		resetParTextField5.setText("jTextField15");
		resetParTextField5.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField15ActionPerformed(evt);
			}
		});

		resetParTextField6.setText("jTextField16");
		resetParTextField6.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField16ActionPerformed(evt);
			}
		});

		resetParTextField7.setText("jTextField17");
		resetParTextField7.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField17ActionPerformed(evt);
			}
		});

		resetParTextField8.setText("jTextField18");
		resetParTextField8.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField18ActionPerformed(evt);
			}
		});

		resetParHighTextField.setText("jTextField19");
		resetParHighTextField.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField19ActionPerformed(evt);
			}
		});

		resetParHighTextField1.setText("jTextField20");
		resetParHighTextField1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField20ActionPerformed(evt);
			}
		});

		resetParHighTextField2.setText("jTextField21");
		resetParHighTextField2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField21ActionPerformed(evt);
			}
		});

		resetParHighTextField3.setText("jTextField22");
		resetParHighTextField3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField22ActionPerformed(evt);
			}
		});

		resetParHighTextField4.setText("jTextField23");
		resetParHighTextField4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField23ActionPerformed(evt);
			}
		});

		resetParHighTextField5.setText("jTextField24");
		resetParHighTextField5.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField24ActionPerformed(evt);
			}
		});

		resetParHighTextField6.setText("jTextField25");
		resetParHighTextField6.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField25ActionPerformed(evt);
			}
		});

		resetParHighTextField7.setText("jTextField26");
		resetParHighTextField7.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField26ActionPerformed(evt);
			}
		});

		resetParHighTextField8.setText("jTextField27");
		resetParHighTextField8.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField27ActionPerformed(evt);
			}
		});

		jCheckBox1.setText("Fix me");
                jCheckBox1.setSelected(true);
		jCheckBox1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox1ActionPerformed(evt);
			}
		});

		jCheckBox2.setText("Fix me");
                jCheckBox2.setSelected(true);
		jCheckBox2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox2ActionPerformed(evt);
			}
		});

		jCheckBox3.setText("Fix me");
		jCheckBox3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox3ActionPerformed(evt);
			}
		});

		jCheckBox4.setText("Fix me");
                jCheckBox4.setSelected(true);
		jCheckBox4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox4ActionPerformed(evt);
			}
		});

		jCheckBox5.setText("Fix me");
                jCheckBox5.setSelected(true);
		jCheckBox5.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox5ActionPerformed(evt);
			}
		});

		jCheckBox6.setText("Fix me");
                jCheckBox6.setSelected(true);
		jCheckBox6.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox6ActionPerformed(evt);
			}
		});

		jCheckBox7.setText("Fix me");
                jCheckBox7.setSelected(true);
		jCheckBox7.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox7ActionPerformed(evt);
			}
		});

		jCheckBox8.setText("Fix me");
                jCheckBox8.setSelected(true);
		jCheckBox8.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox8ActionPerformed(evt);
			}
		});

		jCheckBox9.setText("Fix me");
                jCheckBox9.setSelected(true);
		jCheckBox9.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox9ActionPerformed(evt);
			}
		});

		jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]
		{ "1", "2", "3", "4", "5", "6", "7" }));
		jComboBox1.setSelectedIndex(0);
		jComboBox1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jComboBox1ActionPerformed(evt);
			}
		});

		jLabel15.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel15.setText("Superlayer");

		jButton1.setText("Set Parameters");
		jButton1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton1ActionPerformed(evt);
			}
		});

		jButton2.setText("Go Fit It");
		jButton2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton2ActionPerformed(evt);
			}
		});

		jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]
		{ "1", "2", "3", "4", "5", "6" }));
		jComboBox2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jComboBox2ActionPerformed(evt);
			}
		});

		jLabel17.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel17.setText("Sector");

		jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]
		{ "RMS in x-slice", "RMS/sqrt(N)", "1.0" }));
		jComboBox3.setSelectedIndex(2);
		jComboBox3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jComboBox3ActionPerformed(evt);
			}
		});

		jLabel18.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel18.setText("Uncertainty");

		xLowTextField.setText("jTextField28");
		xLowTextField.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField28ActionPerformed(evt);
			}
		});

		xHighTextField.setText("jTextField29");
		xHighTextField.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField29ActionPerformed(evt);
			}
		});

		jLabel19.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel19.setText("xNormMin");

		jLabel20.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel20.setText("xNormMax");

		jCheckBoxFixAll.setText("Fix All");
		jCheckBoxFixAll.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBoxFixAllActionPerformed(evt);
			}
		});

		jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]
		{ "default", ccdb_variation }));
		jComboBox4.setSelectedIndex(1);
		jComboBox4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jComboBox4ActionPerformed(evt);
			}
		});

		jLabel21.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel21.setText("CCDB variation for Initial Values");

		resetParLowTextField9.setText("jTextField30");

		resetParTextField9.setText("jTextField31");

		resetParHighTextField9.setText("jTextField32");

		jCheckBox10.setText("Fix me");
                jCheckBox10.setSelected(true);
		jCheckBox10.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jCheckBox10ActionPerformed(evt);
			}
		});

		jLabel22.setForeground(new java.awt.Color(0, 102, 0));
		jLabel22.setText("<html>r</sub></html>");
		
		jLabelChiSq.setForeground(new java.awt.Color(0, 102, 0));
		jLabelChiSq.setText("<html>&Chi Square</html>");

		jLabel23.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel23.setText("Step Size");

		resetParStepTextField.setText("jTextField33");
		resetParStepTextField.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField33ActionPerformed(evt);
			}
		});

		resetParStepTextField1.setText("jTextField34");
		resetParStepTextField1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField34ActionPerformed(evt);
			}
		});

		resetParStepTextField2.setText("jTextField35");
		resetParStepTextField2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField35ActionPerformed(evt);
			}
		});

		resetParStepTextField3.setText("jTextField36");
		resetParStepTextField3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField36ActionPerformed(evt);
			}
		});

		resetParStepTextField4.setText("jTextField37");
		resetParStepTextField4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField37ActionPerformed(evt);
			}
		});

		resetParStepTextField5.setText("jTextField38");
		resetParStepTextField5.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField38ActionPerformed(evt);
			}
		});

		resetParStepTextField6.setText("jTextField39");
		resetParStepTextField6.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField39ActionPerformed(evt);
			}
		});

		resetParStepTextField7.setText("jTextField40");
		resetParStepTextField7.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField40ActionPerformed(evt);
			}
		});

		resetParStepTextField8.setText("jTextField41");
		resetParStepTextField8.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField41ActionPerformed(evt);
			}
		});

		resetParStepTextField9.setText("jTextField42");
		resetParStepTextField9.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jTextField42ActionPerformed(evt);
			}
		});

		jButton8.setText("Select Angle Bins");
		jButton8.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton8ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel1Layout.createSequentialGroup()
								.addGap(5, 5, 5)
								.addComponent(jLabel17)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addComponent(jLabel15)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabel21)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(232, 232, 232))
						.addGroup(jPanel1Layout.createSequentialGroup()
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jPanel1Layout.createSequentialGroup()
												.addGroup(jPanel1Layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(jPanel1Layout.createSequentialGroup()
																.addGap(17, 17, 17)
																.addComponent(jComboBox3,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(jPanel1Layout.createSequentialGroup()
																.addGap(35, 35, 35)
																.addComponent(jLabel18)))
												.addGap(24, 24, 24)
												.addGroup(jPanel1Layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(jPanel1Layout.createSequentialGroup()
																.addGap(77, 77, 77)
																.addGroup(jPanel1Layout.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.LEADING,
																		false)
																		.addComponent(initValLabel,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				103, Short.MAX_VALUE)
																		.addComponent(resetParTextField9)
																		.addComponent(resetParTextField1)
																		.addComponent(resetParTextField2)
																		.addComponent(resetParTextField3)
																		.addComponent(resetParTextField4)
																		.addComponent(resetParTextField8)
																		.addComponent(resetParTextField7)
																		.addComponent(resetParTextField6)
																		.addComponent(resetParTextField5)
																		.addComponent(resetParTextField))
																.addGroup(jPanel1Layout.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.LEADING)
																		.addGroup(jPanel1Layout.createSequentialGroup()
																				.addGroup(jPanel1Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(jPanel1Layout
																								.createSequentialGroup()
																								.addGap(18, 18, 18)
																								.addGroup(jPanel1Layout
																										.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.TRAILING,
																												false)
																										.addComponent(resetParHighTextField1,
																												javax.swing.GroupLayout.Alignment.LEADING,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												101,
																												Short.MAX_VALUE)
																										.addComponent(resetParHighTextField2,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField3,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField4,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField5,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField6,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField7,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField8,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField9,
																												javax.swing.GroupLayout.Alignment.LEADING)
																										.addComponent(resetParHighTextField)))
																						.addGroup(jPanel1Layout
																								.createSequentialGroup()
																								.addGap(27, 27, 27)
																								.addComponent(uppLimLabel,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										96,
																										javax.swing.GroupLayout.PREFERRED_SIZE)))
																				.addGap(18, 18, 18)
																				.addGroup(jPanel1Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addGroup(jPanel1Layout
																								.createSequentialGroup()
																								.addComponent(jLabel23)
																								.addGap(0, 0,
																										Short.MAX_VALUE))
																						.addComponent(resetParStepTextField3)
																						.addComponent(resetParStepTextField4)
																						.addComponent(resetParStepTextField5)
																						.addComponent(resetParStepTextField6)
																						.addComponent(resetParStepTextField7)
																						.addComponent(resetParStepTextField8)
																						.addComponent(resetParStepTextField9)
																						.addComponent(resetParStepTextField,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								98, Short.MAX_VALUE)
																						.addComponent(resetParStepTextField1)
																						.addComponent(resetParStepTextField2)))
																		.addGroup(jPanel1Layout.createSequentialGroup()
																				.addGap(12, 12, 12)
																				.addComponent(jButton1)
																				.addPreferredGap(
																						javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																				.addComponent(jButton2))))
														.addGroup(jPanel1Layout.createSequentialGroup()
																.addGroup(jPanel1Layout.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.LEADING)
																		.addGroup(jPanel1Layout.createSequentialGroup()
																				.addGap(4, 4, 4)
																				.addComponent(jLabel19)
																				.addPreferredGap(
																						javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																				.addComponent(jLabel20))
																		.addGroup(jPanel1Layout.createSequentialGroup()
																				.addComponent(xLowTextField,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.PREFERRED_SIZE)
																				.addPreferredGap(
																						javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																				.addComponent(xHighTextField,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.PREFERRED_SIZE)))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(jButton8))))
										.addGroup(jPanel1Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
												.addGroup(jPanel1Layout.createSequentialGroup()
														.addComponent(paramLabel)
														.addGap(12, 12, 12)
														.addComponent(lowLimLabel, javax.swing.GroupLayout.PREFERRED_SIZE,
																111, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(javax.swing.GroupLayout.Alignment.LEADING,
														jPanel1Layout.createSequentialGroup()
																.addGroup(jPanel1Layout.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.TRAILING,
																		false)
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[0])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[1])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField1,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[2])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField2,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[3])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField3,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[4])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField4,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[5])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField5,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[6])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField6,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addComponent(paramsLabel[7])
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(resetParLowTextField7,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								102,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				jPanel1Layout.createSequentialGroup()
																						.addGroup(jPanel1Layout
																								.createParallelGroup(
																										javax.swing.GroupLayout.Alignment.LEADING)
																								.addComponent(paramsLabel[8])
																								.addComponent(jLabel22))
																						.addGroup(jPanel1Layout
																								.createParallelGroup(
																										javax.swing.GroupLayout.Alignment.LEADING)
																								.addGroup(jPanel1Layout
																										.createSequentialGroup()
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(resetParLowTextField9,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												102,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																								.addGroup(
																										javax.swing.GroupLayout.Alignment.TRAILING,
																										jPanel1Layout
																												.createSequentialGroup()
																												.addGap(12,
																														12,
																														12)
																												.addComponent(resetParLowTextField8,
																														javax.swing.GroupLayout.PREFERRED_SIZE,
																														102,
																														javax.swing.GroupLayout.PREFERRED_SIZE)))))
																.addGap(9, 9, 9))))
								.addGap(30, 30, 30)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jCheckBoxFixAll)
										.addComponent(jCheckBox9)
										.addComponent(FitItLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 168,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox1)
										.addComponent(jCheckBox2)
										.addComponent(jCheckBox3)
										.addComponent(jCheckBox4)
										.addComponent(jCheckBox5)
										.addComponent(jCheckBox6)
										.addComponent(jCheckBox7)
										.addComponent(jCheckBox8, javax.swing.GroupLayout.PREFERRED_SIZE, 182,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox10))
								.addContainerGap()));

		//jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]
		//{ paramLabel[0], paramLabel[0], paramLabel[0], paramLabel[0], paramLabel[0], paramLabel[0], paramsLabel[0], paramLabel[0], paramLabel[0], paramLabel[0] });

		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel1Layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel15)
										.addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel17)
										.addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel21))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramLabel)
										.addComponent(lowLimLabel)
										.addComponent(initValLabel)
										.addComponent(uppLimLabel)
										.addComponent(FitItLabel)
										.addComponent(jLabel23))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[0], javax.swing.GroupLayout.PREFERRED_SIZE, 16,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParLowTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox1)
										.addComponent(resetParStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[1])
										.addComponent(resetParLowTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox2)
										.addComponent(resetParStepTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[2])
										.addComponent(resetParLowTextField2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox3)
										.addComponent(resetParStepTextField2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[3])
										.addComponent(resetParLowTextField3, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField3, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField3, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox4)
										.addComponent(resetParStepTextField3, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[4])
										.addComponent(resetParLowTextField4, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField4, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField4, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox5)
										.addComponent(resetParStepTextField4, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[5])
										.addComponent(resetParLowTextField5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox6)
										.addComponent(resetParStepTextField5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[6])
										.addComponent(resetParLowTextField6, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField6, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField6, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox7)
										.addComponent(resetParStepTextField6, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[7])
										.addComponent(resetParLowTextField7, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField7, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox8)
										.addComponent(resetParHighTextField7, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParStepTextField7, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(paramsLabel[8])
										.addComponent(resetParLowTextField8, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField8, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox9)
										.addComponent(resetParHighTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParStepTextField8, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(resetParLowTextField9, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParTextField9, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(resetParHighTextField9, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jCheckBox10)
										.addComponent(jLabel22)
										.addComponent(resetParStepTextField9, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jPanel1Layout.createSequentialGroup()
												.addGap(8, 8, 8)
												.addGroup(jPanel1Layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jComboBox3,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(xLowTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(xHighTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jCheckBoxFixAll))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(jPanel1Layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel18)
														.addComponent(jLabel19)
														.addComponent(jLabel20))
												.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
												jPanel1Layout.createSequentialGroup()
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24,
																Short.MAX_VALUE)
														.addComponent(jButton8)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addGroup(jPanel1Layout.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(jButton1)
																.addComponent(jButton2))
														.addContainerGap()))));

		jButton3.setText("Exit");
		jButton3.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton3ActionPerformed(evt);
			}
		});

//		jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(
//				"Sec    SL   v0   deltanm   tMax  distbeta  data_bfield_coeff.    b1    b2    b3   b4  deltaT0"));
		jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(
				"<html>Sec    &emsp; &emsp;      SL     &emsp; &emsp;      v<sub>0</sub>     &emsp; &emsp;         &v;<sub>mid</sub>   &emsp; &emsp;      t<sub>max</sub>     &emsp; &emsp;     x<sub>&beta;</sub>     &emsp; &emsp;     &delta<sub>B</sub>     &emsp; &emsp;       b<sub>1</sub>          &emsp; &emsp;    b<sub>2</sub>        &emsp; &emsp;         b<sub>3</sub>       &emsp; &emsp;           b<sub>4</sub>       &emsp; &emsp;          &r       &emsp;&emsp; &emsp;          &chi<sup>2</sup></html>"));

		jTextArea1.setColumns(20);
		jTextArea1.setRows(5);
		jScrollPane1.setViewportView(jTextArea1);

		jLabel16.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
		jLabel16.setText("Fit Results");

		jButton4.setText("Slice Viewer");
		jButton4.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton4ActionPerformed(evt);
			}
		});

		jButton5.setText("Residuals");
		jButton5.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton5ActionPerformed(evt);
			}
		});

		jButton6.setText("Times");
		jButton6.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton6ActionPerformed(evt);
			}
		});

		jButton7.setText("B-field");
		jButton7.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton7ActionPerformed(evt);
			}
		});

		jButton9.setText("local-angle");
		jButton9.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButton9ActionPerformed(evt);
			}
		});
		
		jButtonSave.setText("Save To File");
		jButtonSave.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				jButtonSaveActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createSequentialGroup()
												.addGap(6, 6, 6)
												.addComponent(jLabel16))
										.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 756,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
												.addGroup(layout.createSequentialGroup()
														.addComponent(jButton4)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButton5)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButton6)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButton7)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButton9)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jButtonSave)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(jButton3))
												.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 756,
														javax.swing.GroupLayout.PREFERRED_SIZE)))
								.addGap(0, 10, Short.MAX_VALUE)));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jLabel16)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 191,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jButton3)
										.addComponent(jButton4)
										.addComponent(jButton5)
										.addComponent(jButton6)
										.addComponent(jButton7)
										.addComponent(jButton9)
										.addComponent(jButtonSave))
								.addContainerGap()));

		jScrollPane1.getAccessibleContext().setAccessibleName(
				"Sec    SL   v0   vmid   tMax  distbeta  data_bfield_coeff.    b1    b2    b3   b4   r");
		jScrollPane1.getAccessibleContext().setAccessibleDescription("");

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void putNumbersFromTextFieldsIntoResetArrays(int gSuperlayer)
	{
		resetFitParsLow[gSuperlayer - 1][0] = Float.parseFloat(resetParLowTextField.getText());
		resetFitParsLow[gSuperlayer - 1][1] = Float.parseFloat(resetParLowTextField1.getText());
		resetFitParsLow[gSuperlayer - 1][2] = Float.parseFloat(resetParLowTextField2.getText());
		resetFitParsLow[gSuperlayer - 1][3] = Float.parseFloat(resetParLowTextField3.getText());
		resetFitParsLow[gSuperlayer - 1][4] = Float.parseFloat(resetParLowTextField4.getText());
		resetFitParsLow[gSuperlayer - 1][5] = Float.parseFloat(resetParLowTextField5.getText());
		resetFitParsLow[gSuperlayer - 1][6] = Float.parseFloat(resetParLowTextField6.getText());
		resetFitParsLow[gSuperlayer - 1][7] = Float.parseFloat(resetParLowTextField7.getText());
		resetFitParsLow[gSuperlayer - 1][8] = Float.parseFloat(resetParLowTextField8.getText());
		resetFitParsLow[gSuperlayer - 1][9] = Float.parseFloat(resetParLowTextField9.getText());

		resetFitPars[gSuperlayer - 1][0] = Float.parseFloat(resetParTextField.getText());
		resetFitPars[gSuperlayer - 1][1] = Float.parseFloat(resetParTextField1.getText());
		resetFitPars[gSuperlayer - 1][2] = Float.parseFloat(resetParTextField2.getText());
		resetFitPars[gSuperlayer - 1][3] = Float.parseFloat(resetParTextField3.getText());
		resetFitPars[gSuperlayer - 1][4] = Float.parseFloat(resetParTextField4.getText());
		resetFitPars[gSuperlayer - 1][5] = Float.parseFloat(resetParTextField5.getText());
		resetFitPars[gSuperlayer - 1][6] = Float.parseFloat(resetParTextField6.getText());
		resetFitPars[gSuperlayer - 1][7] = Float.parseFloat(resetParTextField7.getText());
		resetFitPars[gSuperlayer - 1][8] = Float.parseFloat(resetParTextField8.getText());
		resetFitPars[gSuperlayer - 1][9] = Float.parseFloat(resetParTextField9.getText());

		resetFitParsHigh[gSuperlayer - 1][0] = Float.parseFloat(resetParHighTextField.getText());
		resetFitParsHigh[gSuperlayer - 1][1] = Float.parseFloat(resetParHighTextField1.getText());
		resetFitParsHigh[gSuperlayer - 1][2] = Float.parseFloat(resetParHighTextField2.getText());
		resetFitParsHigh[gSuperlayer - 1][3] = Float.parseFloat(resetParHighTextField3.getText());
		resetFitParsHigh[gSuperlayer - 1][4] = Float.parseFloat(resetParHighTextField4.getText());
		resetFitParsHigh[gSuperlayer - 1][5] = Float.parseFloat(resetParHighTextField5.getText());
		resetFitParsHigh[gSuperlayer - 1][6] = Float.parseFloat(resetParHighTextField6.getText());
		resetFitParsHigh[gSuperlayer - 1][7] = Float.parseFloat(resetParHighTextField7.getText());
		resetFitParsHigh[gSuperlayer - 1][8] = Float.parseFloat(resetParHighTextField8.getText());
		resetFitParsHigh[gSuperlayer - 1][9] = Float.parseFloat(resetParHighTextField9.getText());

		// Now the reset array for step sizes
		resetFitParSteps[gSuperlayer - 1][0] = Float.parseFloat(resetParStepTextField.getText());
		resetFitParSteps[gSuperlayer - 1][1] = Float.parseFloat(resetParStepTextField1.getText());
		resetFitParSteps[gSuperlayer - 1][2] = Float.parseFloat(resetParStepTextField2.getText());
		resetFitParSteps[gSuperlayer - 1][3] = Float.parseFloat(resetParStepTextField3.getText());
		resetFitParSteps[gSuperlayer - 1][4] = Float.parseFloat(resetParStepTextField4.getText());
		resetFitParSteps[gSuperlayer - 1][5] = Float.parseFloat(resetParStepTextField5.getText());
		resetFitParSteps[gSuperlayer - 1][6] = Float.parseFloat(resetParStepTextField6.getText());
		resetFitParSteps[gSuperlayer - 1][7] = Float.parseFloat(resetParStepTextField7.getText());
		resetFitParSteps[gSuperlayer - 1][8] = Float.parseFloat(resetParStepTextField8.getText());
		resetFitParSteps[gSuperlayer - 1][9] = Float.parseFloat(resetParStepTextField9.getText());

		xNormLow = Float.parseFloat(xLowTextField.getText());
		xNormHigh = Float.parseFloat(xHighTextField.getText());
		if (xNormLow < 0.0)
		{
			xNormLow = 0.0;
		}
		if (xNormHigh > 1.0)
		{
			xNormHigh = 1.0;
		}
	}

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton3ActionPerformed
		System.exit(0);
	}// GEN-LAST:event_jButton3ActionPerformed

	private void jTextField29ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField29ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField29ActionPerformed

	private void jTextField28ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField28ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField28ActionPerformed

	// kp: This comboBox is for selecting the type of error for weighting the data points used in
	// the fits
	private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jComboBox3ActionPerformed
		xMeanErrorType = jComboBox3.getSelectedIndex();
		System.out.println("Selected: item " + (xMeanErrorType + 1) + " i.e., "
				+ jComboBox3.getSelectedItem());

	}// GEN-LAST:event_jComboBox3ActionPerformed

	// kp: This comboBox is for selecting a superlayer out of (1, 2, ..,6)
	private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jComboBox2ActionPerformed
		gSuperlayer = Integer.parseInt(jComboBox2.getSelectedItem().toString());
		putCCDBvaluesToResetArrays(gSector, ccdbVariation);
		assignParValuesToTextFields(gSector, gSuperlayer);
		printValuesOfSelectedAngularBins();
	}// GEN-LAST:event_jComboBox2ActionPerformed

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton2ActionPerformed
		putNumbersFromTextFieldsIntoResetArrays(gSuperlayer); // Just in case the 'reset' button is
																// not clicked
		// TestMainApp test = new TestMainApp ();
		// test.methodToBeInvokedByButtonClickInFitControlUI();
		// fitter.drawHistograms();
		// int Sec = gSector; //2;
		fitter.runFitterAndDrawPlots(this, jTextArea1, gSector, gSuperlayer,
				xMeanErrorType, xNormLow, xNormHigh, checkboxVal, checkBoxFixAll,
				resetFitParsLow, resetFitPars, resetFitParsHigh, resetFitParSteps, selectedAngleBins);
		//----------------- Update fit parameters value --------------
		updateFitValuesToResetArrays(gSector);
		assignParValuesToTextFields(gSector, gSuperlayer);
	}// GEN-LAST:event_jButton2ActionPerformed

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton1ActionPerformed
		putNumbersFromTextFieldsIntoResetArrays(gSuperlayer);
		// assignParValuesToTextFields(gSuperlayer); //Not necessary

		System.out.println(" Superlayer: " + gSuperlayer);
            
		for (int i = 0; i < nFitPars; i++)
		{
			if (checkboxVal[i] == true)
			{
				System.out.println("Parameter " + parName[i] + " has been fixed.");
			}
			System.out.println(String.format("Set vals for par=%d are %5.4f, %5.4f, %5.4f", (i + 1),
					resetFitParsLow[gSuperlayer - 1][i], resetFitPars[gSuperlayer - 1][i],
					resetFitParsHigh[gSuperlayer - 1][i]));
		}

		String fixParMessage = "";
		int fpn = 0;
		for (int i = 0; i < nFitPars; i++)
		{
			if (checkboxVal[i] == true)
			{
				fixParMessage = String.format("%s p%d ", fixParMessage, i + 1);
				fpn++;
			}
		}
		// Following will cause JOptionPane the dialog to be centered on the main GUI window
		// (JFrame)
		javax.swing.JFrame frame = this;
		if (fpn == 0)
		{
			JOptionPane.showMessageDialog(frame, "No parameter is fixed.");
		}
		else if (fpn == 1)
		{
			JOptionPane.showMessageDialog(frame, "Parameter " + fixParMessage + " is fixed.");
		}
		else if (fpn > 1)
		{
			JOptionPane.showMessageDialog(frame, "Parameters ( " + fixParMessage + ") are fixed.");
		}
		// Following will cause JOptionPane dialog to be centered on the user’s screen.
		// JOptionPane.showMessageDialog(null, "A basic JOptionPane message dialog"); //Works
	}// GEN-LAST:event_jButton1ActionPerformed

	// kp: This comboBox is for selecting a sector out of six
	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jComboBox1ActionPerformed
		gSector = Integer.parseInt(jComboBox1.getSelectedItem().toString());
		putCCDBvaluesToResetArrays(gSector, ccdbVariation);
		assignParValuesToTextFields(gSector, gSuperlayer);
		/*
		 * for(int i=0; i<9; i++) { if(checkboxVal[i]==true) System.out.println("Parameter " +
		 * parName[i] + " has been fixed."); }
		 */
	}// GEN-LAST:event_jComboBox1ActionPerformed

	//
	// This method is defined below automatically (couldn't move by hand), so I simply copied my
	// additions there.
	//
	// private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {
	// if (jCheckBox10.isSelected()) {
	// checkboxVal[9] = true;
	// } else {
	// checkboxVal[9] = false;
	// }
	// }

	private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox9ActionPerformed
		if (jCheckBox9.isSelected())
		{
			checkboxVal[8] = true;
		}
		else
		{
			checkboxVal[8] = false;
		}
	}// GEN-LAST:event_jCheckBox9ActionPerformed

	private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox8ActionPerformed
		if (jCheckBox8.isSelected())
		{
			checkboxVal[7] = true;
		}
		else
		{
			checkboxVal[7] = false;
		}
	}// GEN-LAST:event_jCheckBox8ActionPerformed

	private void jCheckBox7ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox7ActionPerformed
		if (jCheckBox7.isSelected())
		{
			checkboxVal[6] = true;
		}
		else
		{
			checkboxVal[6] = false;
		}
	}// GEN-LAST:event_jCheckBox7ActionPerformed

	private void jCheckBox6ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox6ActionPerformed
		if (jCheckBox6.isSelected())
		{
			checkboxVal[5] = true;
		}
		else
		{
			checkboxVal[5] = false;
		}
	}// GEN-LAST:event_jCheckBox6ActionPerformed

	private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox5ActionPerformed
		if (jCheckBox5.isSelected())
		{
			checkboxVal[4] = true;
		}
		else
		{
			checkboxVal[4] = false;
		}
	}// GEN-LAST:event_jCheckBox5ActionPerformed

	private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox4ActionPerformed
		if (jCheckBox4.isSelected())
		{
			checkboxVal[3] = true;
		}
		else
		{
			checkboxVal[3] = false;
		}
	}// GEN-LAST:event_jCheckBox4ActionPerformed

	private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox3ActionPerformed
		if (jCheckBox3.isSelected())
		{
			checkboxVal[2] = true;
		}
		else
		{
			checkboxVal[2] = false;
		}
	}// GEN-LAST:event_jCheckBox3ActionPerformed

	private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox2ActionPerformed
		if (jCheckBox2.isSelected())
		{
			checkboxVal[1] = true;
		}
		else
		{
			checkboxVal[1] = false;
		}
	}// GEN-LAST:event_jCheckBox2ActionPerformed

	private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox1ActionPerformed
		if (jCheckBox1.isSelected())
		{
			checkboxVal[0] = true;
		}
		else
		{
			checkboxVal[0] = false;
		}
	}// GEN-LAST:event_jCheckBox1ActionPerformed

	private void jTextField27ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField27ActionPerformed

	}// GEN-LAST:event_jTextField27ActionPerformed

	private void jTextField26ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField26ActionPerformed

	}// GEN-LAST:event_jTextField26ActionPerformed

	private void jTextField25ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField25ActionPerformed

	}// GEN-LAST:event_jTextField25ActionPerformed

	private void jTextField24ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField24ActionPerformed

	}// GEN-LAST:event_jTextField24ActionPerformed

	private void jTextField23ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField23ActionPerformed

	}// GEN-LAST:event_jTextField23ActionPerformed

	private void jTextField22ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField22ActionPerformed

	}// GEN-LAST:event_jTextField22ActionPerformed

	private void jTextField21ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField21ActionPerformed

	}// GEN-LAST:event_jTextField21ActionPerformed

	private void jTextField20ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField20ActionPerformed

	}// GEN-LAST:event_jTextField20ActionPerformed

	private void jTextField19ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField19ActionPerformed

	}// GEN-LAST:event_jTextField19ActionPerformed

	private void jTextField18ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField18ActionPerformed

	}// GEN-LAST:event_jTextField18ActionPerformed

	private void jTextField17ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField17ActionPerformed

	}// GEN-LAST:event_jTextField17ActionPerformed

	private void jTextField16ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField16ActionPerformed

	}// GEN-LAST:event_jTextField16ActionPerformed

	private void jTextField15ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField15ActionPerformed

	}// GEN-LAST:event_jTextField15ActionPerformed

	private void jTextField14ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField14ActionPerformed

	}// GEN-LAST:event_jTextField14ActionPerformed

	private void jTextField13ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField13ActionPerformed

	}// GEN-LAST:event_jTextField13ActionPerformed

	private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField12ActionPerformed

	}// GEN-LAST:event_jTextField12ActionPerformed

	private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField11ActionPerformed

	}// GEN-LAST:event_jTextField11ActionPerformed

	private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField10ActionPerformed

	}// GEN-LAST:event_jTextField10ActionPerformed

	private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField9ActionPerformed

	}// GEN-LAST:event_jTextField9ActionPerformed

	private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField8ActionPerformed

	}// GEN-LAST:event_jTextField8ActionPerformed

	private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField7ActionPerformed

	}// GEN-LAST:event_jTextField7ActionPerformed

	private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField6ActionPerformed

	}// GEN-LAST:event_jTextField6ActionPerformed

	private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField5ActionPerformed

	}// GEN-LAST:event_jTextField5ActionPerformed

	private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField4ActionPerformed

	}// GEN-LAST:event_jTextField4ActionPerformed

	private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField3ActionPerformed

	}// GEN-LAST:event_jTextField3ActionPerformed

	private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField2ActionPerformed
		// resetFitParsLow[gSuperlayer-1][1] = Float.parseFloat(resetParLowTextField1.getText());
	}// GEN-LAST:event_jTextField2ActionPerformed

	private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField1ActionPerformed
		// resetFitParsLow[gSuperlayer-1][0] = Float.parseFloat(resetParLowTextField.getText());
	}// GEN-LAST:event_jTextField1ActionPerformed

	private void jCheckBoxFixAllActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBoxFixAllActionPerformed
		if (jCheckBoxFixAll.isSelected())
		{
			checkBoxFixAll = true;
			jCheckBox1.setSelected(true);
			jCheckBox2.setSelected(true);
			jCheckBox3.setSelected(true);
			jCheckBox4.setSelected(true);
			jCheckBox5.setSelected(true);
			jCheckBox6.setSelected(true);
			jCheckBox7.setSelected(true);
			jCheckBox8.setSelected(true);
			jCheckBox9.setSelected(true);
			jCheckBox10.setSelected(true);
		}
		else
		{
			checkBoxFixAll = false;
			jCheckBox1.setSelected(false);
			jCheckBox2.setSelected(false);
			jCheckBox3.setSelected(false);
			jCheckBox4.setSelected(false);
			jCheckBox5.setSelected(false);
			jCheckBox6.setSelected(false);
			jCheckBox7.setSelected(false);
			jCheckBox8.setSelected(false);
			jCheckBox9.setSelected(false);
			jCheckBox10.setSelected(false);
		}

		// Whether jCheckBoxSelectAll slected or not, call the following methods
		// whenever there is action on this box. Thus, effectively, when the
		// selectAll button is selected or deselected, all the actions of the other
		// checkboxes will be performed just as they were selected individually.
		jCheckBox1ActionPerformed(evt);
		jCheckBox2ActionPerformed(evt);
		jCheckBox3ActionPerformed(evt);
		jCheckBox4ActionPerformed(evt);
		jCheckBox5ActionPerformed(evt);
		jCheckBox6ActionPerformed(evt);
		jCheckBox7ActionPerformed(evt);
		jCheckBox8ActionPerformed(evt);
		jCheckBox9ActionPerformed(evt);
		jCheckBox10ActionPerformed(evt);
	}// GEN-LAST:event_jCheckBoxFixAllActionPerformed

	// kp: This comboBox is for selecting the CCDB variation from which to initialize the t-vs-x
	// params
	private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jComboBox4ActionPerformed
		ccdbVariation = jComboBox4.getSelectedItem().toString();
		putCCDBvaluesToResetArrays(gSector, ccdbVariation);
		assignParValuesToTextFields(gSector, gSuperlayer);
	}// GEN-LAST:event_jComboBox4ActionPerformed

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton4ActionPerformed
		fitter.SliceViewer(fitter);
	}// GEN-LAST:event_jButton4ActionPerformed

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton5ActionPerformed
		// System.out.println("Residuals button Clicked.");
		fitter.showResidualDistributions(this, gSector, gSuperlayer, xNormLow, xNormHigh);
	}// GEN-LAST:event_jButton5ActionPerformed

	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton6ActionPerformed
		// System.out.println("Times button Clicked.");
		fitter.showTimeDistributions(this, gSector, gSuperlayer, xNormLow, xNormHigh);
	}// GEN-LAST:event_jButton6ActionPerformed

	private void jButton7ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton7ActionPerformed
		fitter.showBFieldDistributions(this, gSector, gSuperlayer, xNormLow, xNormHigh);
	}// GEN-LAST:event_jButton7ActionPerformed

	private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jCheckBox10ActionPerformed
		if (jCheckBox10.isSelected())
		{
			checkboxVal[9] = true;
		}
		else
		{
			checkboxVal[9] = false;
		}
	}// GEN-LAST:event_jCheckBox10ActionPerformed

	private void jTextField33ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField33ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField33ActionPerformed

	private void jTextField34ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField34ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField34ActionPerformed

	private void jTextField35ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField35ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField35ActionPerformed

	private void jTextField36ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField36ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField36ActionPerformed

	private void jTextField37ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField37ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField37ActionPerformed

	private void jTextField38ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField38ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField38ActionPerformed

	private void jTextField39ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField39ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField39ActionPerformed

	private void jTextField40ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField40ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField40ActionPerformed

	private void jTextField41ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField41ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField41ActionPerformed

	private void jTextField42ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jTextField42ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jTextField42ActionPerformed

	private void jButton8ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton8ActionPerformed
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				// In this context, 'this' wont be referring to the object of FitControlUI class
				// rather that of the anonymous Runnable class and so we'll get the following
				// error if we put only 'this' as the first argument:
				// incompatible types <anonymous Runnable> cannot be converted to FitControlUI

				// new FitControlBinSelectionUI(FitControlUI.this, fitter).setVisible(true);
				binSelector = new FitControlBinSelectionUI(FitControlUI.this, fitter);
				binSelector.setVisible(true);
			}
		});
	}// GEN-LAST:event_jButton8ActionPerformed

	private void jButton9ActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_jButton9ActionPerformed
		fitter.showLocalAngleDistributions(this, gSector, gSuperlayer, xNormLow, xNormHigh);
	}// GEN-LAST:event_jButton9ActionPerformed
	
	private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt)
	{
		String pStr = "  ";
		pStr += gSector + "\t" + gSuperlayer + "\t" + 0 + "\t";
		for (int p = 0; p < nFitPars; p++)
		{
			pStr += (float)fitter.fPars[p] + "\t";
		}
		pStr += "\t" + 0 + "\t" + 0 + "\t" + 0;
		boolean append_to_file = true;
		FileOutputWriter file = null;
		try
		{
			file = new FileOutputWriter(outFileForFitPars + TimeToDistanceFitter.runNumber + ".txt", append_to_file);
			file.Write(pStr);
			file.Close();
		}
		catch (IOException ex)
		{
			Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
		}				
	}

	private void printValuesOfSelectedAngularBins()
	{
		System.out.println("Tmp line for debug ..");
		// FitControlBinSelectionUI binSelector = new FitControlBinSelectionUI(this, fitter);
		if (!(binSelector == null))
		{
			selectedAngleBins = binSelector.checkboxVals;
		}

		// Following is simply to print the indices of the bins that were selected
		int countSelectedBins = 0;
		System.out.print("The selected angular bins (indices) are = (");
		for (int i = 0; i < selectedAngleBins.length; i++)
		{
			// if(selectedAngleBins[i] == true) System.out.println((i+1) + "th bin has been
			// selected");
			// System.out.println("selectedAngleBins["+i+"] = " + selectedAngleBins[i]);
			if (selectedAngleBins[i] == true)
			{
				if (countSelectedBins == 0)
				{
					System.out.print(i);
				}
				else
				{
					System.out.print(", " + i);
				}
				countSelectedBins++;
			}
		}
		System.out.println(")");
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[])
	{
		OrderOfAction OA = null;
		boolean isLinearFit = true;
		ArrayList<String> fileArray = null;

		TimeToDistanceFitter fitter = new TimeToDistanceFitter(OA, fileArray, isLinearFit);
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and
		 * feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (ClassNotFoundException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (InstantiationException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (IllegalAccessException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		// </editor-fold>
		// </editor-fold>
		// </editor-fold>
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				new FitControlUI(fitter).setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton6;
	private javax.swing.JButton jButton7;
	private javax.swing.JButton jButton8;
	private javax.swing.JButton jButton9;
	private javax.swing.JButton jButtonSave;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JCheckBox jCheckBox10;
	private javax.swing.JCheckBox jCheckBox2;
	private javax.swing.JCheckBox jCheckBox3;
	private javax.swing.JCheckBox jCheckBox4;
	private javax.swing.JCheckBox jCheckBox5;
	private javax.swing.JCheckBox jCheckBox6;
	private javax.swing.JCheckBox jCheckBox7;
	private javax.swing.JCheckBox jCheckBox8;
	private javax.swing.JCheckBox jCheckBox9;
	private javax.swing.JCheckBox jCheckBoxFixAll;
	private javax.swing.JComboBox<String> jComboBox1;
	private javax.swing.JComboBox<String> jComboBox2;
	private javax.swing.JComboBox<String> jComboBox3;
	private javax.swing.JComboBox<String> jComboBox4;
	private javax.swing.JLabel paramLabel;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel19;
	private javax.swing.JLabel lowLimLabel;
	private javax.swing.JLabel jLabel20;
	private javax.swing.JLabel jLabel21;
	private javax.swing.JLabel jLabel22;
	private javax.swing.JLabel jLabelChiSq;
	private javax.swing.JLabel jLabel23;
	private javax.swing.JLabel initValLabel;
	private javax.swing.JLabel uppLimLabel;
	private javax.swing.JLabel FitItLabel;
	private javax.swing.JLabel[] paramsLabel = new javax.swing.JLabel[nFitPars];
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextArea jTextArea1;
	private javax.swing.JTextField resetParLowTextField;
	private javax.swing.JTextField resetParTextField;
	private javax.swing.JTextField resetParTextField1;
	private javax.swing.JTextField resetParTextField2;
	private javax.swing.JTextField resetParTextField3;
	private javax.swing.JTextField resetParTextField4;
	private javax.swing.JTextField resetParTextField5;
	private javax.swing.JTextField resetParTextField6;
	private javax.swing.JTextField resetParTextField7;
	private javax.swing.JTextField resetParTextField8;
	private javax.swing.JTextField resetParHighTextField;
	private javax.swing.JTextField resetParLowTextField1;
	private javax.swing.JTextField resetParHighTextField1;
	private javax.swing.JTextField resetParHighTextField2;
	private javax.swing.JTextField resetParHighTextField3;
	private javax.swing.JTextField resetParHighTextField4;
	private javax.swing.JTextField resetParHighTextField5;
	private javax.swing.JTextField resetParHighTextField6;
	private javax.swing.JTextField resetParHighTextField7;
	private javax.swing.JTextField resetParHighTextField8;
	private javax.swing.JTextField xLowTextField;
	private javax.swing.JTextField xHighTextField;
	private javax.swing.JTextField resetParLowTextField2;
	private javax.swing.JTextField resetParLowTextField9;
	private javax.swing.JTextField resetParTextField9;
	private javax.swing.JTextField resetParHighTextField9;
	private javax.swing.JTextField resetParStepTextField;
	private javax.swing.JTextField resetParStepTextField1;
	private javax.swing.JTextField resetParStepTextField2;
	private javax.swing.JTextField resetParStepTextField3;
	private javax.swing.JTextField resetParStepTextField4;
	private javax.swing.JTextField resetParStepTextField5;
	private javax.swing.JTextField resetParStepTextField6;
	private javax.swing.JTextField resetParLowTextField3;
	private javax.swing.JTextField resetParStepTextField7;
	private javax.swing.JTextField resetParStepTextField8;
	private javax.swing.JTextField resetParStepTextField9;
	private javax.swing.JTextField resetParLowTextField4;
	private javax.swing.JTextField resetParLowTextField5;
	private javax.swing.JTextField resetParLowTextField6;
	private javax.swing.JTextField resetParLowTextField7;
	private javax.swing.JTextField resetParLowTextField8;
	// End of variables declaration//GEN-END:variables
}
