package com.github.dumpram.mesh.node;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import com.github.dumpram.mesh.data.ConfigAckMeshData;
import com.github.dumpram.mesh.data.MeshPacket;
import com.github.dumpram.mesh.data.NormalStateMeshData;
import com.github.dumpram.mesh.data.StartMeshData;
import com.github.dumpram.mesh.network.MeshNetwork;

public class MeshNode extends JComponent implements Runnable, Comparable<MeshNode> {

	private static final long serialVersionUID = 1L;

	protected static final int HIGHEST_START_NUMBER = 5; // this can be number
															// of nodes in list

	protected final int MONOTONIC_INTERVAL = 10000; // millis

	private static final int DELTA = 3000; // milliseconds

	private Location location;

	protected boolean isGateway;

	protected ConfigData configData;

	protected ConfigAckMeshData configAckMeshData;

	protected NormalStateMeshData currentMeshData;

	private MeshNetwork meshNetwork;

	private boolean configDataSet;

	private boolean data = false;

	protected int id;

	private int startNumber; // for know is same as id

	private boolean isConfigured;

	protected boolean isListening = true;

	private boolean sleeping = false;

	private boolean startEnable = false;

	private boolean nextInterval;

	private String nodeAction;

	private int highestStartNumber;

	protected long awakeTime = 0;

	protected long start;

	protected Image img;

