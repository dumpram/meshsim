package com.github.dumpram.mesh.runner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.dumpram.mesh.gateway.MeshGateway;
import com.github.dumpram.mesh.network.MeshNetwork;
import com.github.dumpram.mesh.node.Location;
import com.github.dumpram.mesh.node.MeshNode;

public class MeshRunner extends JFrame {
	
	private static final long serialVersionUID = 96088882757497090L;

	private XYSeriesCollection dataset = new XYSeriesCollection();
	
	private JFreeChart chart;
	
	private JScrollPane pane;
	
	private JPanel panel = new JPanel();
	
	private JToolBar toolbar = new JToolBar();
	
	public MeshRunner(List<MeshNode> nodes, XYSeries gatewaySeries, XYSeries nodeSeries, boolean
			map) {
		setTitle("MeshSim v0.0.1");
		setSize(600, 600);
		setLayout(new BorderLayout());
		
		if (map) {
			initChart(gatewaySeries, nodeSeries);
		} else {
			initLogMap(nodes);
		}
		final JButton button = new JButton("Show status");
		toolbar.add(button);
		
		MeshNetwork.getInstance().setStatus(true);
		
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean status = MeshNetwork.getInstance().getStatus();
				MeshNetwork.getInstance().setStatus(!status);
				String text = (status) ? "Hide status" : "Show status";
				button.setText(text);
				repaint();
			}
		});
		
		add(toolbar, BorderLayout.BEFORE_FIRST_LINE);
	}

	private void initLogMap(List<MeshNode> nodes) {
		panel.setLayout(null);
		for (MeshNode i : nodes) {
			i.setBounds(i.getBounds());
		}
		for (MeshNode i : nodes) {
			panel.add(i);
		};
		panel.setPreferredSize(new Dimension(2000, 2000));
		pane = new JScrollPane(panel);
		add(pane, BorderLayout.CENTER);
		
		LegendComponent comp = new LegendComponent(pane.getViewport());
		comp.setBounds(comp.getBounds());
		panel.add(comp);
	}

	private void initChart(XYSeries gatewaySeries, XYSeries nodeSeries) {
		dataset.addSeries(nodeSeries);
		dataset.addSeries(gatewaySeries);
		
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
		
		List<Integer> ids = new ArrayList<Integer>();
		
		for (int i = 0; i < confLines.size(); i++) {
			String[] parts = confLines.get(i).split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			boolean isGateway = (i == 0) ? true : false;
			MeshNode current = new MeshNode(i, new Location(x, y));
			if (isGateway) {
				gatewaySeries.add(x, y);
				gateway = new MeshGateway(i, new Location(x, y));
				threads[i] = new Thread(gateway);
			} else {
				nodeSeries.add(x, y);
				threads[i] = new Thread(current);
				gateway.addNode(current);
				remoteNodes.add(current);
				ids.add(i);
			}
		}
		remoteNodes.add(gateway);
		MeshDataGenerator generator = new MeshDataGenerator(ids);
		MeshNetwork.getInstance().setMeshDataGenerator(generator);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MeshRunner runner = new MeshRunner(remoteNodes, gatewaySeries, nodeSeries, false);	
				runner.setVisible(true);
			}
		});	
		for (int i = 0; i < confLines.size(); i++) {
			threads[i].start();
		}		
	}
}
