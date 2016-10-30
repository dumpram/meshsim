package com.github.dumpram.mesh.data;

import com.github.dumpram.mesh.node.MeshNodeEvent;

public enum MeshDataType {
	PROBE_CONFIG_DATA,
	PROBE_CONFIG_ACKNOWLEDGE,
	START_CONFIG_DATA,
	NORMAL_STATE_DATA,
	SYNC_STATE_DATA;

	public MeshNodeEvent produceEvent() {
		MeshNodeEvent type;
		switch(this) {
		case PROBE_CONFIG_DATA: type = MeshNodeEvent.CONFIG_DATA_AVAILABLE; break;
		case PROBE_CONFIG_ACKNOWLEDGE: type = MeshNodeEvent.CONFIG_DATA_ACKNOWLEDGE_AVAILABLE; break;
		case START_CONFIG_DATA: type = MeshNodeEvent.START_DATA_AVAILABLE;break;
		case NORMAL_STATE_DATA: type = MeshNodeEvent.START_DATA_AVAILABLE; break;
		case SYNC_STATE_DATA: type = MeshNodeEvent.START_DATA_AVAILABLE; break;
		default: type = null;
		}
		return type;
	}
}
