package com.github.dumpram.mesh.data;

import java.util.HashMap;

import com.github.dumpram.mesh.node.MeshNode;

public abstract class AbstractMeshData {
	
	
	protected HashMap<MeshNode, Boolean> ackMap = new HashMap<MeshNode, Boolean>();
	
	public abstract MeshDataType getDataType();



}
