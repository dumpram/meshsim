package com.github.dumpram.mesh.data;

public class MeshPacket {
	/**
	 * Fixed payload size for packet.
	 */
	private final int PAYLOAD_SIZE = 16;
	/**
	 * Sender's id.
	 */
	private int id;
	/**
	 * Sender's mesh group's id.
	 */
	private byte groupId;
	/**
	 * Node's data;
	 */
	private int data;
	/**
	 * Sender's state described with 8 bits. LSB is isConfigured bit.
	 */
	private byte state;
	/**
	 * Payload array.
	 */
	private byte[] payload = new byte[PAYLOAD_SIZE];
	
	public MeshPacket(int id, int data) {
		this.id = id;
		this.data = data;
	}
	
	public int getId() {
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

	public int getData() {
		return data;
	}
	
}