package com.github.dumpram.mesh.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.dumpram.mesh.gateway.MeshGateway;
import com.github.dumpram.mesh.node.Location;
import com.github.dumpram.mesh.node.MeshNode;

public class MeshRunner extends JFrame {
	
	/**
	 * Serial version.
	 */
	private static final long serialVersionUID = 96088882757497090L;

	private XYSeriesCollection dataset = new XYSeriesCollection();
	
	private JFreeChart chart;
	
	public MeshRunner(XYSeries gateway, XYSeries nodes) {
		setTitle("MeshSim v0.0.1");
		setSize(600, 600);
		
		dataset.addSeries(nodes);
		dataset.addSeries(gateway);
		
		chart = ChartFactory.createScatterPlot(
		"Node placement in test field", // title
		"X", "Y", // axis labels
		dataset, // dataset
		PlotOrientation.VERTICAL,
		true, // legend? yes
		true, // tooltips? yes
		true // URLs? no
		);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel); 
	}

	public static void main(String[] args) throws IOException {
		int nodeCount = 0;
		
		XYSeries gatewaySeries = new XYSeries("Gateway points");
		XYSeries nodeSeries = new XYSeries("Node points");
		
		if (args.length != 1) {
			System.out.println("Provide path to input file!");
			return;
		}
		
		List<String> confLines = Files.readAllLines(Paths.get(args[0]));		
		nodeCount = confLines.size();
		Thread[] threads = new Thread[nodeCount];
	
		List<MeshNode> remoteNodes = new ArrayList<MeshNode>();
		MeshGateway gateway = null;
		
		for (int i = 0; i < confLines.size(); i++) {
			String[] parts = confLines.get(i).split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			boolean isGateway = (i == 0) ? true : false;
			if (isGateway) {
				gatewaySeries.add(x, y);
				gateway = new MeshGateway(i, new Location(x, y));
				threads[i] = new Thread(gateway);
			} else {
				nodeSeries.add(x, y);
				MeshNode current = new MeshNode(i, new Location(x, y));
				threads[i] = new Thread(current);
				remoteNodes.add(current);
				gateway.addNode(current);
			}
		}
		
		for (int i = 0; i < confLines.size(); i++) {
			threads[i].start();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MeshRunner(gatewaySeries, nodeSeries).setVisible(true);	
			}
		});	
	}
}
