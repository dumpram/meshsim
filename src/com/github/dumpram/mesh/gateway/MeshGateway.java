package com.github.dumpram.mesh.gateway;

import java.util.ArrayList;
import java.util.List;

import com.github.dumpram.mesh.network.MeshNetwork;
import com.github.dumpram.mesh.node.ConfigData;
import com.github.dumpram.mesh.node.Location;
import com.github.dumpram.mesh.node.MeshNode;

public class MeshGateway extends MeshNode {
	
	private List<MeshNode> childNodes; 
	
	public MeshGateway(int id, Location location) {
		super(id, location);
		childNodes = new ArrayList<MeshNode>();
	}

	public void run() {
		gatewayConfigureData();
		gatewayStartData();
	}
	
	private void gatewayConfigureData() {
		configData = new ConfigData(null, childNodes);
		propagateConfigData();
		waitForConfigAck();
		log("Gateway configuration ended!");
	}
	
	private void gatewayStartData() {
		for (MeshNode i : configData.getChildNodes()) {
			MeshNetwork.getInstance().sendStartData(this, i, null);
		}
	}
	
	@Override
	protected void waitForConfigAck() {
		if (!configData.getChildNodes().isEmpty()) {
			while(!configAck()) {
				sleep(10);
			}
			log("Got config ack");
			log(configData.getChildNodes().toString());
		}
	}
	
	private void gatewayConfigureMaxStartNumber(int maxStart) {
		// TODO Auto-generated method stub
		
	}

	private int gatewayFindMaxStartNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void gatewayPropagateData() {
		
	}

	private void gatewayGetData() {
		
	}

	public void addNode(MeshNode node) {
		childNodes.add(node);
	}
	
	public List<MeshNode> getNodes() {
		return childNodes;
	}
	
	
}
