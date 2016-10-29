package com.github.dumpram.mesh.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.dumpram.mesh.node.Location;
import com.github.dumpram.mesh.node.MeshNode;

public class MeshRunner {

	public static void main(String[] args) throws IOException {
		int nodeCount = 0;
		
		if (args.length != 1) {
			System.out.println("Provide path to input file!");
			return;
		}
		
		List<String> confLines = Files.readAllLines(Paths.get(args[0]));		
		nodeCount = confLines.size();
		Thread[] threads = new Thread[nodeCount];
	
		List<MeshNode> remoteNodes = new ArrayList<MeshNode>();
		MeshNode gateway = null;
		
		for (int i = 0; i < confLines.size(); i++) {
			String[] parts = confLines.get(i).split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			boolean isGateway = (i == 0) ? true : false;
			MeshNode current = new MeshNode(i, new Location(x, y), isGateway);
			threads[i] = new Thread(current);
			if (!isGateway) {
				remoteNodes.add(current);
				gateway.addChildNode(current);
			} else {
				gateway = current;
			}
		}
		
		
		for (int i = 0; i < confLines.size(); i++) {
			threads[i].start();
		}
		

		//new MeshNode(0, new Location(0, 0)).run();
	}
}