	public MeshNode(int id, Location location) {
		this.location = location;
		this.id = id;
		meshNetwork = MeshNetwork.getInstance();
		try {
			img = ImageIO.read(new File("antenna.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * There is the list of numbers. Those numbers are id of nodes. Current node
	 * finds itself. Node then tries to send the list to neighbour nodes.
	 */
	@Override
	public void run() {
		onWakeUp();
		nextInterval = false;
		while (!startEnable) {
			sleep(10);
		}
		stopListening();
		if (!configData.getChildNodes().isEmpty()) {
			for (MeshNode i : configData.getChildNodes()) {
				meshNetwork.sendStartData(this, i, new StartMeshData(highestStartNumber));
			}
		}
		Timer t = new Timer();
		t.schedule(new NodeTimerTask(this), MONOTONIC_INTERVAL, MONOTONIC_INTERVAL);
		do {
			setSleeping(false);
			waitForDataFromMesh();
			forwardDataToMesh();
			setSleeping(true);
			log("Next interval pending!");
			while (!nextInterval) {
				sleep(10);
			}
			nextInterval = false;
		} while (true);
	}

	private void onWakeUp() {
		if (isConfigured) {
			return;
		}
		configureNode();
		waitForConfigAck();
		waitForStart();
	}

	/**
	 * Wait for node list. Remove yourself from list (maybe create copy at this
	 * point and to memorise parent and children nodes nodes which have the same
	 * parent, should know that every node should get the new configuration data
	 * this real situation in which every parent node is responsible for its
	 * children nodes with same parent must be excluded from each other lists
	 * however it would be good to have brothers and sisters list so if some
	 * nodes parent dies it can always turn to its ant or uncle propagate data
	 * should wait some time before it decides to continue with normal work
	 * after configuration, if successfully configured node should wait for
	 * start of normal work waiting means listening for start beacon from
	 * gateway.
	 */
	private void configureNode() {
		waitConfigData();
		propagateConfigData();

		isConfigured = true;
	}

	protected void waitForConfigAck() {
		log(configData.getChildNodes().toString());
		this.configAckMeshData = new ConfigAckMeshData(new ArrayList<Integer>());
		log("My start number is: " + startNumber);
		configAckMeshData.addNumberToList(startNumber);

		if (!configData.getChildNodes().isEmpty()) {
			startListening();
			while (!configAck()) {
				sleep(10);
			}
			log("Got config ack");
			log(configData.getChildNodes().toString());
			stopListening();
		}
		log("Send ack to parent: " + configData.getParent());
		meshNetwork.sendConfigAckToParent(this, configData.getParent(), configData.getParent().configData);
	}

	/**
	 * Node waits for configuration data.
	 */
	private void waitConfigData() {
		startListening();
		while (!configDataSet) {
			sleep(10);
		}
		stopListening();
	}

	/**
	 * When some node successfully probes its child, child should wait sometime
	 * before it starts probing further because parent can find more children
	 * and possibly it could happen that the children are close enough, but we
	 * don't want that one child is dominant over other, pretending as he or she
	 * is the parent, because it is not. SOLUTION: we can probe all nodes and
	 * then send data
	 */
	protected void propagateConfigData() {
		List<MeshNode> potentialChildren = configData.getChildNodes();
		List<MeshNode> notChildren = new ArrayList<MeshNode>();
		List<MeshNode> realChildren = new ArrayList<MeshNode>();
		for (MeshNode i : potentialChildren) {
			if (!meshNetwork.probe(this, i)) {
				notChildren.add(i);
			} else {
				realChildren.add(i);
			}
		}
		int startNumberCnt = 1;
		for (MeshNode i : realChildren) {
			i.setStartNumber((startNumberCnt++) + this.startNumber);
			ConfigData forExport = new ConfigData(i, notChildren);
			forExport.addParent(this);
			forExport.removeChildNode(i);
			meshNetwork.sendConfigData(i, this, forExport);
			configData.configAckMap.put(i, false);
		}
		configData.setChildNodes(realChildren);
	}

	private void waitForDataFromMesh() {
		int deltas = 0;
		this.currentMeshData = new NormalStateMeshData();
		currentMeshData.addPacket(new MeshPacket(id, meshNetwork.getMeshDataGenerator().getData(id)));
		Collections.sort(configData.getChildNodes());
		for (int i = 0; i < configData.getChildNodes().size(); i++) {
			int current = highestStartNumber - configData.getChildNodes().get(i).startNumber;
			realSleep(deltas, current);
			deltas += (current - deltas);
			getDataFromNode(configData.getChildNodes().get(i).id);
		}
		int current = highestStartNumber - startNumber;
		realSleep(deltas, current);
	}

	private void realSleep(int deltas, int current) {
		setSleeping(true);
		sleep((current - deltas) * DELTA);
		setSleeping(false);
	}

	public ConfigAckMeshData getConfigAckMeshData() {
		return configAckMeshData;
	}

	protected void getDataFromNode(int i) {
		log("Waiting data from: " + i);
		startListening();
		while (!data) {
			sleep(1);
		}
		data = false; // reset for next node
		log("Got data from: " + i);
		stopListening();
	}

	protected boolean configAck() {
		boolean forExport = true;
		for (MeshNode i : configData.configAckMap.keySet()) {
			forExport &= configData.configAckMap.get(i);
		}
		return forExport;
	}

	private void waitForStart() {
		startListening();
	}

	private void forwardDataToMesh() {
		setSleeping(false);
		log("Forward via parent: " + configData.getParent());
		int retryTime = 100;
		// int cnt = 0;
		while (!meshNetwork.setMeshData(this, configData.getParent(), currentMeshData) && retryTime-- > 0) {
			sleep(1);
			// cnt++;
		}
		// log("Number of misses: " + cnt);
	}

	public Location getNodeLocation() {
		return location;
	}

	public int getId() {
		return id;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setConfigData(ConfigData configData) {
		this.configData = configData;
		configDataSet = true;
	}

	public void setMeshData(NormalStateMeshData data) {
		this.data = true;
		currentMeshData.addPackets(data.getPackets());
	}

	protected void stopListening() {
		isListening = false;
		repaint();
	}

	private void startListening() {
		isListening = true;
		repaint();
	}

	public boolean isListening() {
		return isListening;
	}

	public boolean isSleeping() {
		return sleeping;
	}

	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
		if (!sleeping) {
			start = System.nanoTime();
		}
		if (sleeping) {
			awakeTime += System.nanoTime() - start;
		}
		repaint();
	}

	public int getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
	}

	protected void log(String input) {
		nodeAction = input;
		repaint();
		System.out.println("Node " + id + ": " + input);
	}

	protected void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Rectangle getBounds() {
		int height = 130;
		int width = 300;
		int x = (int) location.getX() * 140 + 2000 / 2 - width / 2;
		int y = -(int) location.getY() * 140 + 2000 / 2 - height / 2;
		return new Rectangle(x, y, width, height);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(110, 310);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Color c = (isSleeping() && !isListening) ? Color.GREEN : Color.ORANGE;
		boolean isParent = !configData.getChildNodes().isEmpty();
		if (isParent && !isGateway) {
			c = Color.MAGENTA;
		}
		
		if (meshNetwork.getStatus()) {
			if (isGateway) {
				try {
					img = ImageIO.read(new File("router.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			g.drawImage(img, 0, 0, getBounds().width / 2, getBounds().height / 2, c, this);
		} else {
			Rectangle rect = getBounds();
			g.drawOval(0, 0, 10, 10);
			g.drawRect(0, 0, rect.width - 30, rect.height - 10);
			g.setColor(c);
			g.fillRect(0, 0, rect.width - 30, rect.height - 10);
			g.setColor(Color.BLACK);
			Font f = g.getFont();
			Font f2 = new Font(f.getFontName(), Font.BOLD, f.getSize() + 1);
			g.setFont(f2);
			g.drawString("Node id: " + id, 0, 10);
			g.drawString("Node action:" + nodeAction, 0, 30);
			g.drawString("Node start number: " + startNumber, 0, 50);
			long time = (!isGateway) ? (long) (awakeTime / 1e6) : meshNetwork.getMillis();
			g.drawString("Awake time:" + time + " ms", 0, 70);
			double energyConsumption = 20 * time / 3600.0;
			String result = String.format("%.2f", energyConsumption);
			g.drawString("Energy consumption:" + result + "mAh", 0, 90);
			double estimatedLiving = 4000.0 / (energyConsumption / ((meshNetwork.getMillis() / 1000.0) / 3600.0));
			String estimation = String.format("%.2f", estimatedLiving);
			g.drawString("Estimated living:" + estimation + " h", 0, 110);
		}
	}

	public void nextInterval() {
		nextInterval = true;
	}

	public void startData(StartMeshData data) {
		startEnable = true;
		highestStartNumber = data.getHighestStartNumber();
	}

	public void setConfigAckMeshData(ConfigAckMeshData configAckMeshData) {
		this.configAckMeshData = configAckMeshData;
	}

	@Override
	public int compareTo(MeshNode o) {
		return -Integer.compare(startNumber, o.startNumber);
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeshNode other = (MeshNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
