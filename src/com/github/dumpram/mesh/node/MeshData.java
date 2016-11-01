package com.github.dumpram.mesh.node;

import java.util.ArrayList;
import java.util.List;

import com.github.dumpram.mesh.data.MeshPacket;

public class MeshData {


	private List<MeshPacket> packets = new ArrayList<MeshPacket>();

	public MeshData() {
			
	}
	
	public void addMeshPacket(MeshPacket packet) {
		packets.add(packet);
	}

}
