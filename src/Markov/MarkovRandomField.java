package Markov;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MarkovRandomField<T> {
	public int DIMENSIONS;	// 1-D, 2-D, etc.
	public int[] size;
	
	// We have one specific MarkovNode for each discrete value of T that can appear. 
	// i.e. only one MarkovNode to represent all "Hello" nodes. 
	TreeMap<T, KnownNode<T>> nodes = new TreeMap<T, KnownNode<T>>();
	// Keeps track of all of the unknown variables
	ArrayList<Coordinate> unknowns = new ArrayList<Coordinate>();
	// Keeps track of where each node is located in the image/sequence
	TreeMap<Coordinate, Node<T>> sequence = new TreeMap<Coordinate, Node<T>>();
	
	double totalConfidence = 0;	// maximize this. Only counted for unknown guesses
	
	int[] currentPos;
	
	// of a given Width and Height
	public MarkovRandomField(int... s){
		size = s;
		DIMENSIONS = s.length;
		currentPos = new int[DIMENSIONS];
		for(int i = 0; i < currentPos.length; i++)
			currentPos[i] = 0;
	}
	
	public Node<T> newNode(T val, int... coords){
		KnownNode<T> n = nodes.get(val);
		if(n == null){						// Node doesn't exist yet. Create it. 
			n = new KnownNode<T>(new Coordinate(coords), val);
			nodes.put(val, n);
		}else{
			n.addSimilar(new Coordinate(coords), val);
		}
		return n;
	}
	
	public UnknownNode<T> newUnknown(int... coords){
		return new UnknownNode<T>(new Coordinate(coords));
	}
	
	public int[] diff(int[] a, int[] b){
		assert a.length == b.length: "Arrays have to be of the same length to difference";
		int[] difference = new int[a.length];
		for(int i = 0; i < a.length; i++)
			difference[i] = a[i] - b[i];
		return difference;
	}
	
	public void add(Node<T> newNode){
		add(newNode, currentPos);		
		incPos();
	}
	
	public void add(Node<T> newNode, int... coordinates){
		Coordinate newNodeCoord = new Coordinate(coordinates.clone());

		// Keep track of how many nodes we have to solve for
		if(newNode instanceof UnknownNode){
			unknowns.add(newNodeCoord);
		}
		
		// Just assume 2D...
		ArrayList<Coordinate> neighbors = new ArrayList<Coordinate>();
		if(newNodeCoord.coords.length == 2){
			Coordinate tl = new Coordinate(newNodeCoord.coords[0] - 1, newNodeCoord.coords[1] - 1);
			if(sequence.containsKey(tl)){ // top left
				neighbors.add(tl);
			}
			Coordinate t = new Coordinate(newNodeCoord.coords[0], newNodeCoord.coords[1] - 1);
			if(sequence.containsKey(t)){ // directly above
				neighbors.add(t);
			}
			Coordinate tr = new Coordinate(newNodeCoord.coords[0] + 1, newNodeCoord.coords[1] - 1);
			if(sequence.containsKey(tr)){ // top right
				neighbors.add(tr);
			}
			Coordinate l = new Coordinate(newNodeCoord.coords[0] - 1, newNodeCoord.coords[1]);	// left
			if(sequence.containsKey(l)){
				neighbors.add(l);
			}
			Coordinate t2 = new Coordinate(newNodeCoord.coords[0], newNodeCoord.coords[1] - 2);
			if(sequence.containsKey(t2)){ // directly above
				neighbors.add(t2);
			}
			Coordinate l2 = new Coordinate(newNodeCoord.coords[0] - 2, newNodeCoord.coords[1]);	// left
			if(sequence.containsKey(l2)){
				neighbors.add(l2);
			}
			
			//System.out.println("Adding a new thingie at " + newNodeCoord + " which has " + neighbors.size() + " valid neighbors : "+neighbors);
			//System.out.println("Valid neighbors: " + neighbors.size());
		}else{
			neighbors = new ArrayList<Coordinate>(sequence.keySet());
		}
		
		
		/*
		// Relate it to all existing nodes
		// TODO: Only do this for nodes at a certain distance from currentNode
		// Coordinate coord: sequence.keySet() 
		 */
		for(Coordinate coord: neighbors){
			//	sequence.get(coord).addConnection(newNode, newNode instanceof KnownMarkovNode ? 1.0f : 0.0f, );
			Node<T> n = sequence.get(coord);
			int[] distance = diff(coordinates, coord.coords).clone();
			int[] revDistance = Node.distanceBack(distance).clone();
//			System.out.println("Distance is " + Arrays.toString(distance) + " and rev is " + Arrays.toString(revDistance));
			n.addConnection(newNode, 1.0f, distance);
			newNode.addConnection(n, 1.0f, revDistance);
			
//			n.addConnection(newNode, 1.0f, revDistance);
//			newNode.addConnection(n, 1.0f, distance);			
		}
		
		// THEN add it (so that it doesn't get connected with itself)
		sequence.put(newNodeCoord, newNode);
//		System.out.println("PUt the sequene in new node coord " + newNodeCoord + " got " + coordinates + " " + sequence.get(new Coordinate(coordinates)));
	}
	
	// Increment position, carrying over based on size. 
	public void incPos(){
		for(int i = 0; i < DIMENSIONS; i++){
			if(currentPos[i] == size[i] - 1){
				currentPos[i] = 0;	// Carry
			}else{
				currentPos[i] = currentPos[i] + 1;
				break;
			}
		}		
	}
	
	public void solve(int iterations){
		for(Coordinate coord: unknowns){
			UnknownNode<T> unk = (UnknownNode<T>) sequence.get(coord);
			unk.initializeIdentity();
			//System.out.println("Unknown's initial state is " + unk.baseTotal + " : " + unk.baseVotes);
		}
		
		for(int i = 0; i < iterations; i++){
			//System.out.println("Identity crisis version : " + i);
			boolean changed = false;
			for(Coordinate coord: unknowns){
				UnknownNode<T> unk = (UnknownNode<T>) sequence.get(coord);
				changed = unk.decideIdentity() || changed;
			}
			for(Coordinate coord: unknowns){
				UnknownNode<T> unk = (UnknownNode<T>) sequence.get(coord);
				unk.notifyNeighbors();
			}
			
			// We've reached a steady state
			if(!changed && i !=0){
				System.out.println("Early termination at steady state");
				break;
			}
			
			//try{
//				Thread.sleep(1000);
//			}catch(Exception e){}
		}
	}
	
	public void solve(){
		solve(250);
	}
	
	public TreeMap<Coordinate, Node<T>> getSequence(){
		return sequence;
	}
}
