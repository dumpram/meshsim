package com.github.dumpram.mesh.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.github.dumpram.mesh.data.AbstractMeshData;
import com.github.dumpram.mesh.data.MeshDataType;
import com.github.dumpram.mesh.network.MeshNetwork;

public class MeshNode implements Runnable, Comparable<MeshNode> {
	
	private static final int HIGHEST_START_NUMBER = 5; // this can be number of nodes in list
	
	private static final int DELTA = 500; // millis
	
	private Location location;
	
	protected ConfigData configData;
	
	private MeshData currentMeshData;
	
	private MeshNetwork meshNetwork;
	
	private boolean configDataSet;
	
	private boolean data = false;
	
	private int id;
	
	private int startNumber; // for know is same as id
	
	private boolean isConfigured;
	
	private boolean isListening = true;
	
	private boolean startEnable = false;
	
	private MeshNodeState state;
	
	private MeshNodeEvent event;
	
	private boolean nextInterval;
	
	public MeshNode(int id, Location location) {
		this.location = location;
		this.id = id;
		meshNetwork = MeshNetwork.getInstance();
	}
	
	// postoji niz brojeva
	// to su identifikatori čvorova
	// trenutni čvor nađe sebe i proba poslati svima uokolo
	// izbaci sebe i ostavi preostale čvorove i pošalje im to
	// potrebno bi bilo poslati i neku konfiguracijsku shemu
	// koja ce pospojiti cvorove medjusobno
	// alternativa je algoritam koji ce stvoriti optimalno stablo
	// gateway je ustvari posebna vrsta čvora s beskonačnim bufferom
	// i podaci se iz njega mogu neprestano vaditi
	@Override
	public void run() {
		onWakeUp();
		nextInterval = false;
		while(!startEnable) {
			sleep(1);
		}
		if (!configData.getChildNodes().isEmpty()) {
			for (MeshNode i : configData.getChildNodes()) {
				meshNetwork.sendStartData(this, i, null);
			}
		}
		Timer t = new Timer();
		t.schedule(new NodeTimerTask(this), 0, 20 * 1000);
		do {
			waitForDataFromMesh();
			forwardDataToMesh();
			//sleep(10000000);
			while(!nextInterval) {
				sleep(1);
			}
			nextInterval = false;
		} while (true);
	}

	private void waitForDataFromMesh() {
		int deltas = 0;
		Collections.sort(configData.getChildNodes());
		for (int i = 0; i < configData.getChildNodes().size(); i++) {
			int current = HIGHEST_START_NUMBER - configData.getChildNodes().get(i).startNumber;
			log("Child index: " + i + " deltas: " + deltas + " current: " + current);
			sleep((current - deltas) * DELTA);
			log("Child index: " + i + " deltas: " + deltas);
			deltas += (current - deltas);
			getDataFromNode(configData.getChildNodes().get(i).id);
		}
		int current = HIGHEST_START_NUMBER - startNumber;
		sleep((current - deltas) * DELTA);
	}
	
	private void onWakeUp()  {
		if (isConfigured) {
			return;
		}
		configureNode();
		waitForConfigAck();// 
		waitForStart();
	}
	
	private void configureNode() {
		waitConfigData(); // wait for node list
		propagateConfigData(); // remove yourself from list (maybe create copy at
		// this point and to memorise parent and children nodes
		// nodes which have the same parent, should know that
		// every node should get the new configuration data
		// this real situation in which every parent node
		// is responsible for its children
		// nodes with same parent must be excluded from each other lists
		// however it would be good to have brothers and sisters list
		// so if some nodes parent dies it can always turn to its ant 
		// or uncle
		// propagate data should wait some time before it decides
		// to continue with normal work
		// after configuration, if successfully configured 
		// node should wait for start of normal work
		// waiting means listening for start beacon from gateway
		isConfigured = true;
	}
	
	// when some node successfully probes its child
	// child should wait sometime before it starts probing
	// further because parent can find more children 
	// and possibly it could happen that the children are 
	// close enough, but we don't want that one child is
	// dominant over other, pretending as he or she is the
	// parent, because it is not
	// SOLUTION: we can probe all nodes and then send data
	protected void propagateConfigData() {
		List<MeshNode> potentialChildren = configData.getChildNodes();
		List<MeshNode> notChildren = new ArrayList<MeshNode>();
		List<MeshNode> realChildren = new ArrayList<MeshNode>();
		for (MeshNode i : potentialChildren) {
			if(!meshNetwork.probe(this, i)) {
				notChildren.add(i);
			} else {
				realChildren.add(i);
			}
		}
		for (MeshNode i : realChildren) {
			i.setStartNumber(realChildren.indexOf(i) + this.startNumber);
			ConfigData forExport = new ConfigData(i, notChildren);
			forExport.addParent(this);
			forExport.removeChildNode(i);
			meshNetwork.sendConfigData(i, this, forExport);
			configData.configAckMap.put(i, false);
		}
		configData.setChildNodes(realChildren);
	}
	
