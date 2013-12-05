import Markov.MarkovRandomField;


public class TestMarkov {

	public static void main(){
		MarkovRandomField<String> mrf = new MarkovRandomField<String>(3, 4);
		mrf.add(mrf.newNode("1", 0, 0));
		mrf.add(mrf.newNode("2", 1, 0));
		mrf.add(mrf.newNode("3", 2, 0));
		mrf.add(mrf.newNode("1", 1, 1));
		mrf.add(mrf.newNode("2", 2, 1));
		mrf.add(mrf.newUnknown(3, 1));
		
		mrf.solve();
	}
}

