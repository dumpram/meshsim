package com.github.dumpram.mesh.gateway;

import java.util.ArrayList;
import java.util.List;

import com.github.dumpram.mesh.node.MeshNode;

public class MeshGateway extends MeshNode {
	
	
	private List<MeshNode> nodes; 
	
	public MeshGateway() {
		super(0, null, true);
		nodes = new ArrayList<MeshNode>();
	}
	
	
	public void addNode(MeshNode node) {
		nodes.add(node);
	}
	
	public List<MeshNode> getNodes() {
		return nodes;
	}
	
}
