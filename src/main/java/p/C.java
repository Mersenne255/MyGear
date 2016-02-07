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
		Unmarshaller unmarshaller=new Unmarshaller();
		for(String argument:arguments){
			if(argument.matches("FILE=.*")){
				unmarshaller.setExportFilePath(argument.split("=")[1]);
			}
		}
		unmarshaller.transform(o);
	}
}
