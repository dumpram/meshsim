package com.github.dumpram.mesh.node;

import java.util.ArrayList;
import java.util.List;

public class MeshData {

	public class MeshPacket {
		/**
		 * Fixed payload size for packet.
		 */
		private final int PAYLOAD_SIZE = 16;
		/**
		 * Sender's id.
		 */
		private byte id;
		/**
		 * Sender's mesh group's id.
		 */
		private byte groupId;
		/**
		 * Sender's state described with 8 bits. LSB is isConfigured bit.
		 */
		private byte state;
		/**
		 * Payload array.
		 */
		private byte[] payload = new byte[PAYLOAD_SIZE];
		
		public byte getId() {
			return id;
		}

		public byte getGroupId() {
			return groupId;
		}

		public byte[] getPayload() {
			return payload;
		}
		
		public byte getState() {
			return state;
		}
		
	}
	private List<MeshPacket> packets = new ArrayList<MeshPacket>();

	public MeshData() {
			
	}
	
	public void addMeshPacket(MeshPacket packet) {
		packets.add(packet);
	}

}
