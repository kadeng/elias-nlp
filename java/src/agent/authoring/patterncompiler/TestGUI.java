/*
 *  Copyright 2001 - Kai Londenberg
 *  All rights reserved
 *  Alle Rechte vorbehalten.
 */
package elias.agent.authoring.patterncompiler;

import java.awt.*;
import elias.agent.authoring.patterncompiler.transformation.JSGFPatternTransformator;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java_cup.runtime.*;
import javax.swing.event.*;
import java.util.*;
import com.borland.jbcl.layout.*;
import java.beans.*;
import elias.agent.runtime.pattern.Matcher;
import elias.agent.runtime.pattern.PatternStructure;
import elias.agent.runtime.pattern.DefaultTokenizedInput;
import elias.agent.runtime.pattern.DefaultMatchResult;
import elias.agent.runtime.base.FastInputTokenizer;

public class TestGUI extends JFrame {
	private static Hashtable empty_table = new Hashtable(1);
	JLabel statusBar = new JLabel();
	PatternStructure pstructures[];
	BorderLayout borderLayout1 = new BorderLayout();
	byte serializedPattern[];
	JPanel jPanel1 = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	JTextField inputField = new JTextField();
	parser patternparser = new parser();
	PatternStructure pstructure;
	FastInputTokenizer tokenizer = new FastInputTokenizer();
	Matcher matcher = new Matcher(10000);
	TextArea patternArea = new TextArea();
	JPanel jPanel2 = new JPanel();
	JButton performanceTestButton = new JButton();
	JButton parseButton = new JButton();
	JSlider iterationSlider = new JSlider();
	JPanel buttonPanel = new JPanel();
	BorderLayout borderLayout3 = new BorderLayout();
	JPanel jPanel3 = new JPanel();
	JPanel jPanel4 = new JPanel();
	JLabel optimizationLevelLabel = new JLabel();
	JSlider optimizationLevelSlider = new JSlider();
	JLabel jLabel1 = new JLabel();
	JPanel jPanel5 = new JPanel();
	JLabel instanceCountLabel = new JLabel();
	JSlider instanceCountSlider = new JSlider();
	JLabel jLabel2 = new JLabel();
	VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
	JPanel jPanel6 = new JPanel();
	JLabel matchLabel = new JLabel();
	BorderLayout borderLayout4 = new BorderLayout();
	DefaultMatchResult mr = null;
	JComboBox matchResultSelectorBox = new JComboBox();
	JButton sddORsButton = new JButton();
  JButton toJSGFButton = new JButton();


	//Frame konstruieren
	public TestGUI() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void updateMatchResultDisplay(DefaultMatchResult matchres) {
		mr = matchres;
		matchLabel.setText("");
		matchResultSelectorBox.setEnabled(false);
		matchResultSelectorBox.removeAllItems();
		if (matchres == null) {
			return;
		}
		System.out.println("Match result: " + mr.toString());
		Iterator en = matchres.getMatchNames();
		if (!en.hasNext()) {
			return;
		}
		while (en.hasNext()) {
			matchResultSelectorBox.addItem(en.next());
		}
		matchResultSelectorBox.setEditable(false);
		matchResultSelectorBox.setSelectedIndex(0);
		matchResultSelectorBox.setEnabled(true);
		matchLabel.setText(mr.getMatch(matchResultSelectorBox.getSelectedItem().toString()));
	}


