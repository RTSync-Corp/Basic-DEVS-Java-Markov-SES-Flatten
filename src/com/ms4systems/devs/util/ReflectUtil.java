package com.ms4systems.devs.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Convenience class for accessing fields 
 * @author bcoop
 *
 */
public class ReflectUtil {
	
	
	public static <T> T get(Object obj, String field, Class<T> type) {
		return getFromClass(obj, obj.getClass(), field, type);
	}

	protected static <T> T getFromClass(Object obj, Class<?> klass, String field, Class<T> type) {
		Field f;
		try {
			f =  klass.getDeclaredField(field);
			f.setAccessible(true);
			return type.cast(f.get(obj));
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchFieldException e) {
			if (klass.getSuperclass()!=null)
				return getFromClass(obj, klass.getSuperclass(), field, type);
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void set(Object obj, String field, Object value) {
		Field f;
		try {
			f = obj.getClass().getDeclaredField(field);
			f.setAccessible(true);
			f.set(obj, value);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static Object invoke(Object obj, String method, Object[] args, Class<?>[] parameterTypes) {
		return invokeOnClass(obj, obj.getClass(), method, args, parameterTypes);
	}

	protected static Object invokeOnClass(Object obj, Class<?> klass, String method,
			Object[] args, Class<?>[] parameterTypes) {
		try {
			Method m = klass.getDeclaredMethod(method, parameterTypes);
			m.setAccessible(true);
			return m.invoke(obj, args);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			if (klass.getSuperclass()!=null)
				return invokeOnClass(obj, klass.getSuperclass(), method, args, parameterTypes);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T invoke(Object obj, String method, Object[] args, Class<?>[] parameterTypes, 
			Class<T> returnType) {
		return returnType.cast(invoke(obj,method,args,parameterTypes));
	}

}