	private void waitConfigData() {
		startListening();
		while(!configDataSet) {
			sleep(10);
		}
		stopListening();
	}
	
	protected void waitForConfigAck() {
		log(configData.getChildNodes().toString());
		
		if (!configData.getChildNodes().isEmpty()) {
			startListening();
			while(!configAck()) {
				sleep(10);
			}
			log("Got config ack");
			log(configData.getChildNodes().toString());
			stopListening();
		}
		log("Sending ack to parent: " + configData.getParent());
		meshNetwork.sendConfigAckToParent(this, configData.getParent(), configData.getParent().configData);	
	}
	
	private void getDataFromNode(int i) {
		log("Waiting for data from: " + i);
		startListening();
		while(!data) {
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
		// i think this should be handled somehow
		stopListening();
	}

	private void getDataFromMesh() {
		if (configData.getChildNodes().isEmpty()) {
			return;
		}
		startListening();
		waitForMeshDataWithTimeout();
		stopListening();
	}
	
	private void waitForMeshDataWithTimeout() {
		// do timeout somehow 
	}

	private void forwardDataToMesh() {
		log("Forwarding data to mesh via parent: " + configData.getParent());
		int retryTime = 100;
		int cnt = 0;
		while (!meshNetwork.setMeshData(this, configData.getParent()) && retryTime-- > 0) {
			sleep(1);
			cnt++;
		}
		log("Number of misses: " + cnt);
	}
	
	// proposal for waiting routines
	// wait functions are all similar 
	// while loop with some condition and little sleep 
	// waitForEvent()
	// events are on different levels
	// for know 2 levels 
	// 1fst level 1 event: data available event
	// 2nd level deduced from data available event:
	// probe_config_data available
	// probe_config_ack
	// etc.
	//
	private void waitForEvent(MeshNodeEvent e) {
		while (this.event.compareTo(e) != 0) {
			sleep(1);
		}
	}	
	
	public Location getLocation() {
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

	public void setCurrentMeshData(MeshData meshData) {
		currentMeshData = meshData;
	}
	
	// Timing problem
	// for tree-like mesh structures, it would be enough 
	// that ancestor nodes always turn on in time t + (n-1) * timeout
	// n is depth of their family
	// this way farthest nodes with no children will turn on in time 
	// t - timeout, their parent and uncles and aunts will turn in t
	// and then t + timeout and etc.
	// this way energy is conserved
	// additionally parent node could have children with different 
	// family depths 
	// in this situation parent node will have to activate twice to 
	// collect data, from both family lines
	// timeout should be time long enough for parent to collect data from all
	// children
	// generally parent could activate n different times 
	// generally node activates at least twice once to collect data, and once to propagate data
	// generally nodes have start numbers 
	// nodes activate when their start number is in queue
	// parent nodes activate with start number of their children first
	// after that they activate with their own start number 
	// generally time between activations is delta
	// gateway should calculate and send start configuration after 
	// probe configuration was acknowledged
	// however every node should only know the greatest start number because
	// the greatest start is one which dictates zero moment on start 
	// every other is calculated as (START_NUMBER - GREATEST) * DELTA...	
	public void setMeshData() {
		data = true;
	}

	public void receiveData(AbstractMeshData data) {
		MeshDataType type = data.getDataType();
		
		event = type.produceEvent();
		state = state.next(event);
		
		
		// config data 
		// 		- produces: config data available event
		//		- should contain: child nodes, parent nodes etc. 
		//		- config ack map
		// config ack data
		//		- produces: config ack event
		//		- should change config data
		// normal data
		//		- produces: normal data available event
		//		- should contain: some form of data??		
	}
	
	private void stopListening() {
		isListening = false;
		
	}

	private void startListening() {
		isListening = true;
		
	}
	
	public boolean isListening() {
		return isListening;
	}
	
	public int getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
	}

	protected void log(String input) {
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
	
	public void nextInterval() {
		nextInterval = true;
	}

	public void startData(AbstractMeshData data2) {
		startEnable = true;		
	}
}



