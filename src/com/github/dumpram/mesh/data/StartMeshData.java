package com.github.dumpram.mesh.data;

public class StartMeshData extends AbstractMeshData {

	private int highestStartNumber;
	
	public StartMeshData(int startNumber) {
		this.highestStartNumber = startNumber;
	}
	
	@Override
	public MeshDataType getDataType() {
		return MeshDataType.START_CONFIG_DATA;
	}

	public int getHighestStartNumber() {
		return highestStartNumber;
	}
	
}
