package pks;

import java.util.Observable;
import java.util.Observer;

/**
 * A helper observable class to immediately pass string to observers
 */
public class MyObservable extends Observable {
	
	public MyObservable(Observer o) {
		this.addObserver(o);
	}
	
	public void informUser(String text) {
		setChanged();
		notifyObservers(text);
	}

}
