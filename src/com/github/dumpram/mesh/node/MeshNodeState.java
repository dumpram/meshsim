package com.github.dumpram.mesh.node;

public enum MeshNodeState {
	CONFIG_DATA_WAIT,
	CONFIG_DATA_PROPAGATE,
	CONFIG_DATA_ACKNOWLEDGE_WAIT,
	CONFIG_DATA_ACKNOWLEDGE_PROPAGATE,
	START_DATA_WAIT,
	START_DATA_PROPAGATE,
	NORMAL_DATA_WAIT,
	NORMAL_DATA_PROPAGATE;

	public MeshNodeState next(MeshNodeEvent event) {
		MeshNodeState newState = this;
		switch(this) {
		case CONFIG_DATA_WAIT:
			if (event.compareTo(MeshNodeEvent.CONFIG_DATA_AVAILABLE) == 0) {
				newState = CONFIG_DATA_ACKNOWLEDGE_WAIT;
			} 
			break;
		case CONFIG_DATA_ACKNOWLEDGE_WAIT:
			if (event.compareTo(MeshNodeEvent.CONFIG_DATA_ACKNOWLEDGE_AVAILABLE) == 0) {
				newState = START_DATA_WAIT;
			}
			break;
		case START_DATA_WAIT:
			if (event.compareTo(MeshNodeEvent.START_DATA_AVAILABLE) == 0) {
				newState = NORMAL_DATA_WAIT;
			} 
			break;
		case NORMAL_DATA_WAIT:
			if (event.compareTo(MeshNodeEvent.NORMAL_DATA_AVAILABLE) == 0) {
				newState = NORMAL_DATA_WAIT;
			} 
			break;
		default: break;
		}
		return newState;
	}
}
