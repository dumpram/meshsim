package com.github.dumpram.mesh.node;

import java.util.TimerTask;

public class NodeTimerTask extends TimerTask {

	MeshNode node;
	
	public NodeTimerTask(MeshNode node) {
		this.node = node;
	}
	
	@Override
	public void run() {
		node.log("Next interval pending!");
		node.nextInterval();
	}
}
