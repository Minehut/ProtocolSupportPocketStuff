package protocolsupportpocketstuff.util;

import java.lang.reflect.Field;

public class ReflectionUtils {
	public static int getInt(String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
		Field f = object.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.getInt(object);
	}

	public static double getDouble(String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
		Field f = object.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.getDouble(object);
	}

	public static Object get(String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
		Field f = object.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(object);
	}
}
