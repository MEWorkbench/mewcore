package pt.uminho.ceb.biosystems.mew.mewcore.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class Evaluator {
	
	public static Object evaluate(String object, Class<?> klazz) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {;
		if(klazz.isAssignableFrom(String.class))
			return object;
		else {
			Method valueOf = klazz.getMethod("valueOf", String.class);
			return valueOf.invoke(null, object);
		}
	}
	
	public static void main(String... args) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{
		
		SolverType sv = (SolverType) evaluate("CPLEX", SolverType.class);
		
		System.out.println("solver = "+sv.name());
	}
}

