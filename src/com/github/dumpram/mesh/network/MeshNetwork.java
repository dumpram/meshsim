package com.github.dumpram.mesh.network;

import com.github.dumpram.mesh.data.AbstractMeshData;
import com.github.dumpram.mesh.data.NormalStateMeshData;
import com.github.dumpram.mesh.data.StartMeshData;
import com.github.dumpram.mesh.gateway.MeshGateway;
import com.github.dumpram.mesh.node.ConfigData;
import com.github.dumpram.mesh.node.Location;
import com.github.dumpram.mesh.node.MeshData;
import com.github.dumpram.mesh.node.MeshNode;
import com.github.dumpram.mesh.runner.MeshDataGenerator;

public class MeshNetwork implements Runnable {
	
	public static final double MESH_RADIO_LIMIT = Math.sqrt(2);
	
	private MeshGateway gateway;
	
	private static MeshNetwork network = new MeshNetwork();
	
	private MeshDataGenerator generator;
	
	private MeshNetwork() {
		//gateway = new MeshGateway();
	}
	
	@Override
	public void run() {
		configureMeshNetwork();
	}
	
	public MeshGateway getGateway() {
		return gateway;
	}
	
	void configureMeshNetwork() {
		gateway.run();
	}

	public boolean probe(MeshNode parent, MeshNode child) {
		// lock
		synchronized(child) {
			boolean probe = areCloseEnough(parent, child) && child.isListening();
			System.out.println("Parent[" + parent.getId() + "] probes child[" + child.getId() + "] :" + probe);
			if (probe) {
				return true;
				//child.setConfigData(configData);
			} else {
				return false;
			}
		}
		// unlock
	}
	
	public boolean areCloseEnough(MeshNode fst, MeshNode snd) {
		Location loc1 = fst.getNodeLocation();
		Location loc2 = snd.getNodeLocation();
		double x1 = loc1.getX();
		double x2 = loc2.getX();
		double y1 = loc1.getY();
		double y2 = loc2.getY();
		boolean forExport = Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2)) <= MESH_RADIO_LIMIT;
		return forExport;
		
	}
	
	// in send methods we should always check listening and distance 
	public void sendConfigData(MeshNode child, MeshNode parent, ConfigData configData) {
		synchronized(child) {
			if (isSendable(parent, child)) {
				child.setConfigData(configData);
			}
		}
	}

	public void sendConfigAckToParent(MeshNode child, MeshNode parent, ConfigData parentData) {
		synchronized(parentData) {
			if (isSendable(child, parent)) {
				parentData.configAckMap.put(child, true);
				parent.getConfigAckMeshData().addNumbersToList(child.getConfigAckMeshData().getPreviousNumbers());
			}
		}
	}

	public void sendMeshData(MeshNode parent, MeshData meshData) {
		synchronized(parent) {
			//parent.setCurrentMeshData(meshData);
		}
	}
	
	public static MeshNetwork getInstance() {
		return network;
	}

	public boolean setMeshData(MeshNode child, MeshNode parent, NormalStateMeshData data) {
		boolean forExport = false;
		synchronized(parent) {
			if (isSendable(child, parent)) {
				parent.setMeshData(data);
				forExport = true;
			}
		}
		return forExport;
	}
	
	public boolean sendNormalData(MeshNode child, MeshNode parent, NormalStateMeshData data) {
		boolean sendable = isSendable(child, parent);
		synchronized(parent) {
			parent.setMeshData(data);
		}
		return sendable;
	}
	
	public boolean sendData(MeshNode sender, MeshNode listener, AbstractMeshData data) {
		boolean sendable = isSendable(sender, listener);
		synchronized(listener) {
			listener.receiveData(data);
		}
		return sendable;
	}
	
	public boolean isSendable(MeshNode sender, MeshNode listener) {
		return areCloseEnough(sender, listener) && listener.isListening();
	}

	public boolean sendStartData(MeshNode sender, MeshNode listener, StartMeshData data) {
		boolean sendable = isSendable(sender, listener);
		synchronized(listener) {
			listener.startData(data);
		}
		return sendable;
	}
	
	public void setMeshDataGenerator(MeshDataGenerator generator) {
		this.generator = generator;
	}
	
	public MeshDataGenerator getMeshDataGenerator() {
		return generator;
	}
}
	
