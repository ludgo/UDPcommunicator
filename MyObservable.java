package pks;

import java.util.Observable;
import java.util.Observer;

public class MyObservable extends Observable {
	
	public MyObservable(Observer o) {
		this.addObserver(o);
	}
	
	public void informUser(String text) {
		setChanged();
		notifyObservers(text);
	}

}
