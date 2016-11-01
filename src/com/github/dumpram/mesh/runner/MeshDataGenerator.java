package com.github.dumpram.mesh.runner;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MeshDataGenerator {

	private HashMap<Integer, Integer> data = new HashMap<Integer, Integer>();
	
	private List<Integer> ids;
	
	private Random rand = new Random();
	
	protected MeshDataGenerator(List<Integer> ids) {
		this.ids = ids;
		for (Integer i : ids) {
			data.put(i, rand.nextInt());
		}
	}
	
	public void refreshData() {
		for (Integer i : ids) {
			data.put(i, rand.nextInt());
		}
	}
	
	public void verifyData(int i, int value) {
		if (data.get(i) == value) {
			System.out.println("Data from node " + i + " verified.");
		} else {
			System.out.println("Data from node " + i + " not correct.");
		}
	}
	
	public int getData(Integer i) {
		return data.get(i); 
	}
	
}
