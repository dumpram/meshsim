package com.github.dumpram.mesh.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigData {

	private List<MeshNode> childNodeList;
	
	private List<MeshNode> parentNodeList;
	
	public HashMap<MeshNode, Boolean> configAckMap = new HashMap<MeshNode, Boolean>();
	
	public int maxStartNumber = -1;
	
	public int delta; // milliseconds
	
	public ConfigData(List<MeshNode> list) {
		childNodeList = new ArrayList<MeshNode>();
		for (MeshNode i : list) {
			childNodeList.add(i);
		}
		//Collections.copy(childNodeList, list);
		parentNodeList = new ArrayList<MeshNode>();
	}
	
	public void removeChildNode(MeshNode node) {
		childNodeList.remove(node);
	}
	
	public void addNode(MeshNode node) {
		childNodeList.add(node);
	}

	public List<MeshNode> getChildNodes() {
		return childNodeList;
	}
	
	public void addParent(MeshNode parent) {
		parentNodeList.add(parent);
	}
	
	public MeshNode getParent() {
		return parentNodeList.get(0); // for now only one parent
	}

	public void setChildNodes(List<MeshNode> realChildren) {
		childNodeList = realChildren;	
	}
}
