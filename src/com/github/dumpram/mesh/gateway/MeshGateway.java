package com.github.dumpram.mesh.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.dumpram.mesh.data.ConfigAckMeshData;
import com.github.dumpram.mesh.data.MeshPacket;
import com.github.dumpram.mesh.data.NormalStateMeshData;
import com.github.dumpram.mesh.data.StartMeshData;
import com.github.dumpram.mesh.network.MeshNetwork;
import com.github.dumpram.mesh.node.ConfigData;
import com.github.dumpram.mesh.node.Location;
import com.github.dumpram.mesh.node.MeshNode;

public class MeshGateway extends MeshNode {
	
	private static final long serialVersionUID = 1L;
	
	private List<MeshNode> childNodes; 
	
	public MeshGateway(int id, Location location) {
		super(id, location);
		childNodes = new ArrayList<MeshNode>();
		this.isGateway = true;
	}

	public void run() {
		gatewayConfigureData();
		gatewayStartData();
		while(true) {
			gatewayGetData();
		}
	}
	
	private void gatewayConfigureData() {
		configData = new ConfigData(null, childNodes);
		propagateConfigData();
		waitForConfigAck();
		log("Gateway configuration ended!");
		log(configAckMeshData.toString());
	}
	
	private void gatewayStartData() {
		int maxStartNumber = Collections.max(configAckMeshData.getPreviousNumbers());
		log("Max start number: " + maxStartNumber);
		for (MeshNode i : configData.getChildNodes()) {
			MeshNetwork.getInstance().sendStartData(this, i, new StartMeshData(maxStartNumber));
		}
	}
	
	@Override
	protected void waitForConfigAck() {
		configAckMeshData = new ConfigAckMeshData(new ArrayList<Integer>());
		if (!configData.getChildNodes().isEmpty()) {
			while(!configAck()) {
				sleep(10);
			}
			log("Got config ack");
			log(configData.getChildNodes().toString());
		}
	}

	private void gatewayGetData() {
		this.currentMeshData = new NormalStateMeshData();
		for (int i = 0; i < configData.getChildNodes().size(); i++) {
			getDataFromNode(configData.getChildNodes().get(i).getId());
		}
		checkDataFromNodes();
		MeshNetwork.getInstance().getMeshDataGenerator().refreshData();
	}
	
	/**
	 * Sanity test for data.
	 */
	private void checkDataFromNodes() {
		List<MeshPacket> packets = currentMeshData.getPackets();
		for (int i = 0; i < packets.size(); i++) {
			MeshNetwork.getInstance().getMeshDataGenerator().verifyData(
					currentMeshData.getPackets().get(i).getId(), 
					currentMeshData.getPackets().get(i).getData());
		}
	}

	@Override
	public void setMeshData(NormalStateMeshData data) {
		super.setMeshData(data);
	}

	public void addNode(MeshNode node) {
		childNodes.add(node);
	}
	
	public List<MeshNode> getNodes() {
		return childNodes;
	}
	
	@Override
	protected void stopListening() {
		isListening = true;
		repaint();
	}
	
	
}
