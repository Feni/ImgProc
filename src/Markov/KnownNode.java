package Markov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.sound.midi.Patch;


public class KnownNode<T> extends Node<T>{
	//T value;
	
	TreeMap<Coordinate, T> values = new TreeMap<Coordinate, T>();
	
	// Keep a count of how many times we see a particular node at a particular distance 
	TreeMap<Coordinate, HashMap<KnownNode<T>, Float>> atOffset = new TreeMap<Coordinate, HashMap<KnownNode<T>, Float>>();
	
	// Keep track of the sum at each location for convenience when calculating normalized probability from atOffset
	TreeMap<Coordinate, Double> locationProbSum = new TreeMap<Coordinate, Double>();	
	
	public KnownNode(Coordinate c, T val){
		//value = val;
		addSimilar(c, val);
	}
	
	public void addSimilar(Coordinate c, T val){
		values.put(c, val);
	}
	
	// Observe that node is distance from 'this' with given weight (generally 1)
	public void addConnection(Node<T> node, float weight, int... distance){
		if(node instanceof KnownNode){
			KnownNode<T> knownNode = (KnownNode<T>)  node;
			
			Coordinate iDistance = new Coordinate(distance);
			
			HashMap<KnownNode<T>, Float> atOffD = atOffset.get(iDistance);
			if(atOffD == null){	// Nothing till now has been observed at that offset. 
				atOffD = new HashMap<KnownNode<T>, Float>();
			}
			
			if(atOffD.containsKey(node)){
				atOffD.put(knownNode, atOffD.get(node) + weight);
			}else{
				atOffD.put(knownNode, weight);
			}
			
			atOffset.put(iDistance, atOffD);
			
			// Add it to sum
			if(locationProbSum.containsKey(iDistance)){	
				locationProbSum.put(iDistance, locationProbSum.get(iDistance) + weight);
			}else{
				locationProbSum.put(iDistance, (double) weight);
			}
		}
		
	}
		
	public void printOffsets(){
		for(Entry<Coordinate, HashMap<KnownNode<T>, Float>> c : atOffset.entrySet()){
			System.out.println(this + " at " + c.getKey() + " : " + c.getValue());
		}
	}
	
	public String toString(){
		return "<" + values + ">"; 
	}
	
	
	
	public T getValue(int... coords){
		Coordinate relative = new Coordinate(coords);
		//return value;
		double minDistance = Double.MAX_VALUE;
		T minVal = null;
		for(Entry<Coordinate, T> e : values.entrySet()){
			double eDistance =  relative.distanceSq(e.getKey());
			if(eDistance < minDistance){
				minDistance = eDistance;
				minVal = e.getValue();
			}
		}
		return minVal;
		
		
	}
	
	
}
