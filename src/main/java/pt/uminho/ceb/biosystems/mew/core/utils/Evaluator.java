package pt.uminho.ceb.biosystems.mew.core.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {
	
	public static final Pattern	COLLECTION_PATTERN	= Pattern.compile("\\w*\\[(.*)\\]\\w*");
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object evaluate(String object, Class<?> klazz) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (klazz.isAssignableFrom(String.class))
			return object;
		else if (Collection.class.isAssignableFrom(klazz)) {
			Collection newColl = null;
			if (SortedSet.class.isAssignableFrom(klazz)) {
				newColl = new TreeSet();
			} else if (Set.class.isAssignableFrom(klazz)) {
				newColl = new HashSet();
			} else {
				newColl = new ArrayList();
			}
			
			String[] elems = processCollectionString(object);
			if (elems != null) for (String s : elems) {
				System.out.println("Evaluator elem [" + s + "]");
				newColl.add(s);
			}
			return newColl;
		} else {
			Method valueOf = klazz.getMethod("valueOf", String.class);
			return valueOf.invoke(null, object);
		}
	}
	
	public static String[] processCollectionString(String collectionString) {
		Matcher m = COLLECTION_PATTERN.matcher(collectionString);
		if (m.matches()) {
			String list = m.group(1);
			String[] elements = list.split(",");
			List<String> newElems = new ArrayList<String>();
			for (int i = 0; i < elements.length; i++) {
				String elem = elements[i].trim();
				if (!elem.isEmpty()) newElems.add(elem);
			}
			if (!newElems.isEmpty()) {
				String[] newArray = new String[newElems.size()];
				return newElems.toArray(newArray);
			} else
				return null;
		} else
			throw new IllegalArgumentException("Wrong collection format for [" + collectionString + "]. Collections should be in the format [elem1, elem2, ..., enemN]");
	}
	
	//	public static void main(String... args) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	//		
	//		//		SolverType sv = (SolverType) evaluate("CPLEX", SolverType.class);
	//		//		
	//		//		System.out.println("solver = "+sv.name());
	//		
	//		ArrayList<String> set = (ArrayList<String>) evaluate("[a]", ArrayList.class);
	//		System.out.println(set);
	//	}
	
}
