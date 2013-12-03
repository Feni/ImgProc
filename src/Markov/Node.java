package Markov;


public abstract class Node<T> {
	
	public static int[] distanceBack(int... distance){
		int[] dBack = new int[distance.length];
		for(int i = 0; i < distance.length; i++)
			dBack[i] = -1 * distance[i];
		return dBack;
	}	
	
	public abstract void addConnection(Node<T> node, float weight, int... distance);
			
}
