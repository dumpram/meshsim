package com.github.dumpram.mesh.data;

import java.util.ArrayList;
import java.util.List;

public class NormalStateMeshData extends AbstractMeshData {
	
	private List<MeshPacket> packets = new ArrayList<MeshPacket>();
	
	public NormalStateMeshData() {
		
	}

	@Override
	public MeshDataType getDataType() {
		return MeshDataType.NORMAL_STATE_DATA;
	}

	public void addPacket(MeshPacket packet) {
		packets.add(packet);
	}
	
	public void addPackets(List<MeshPacket> otherPackets) {
		packets.addAll(otherPackets);
	}

	public List<MeshPacket> getPackets() {
		return packets;
	}
}
