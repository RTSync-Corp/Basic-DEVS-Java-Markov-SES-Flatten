package com.ms4systems.devs.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;

public class DevsUtil {

	/**
	 * Logic - the classloader we need is the top level classloader
	 * UNLESS the model(s) has been wrapped in coupled models
	 * 
	 * So, we climb down the tree until we find 
	 * 	(a) A SimulationClassLoader or 
	 *  (b) Two child models - we take the classloader of one of them
	 * @return
	 */
	public static ClassLoader findContextClassLoader(AtomicModel model) {
		while (true) {
			if (model.getClass().getClassLoader() instanceof SimulationClassLoader) 
				break;
			if (model instanceof CoupledModel) {
				final ArrayList<AtomicModel> children = ((CoupledModel) model).getChildren();
				if (children.isEmpty()) break;
				if (children.size()>1) {
					model = children.get(0);
					break;
				}
				model = children.get(0);
			}
			else break;
		}
		return model.getClass().getClassLoader();
	}

	/**
	 * Convenience method using null newName, see:
	 * {@link com.ms4systems.devs.util.DevsUtil#getFreshCopy(AtomicModel, String)}
	 */
	public static <T extends AtomicModel> T getFreshCopy(T model) {
		return getFreshCopy(model, null);
	}
	
	/**
	 * Will obtain a 'fresh copy' of a simulation.  This is defined by a new instantiation of the model.
	 * Models must have either a 1 argument constructor that takes a String name, or a no-article constructor.
	 * 
	 * @param <T>
	 * @param model
	 * @param newName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends AtomicModel> T getFreshCopy(T model, String newName) {
		final Constructor<T>[] constructors;
		try {
			constructors = (Constructor<T>[]) model.getClass().getConstructors();
		} catch (ClassCastException e) {
			e.printStackTrace();
			return null;
		}
		
		Constructor<T> noArgConstructor = null;
		Constructor<T> nameConstructor = null;
		for (Constructor<T> c : constructors) {
			final Class<?>[] parameterTypes = c.getParameterTypes();
			if (parameterTypes.length==0) noArgConstructor = c;
			else if (parameterTypes.length==1 && 
					parameterTypes[0].isAssignableFrom(String.class))
				nameConstructor = c;
		}
		if (newName==null) {
			if (noArgConstructor!=null)
				try {
					return noArgConstructor.newInstance();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			newName = model.getName() + " (Copy)";
		}
		
		if (nameConstructor==null) return null;
		
		try {
			return nameConstructor.newInstance(newName);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