	//Ueberschreiben, damit das Programm bei Herunterfahren des Systems beendet werden kann
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}


	void parseButton_actionPerformed(ActionEvent e) {
		try {
			Symbol sym;
			patternparser.setScanner(new DefaultLexer(patternArea.getText(), empty_table));
			sym = patternparser.parse();
			pstructure = (PatternStructure) sym.value;
			//pstructure.getStart().printInfo(0);
			System.out.println("Compiled successfully");
			statusBar.setText("Compiled successfully");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(byteStream);
			os.writeObject(pstructure);
			os.close();
			serializedPattern = byteStream.toByteArray();
		} catch (Exception egg) {
			pstructure = null;
			statusBar.setText(egg.getMessage());
			egg.printStackTrace();
		}
	}


	void inputField_actionPerformed(ActionEvent e) {
		try {
			Symbol sym;
			patternparser.setScanner(new DefaultLexer(patternArea.getText(), empty_table));
			sym = patternparser.parse();
			pstructure = (PatternStructure) sym.value;
			statusBar.setText("Compiled successfully");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(byteStream);
			os.writeObject(pstructure);
			os.close();
			serializedPattern = byteStream.toByteArray();
		} catch (Exception egg) {
			pstructure = null;
			statusBar.setText(egg.getMessage());
			return;
		}
		if (pstructure == null) {
			statusBar.setText("No valid pattern present");
			return;
		}
		try {
			String instr = inputField.getText();
			DefaultTokenizedInput input = new DefaultTokenizedInput(instr, tokenizer.Tokenize(instr));
			System.out.println(input.getTokensAsString(","));
			boolean match = matcher.match(pstructure, input);
			if (match) {
				mr = matcher.createMatchResult(pstructure, input);
				updateMatchResultDisplay(mr);
			}
			statusBar.setText(matcher.match(pstructure, input) ? "Matched" : "No match");
		} catch (Throwable t) {
			statusBar.setText("Exception: " + t.getMessage());
			System.err.println(t.getMessage());
			t.printStackTrace();
		}

	}


	void iterationSlider_stateChanged(ChangeEvent e) {
		statusBar.setText("Iterations for Performance Test: #" + iterationSlider.getValue());
	}


	void performanceTestButton_actionPerformed(ActionEvent e) {
		long start;
		long stop;
		PatternStructure pstructures[];
		if (pstructure == null) {
			statusBar.setText("No valid pattern present");
			return;
		}
		int instanceCount = instanceCountSlider.getValue();
		pstructures = new PatternStructure[instanceCount];
		System.gc();
		statusBar.setText("Loading " + instanceCount + " copies of Pattern");
		this.repaint(1);
		ByteArrayInputStream ins = null;
		ObjectInputStream ois = null;
		try {
			ins = new ByteArrayInputStream(serializedPattern);
			ois = new ObjectInputStream(ins);
			ins.mark(serializedPattern.length * 2);
			for (int i = 0; i < instanceCount; i++) {
				pstructures[i] = (PatternStructure) ois.readObject();
				ins.reset();
			}
		} catch (Exception igsa) {
			statusBar.setText("Error loading Patterns: " + igsa.getMessage());
			igsa.printStackTrace();
		}
		try {
			if (ois != null) {
				ois.close();
			}
		} catch (Exception xjjs) {
		}
		try {
			if (ins != null) {
				ins.close();
			}
		} catch (Exception sjjs) {
		}
		ins = null;
		ois = null;
		try {
			String instr = inputField.getText();
			DefaultTokenizedInput input = new DefaultTokenizedInput(instr, tokenizer.Tokenize(instr));
			boolean res = false;
			statusBar.setText("Patterns loaded - Running performance test..");
			int count = iterationSlider.getValue();
			start = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				res = matcher.match(pstructure, input);
			}
			stop = System.currentTimeMillis();
			statusBar.setText((res ? "Matched" : "Did not match") + " with a rate of " + (count * 1000 / (stop - start)) + " iterations/second");
			System.gc();
		} catch (Throwable t) {
			statusBar.setText("Exception: " + t.getMessage());
			System.err.println(t.getMessage());
			t.printStackTrace();
			System.gc();
		}
	}


	void optimizationLevelSlider_stateChanged(ChangeEvent e) {
		optimizationLevelLabel.setText("" + optimizationLevelSlider.getValue());
		PatternStructure.optimization_level = optimizationLevelSlider.getValue();
	}


	void instanceCountSlider_stateChanged(ChangeEvent e) {
		instanceCountLabel.setText("" + instanceCountSlider.getValue());
	}


	void matchResultSelectorBox_actionPerformed(ActionEvent e) {
		if (mr == null) {
			return;
		}
		Object name = matchResultSelectorBox.getSelectedItem();
		if (name == null) {
			return;
		}
		String value = mr.getMatch(matchResultSelectorBox.getSelectedItem().toString());
		if (value == null) {
			return;
		}
		matchLabel.setText(value);
	}


	void sddORsButton_actionPerformed(ActionEvent e) {
		char data[] = patternArea.getText().toCharArray();
		int max = data.length;
		for (int i = 0; i < max; i++) {
			if (data[i] == '\n') {
				data[i] = '|';
			}
		}
		patternArea.setText(new String(data));
	}


	//Initialisierung der Komponente
	private void jbInit() throws Exception {
		this.getContentPane().setLayout(borderLayout1);
		this.setSize(new Dimension(506, 392));
		this.setTitle("Pattern Engine Test");
		statusBar.setText("Status");
		jPanel1.setLayout(borderLayout2);
		inputField.setPreferredSize(new Dimension(400, 20));
		inputField.setText("Test input");
		inputField.addActionListener(
			new java.awt.event.ActionListener() {

				public void actionPerformed(ActionEvent e) {
					inputField_actionPerformed(e);
				}
			});
		patternArea.setText("Pattern");
		performanceTestButton.setText("Test Performance");
		performanceTestButton.addActionListener(
			new java.awt.event.ActionListener() {

				public void actionPerformed(ActionEvent e) {
					performanceTestButton_actionPerformed(e);
				}
			});
		parseButton.setText("Compile Pattern");
		parseButton.addActionListener(
			new java.awt.event.ActionListener() {

				public void actionPerformed(ActionEvent e) {
					parseButton_actionPerformed(e);
				}
			});
		iterationSlider.setValue(400000);
		iterationSlider.setMaximum(2000000);
		iterationSlider.setToolTipText("Number of Iterations for performance Test");
		iterationSlider.addChangeListener(
			new javax.swing.event.ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					iterationSlider_stateChanged(e);
				}
			});
		jPanel2.setLayout(borderLayout3);
		optimizationLevelLabel.setText("00");
		optimizationLevelLabel.setText("" + PatternStructure.optimization_level);
		optimizationLevelSlider.setMaximum(5);
		optimizationLevelSlider.addChangeListener(
			new javax.swing.event.ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					optimizationLevelSlider_stateChanged(e);
				}
			});
		optimizationLevelSlider.setMinimumSize(new Dimension(20, 24));
		optimizationLevelSlider.setValue(PatternStructure.optimization_level);
		jLabel1.setText("Optimization Level");
		instanceCountLabel.setText("1");
		jLabel2.setText("Instance Count");
		jPanel3.setLayout(verticalFlowLayout1);
		instanceCountSlider.setValue(1);
		instanceCountSlider.setMaximum(30000);
		instanceCountSlider.addChangeListener(
			new javax.swing.event.ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					instanceCountSlider_stateChanged(e);
				}
			});
		matchLabel.setToolTipText("");
		matchLabel.setText("     ");
		jPanel6.setLayout(borderLayout4);
		matchResultSelectorBox.setMinimumSize(new Dimension(150, 21));
		matchResultSelectorBox.setPreferredSize(new Dimension(150, 21));
		matchResultSelectorBox.addActionListener(
			new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					matchResultSelectorBox_actionPerformed(e);
				}
			});
		sddORsButton.setText("Add ORs");
		sddORsButton.addActionListener(
			new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sddORsButton_actionPerformed(e);
				}
			});
		toJSGFButton.setText("To JSGF");
    toJSGFButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toJSGFButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(statusBar, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(inputField, BorderLayout.SOUTH);
		jPanel1.add(patternArea, BorderLayout.CENTER);
		this.getContentPane().add(jPanel2, BorderLayout.NORTH);
		jPanel2.add(buttonPanel, BorderLayout.CENTER);
    buttonPanel.add(toJSGFButton, null);
		buttonPanel.add(sddORsButton, null);
		buttonPanel.add(parseButton, null);
		buttonPanel.add(performanceTestButton, null);
		buttonPanel.add(iterationSlider, null);
		jPanel2.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(jPanel6, null);
		jPanel6.add(matchLabel, BorderLayout.CENTER);
		jPanel6.add(matchResultSelectorBox, BorderLayout.WEST);
		jPanel3.add(jPanel4, null);
		jPanel4.add(jLabel1, null);
		jPanel4.add(optimizationLevelSlider, null);
		jPanel4.add(optimizationLevelLabel, null);
		jPanel3.add(jPanel5, null);
		jPanel5.add(jLabel2, null);
		jPanel5.add(instanceCountSlider, null);
		jPanel5.add(instanceCountLabel, null);
		updateMatchResultDisplay(null);
	}

  void toJSGFButton_actionPerformed(ActionEvent e) {
    try {
      JSGFPatternTransformator jsgfTrans = new JSGFPatternTransformator();
      patternArea.setText(jsgfTrans.transformPatternDefinition(patternArea.getText(), new Hashtable(), "package"));
    } catch (ParseException exx) {
      statusBar.setText("Parse exception: " + exx);
    } catch(Exception he) {
        statusBar.setText("Exception");
        he.printStackTrace();
    }

  }

}
