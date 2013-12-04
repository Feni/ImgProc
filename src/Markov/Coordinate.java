package Markov;
import java.util.Arrays;

// An n-dimensional coordinate generally used as a relative offset between two MarkovNodes. 
public class Coordinate implements Comparable<Coordinate>{
	int[] coords;
	public Coordinate(int... c){
		coords = Arrays.copyOf(c, c.length);
	}
	
	public boolean equals(Object o){
		if(o instanceof Coordinate){
			Coordinate m = (Coordinate) o;
			return Arrays.equals(m.coords, coords);
		}
		return false;
	}

	@Override
	public int compareTo(Coordinate m) {
		if(m.coords.length == coords.length){
			for(int i = 0; i < coords.length; i++){
				if(m.coords[i] < coords[i])
					return -1;
				else if(m.coords[i] > coords[i])
					return 1;
			}
			return 0;
		}
		return -1;
	}
	
	public String toString(){
		return Arrays.toString(coords);
	}
	
	public int[] getCoords(){
		return coords;
	}
	
	// returns (this - other) ^2
	public double distanceSq(Coordinate other){
		assert other.coords.length == coords.length;
		double sumSq = 0.0;
		for(int i = 0; i < coords.length; i++){
			sumSq += Math.pow(coords[i] - other.coords[i], 2); 
		}
		return sumSq;
	}
	
	public double distanceSq(){
		double sumSq = 0.0;
		for(int i : coords){
			sumSq += Math.pow(i, 2);
		}
		return sumSq;
	}
	
	public double distance(Coordinate other){
		return Math.sqrt(distanceSq(other));
	}
	
	public double distance(){
		return Math.sqrt(distanceSq());
	}
	
	public double distanceAbs(Coordinate other){
		double sumAbs = 0.0;
		for(int i = 0; i < coords.length; i++){
			sumAbs += Math.abs(coords[i] - other.coords[i]);
		}
		
		return sumAbs;
		
	}
	
}

