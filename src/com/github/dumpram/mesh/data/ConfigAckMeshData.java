package com.github.dumpram.mesh.data;

import java.util.List;

public class ConfigAckMeshData extends AbstractMeshData {

	private List<Integer> previousNumbers;
	
	public ConfigAckMeshData(List<Integer> previousNumbers) {
		this.previousNumbers = previousNumbers;
	}
	
	public List<Integer> getPreviousNumbers() {
		return previousNumbers;
	}

	@Override
	public MeshDataType getDataType() {
		return MeshDataType.PROBE_CONFIG_ACKNOWLEDGE;
	}
	
	public void addNumbersToList(List<Integer> numbers) {
		previousNumbers.addAll(numbers);
	}
	
	public void addNumberToList(int number) {
		previousNumbers.add(number);
	}
	
	@Override
	public String toString() {
		return previousNumbers.toString();
	}

}
