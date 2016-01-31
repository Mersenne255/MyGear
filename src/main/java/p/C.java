package p;

import com.mygear.tools.Unmarshaller;

/**
 * Facade class providing short URL access
 * 
 * @author Martin
 *
 */
public class C {

	public static void m(Object o) {
		new Unmarshaller().transform(o);
	}

	public static void m(Object o, String args) {
		String[] arguments = args.split(" ");
		m(o);
	}
}
