
class PatchConnection implements Comparable<PatchConnection>{
	Patch currentState;
	Patch nextState;
	
	// Where is nextState relative to the center of the currentState. 
	int dX;
	int dY;
	
	// Just one way probability right now...
	// What's the chance of nextState being on the right of currentState. 
	double probability;
	public PatchConnection(Patch cS, Patch nS, int dX, int dY, double prob){
		currentState = cS;
		nextState = nS;
		probability = prob;
	}
	
	public int compareTo(PatchConnection pc) {
		if(pc.currentState == this.currentState){
			return pc.nextState.compareTo(nextState);
		}
		else{
			return -1;
		}
	}
}
