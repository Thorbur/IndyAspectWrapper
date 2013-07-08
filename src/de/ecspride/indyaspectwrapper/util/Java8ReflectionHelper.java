package de.ecspride.indyaspectwrapper.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import de.ecspride.indyaspectwrapper.model.MemberNameInfo;

/**
 * Helps to retrieve data from Java 8 internal classes. Makes the access easier.
 * Uses Reflection.
 * 
 */
public class Java8ReflectionHelper extends ReflectionHelper {
	
	static Class<?> memberName = null;

	static Constructor<?> mhnCon = null;
	static Object mhnInstance = new Object();

	static Method getDeclaringClass = null;
	static Method getName = null;
	static Method getMethodType = null;
	static Method internalMemberName = null;

	// initialize all fields
	static {

		try {

			memberName = Class.forName("java.lang.invoke.MemberName");

			getDeclaringClass = memberName.getDeclaredMethod("getDeclaringClass", new Class[] {});
			getDeclaringClass.setAccessible(true);
			getName = memberName.getDeclaredMethod("getName", new Class[] {});
			getName.setAccessible(true);
			getMethodType = memberName.getDeclaredMethod("getMethodType", new Class[] {});
			getMethodType.setAccessible(true);
			internalMemberName = MethodHandle.class.getDeclaredMethod("internalMemberName", new Class[] {});
			internalMemberName.setAccessible(true);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public MemberNameInfo getInsideMemberNameInfo(MethodHandle mh) {

		MemberNameInfo memberNameDescriptor = new MemberNameInfo();

		try {
			if (mh != null) {
				Object target = internalMemberName.invoke(mh);
				if (target != null) {
					memberNameDescriptor.setDeclaringClass((Class<?>) getDeclaringClass.invoke(target));
					memberNameDescriptor.setName((String) getName.invoke(target));
					memberNameDescriptor.setMethodType((MethodType) getMethodType.invoke(target));
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return memberNameDescriptor;
	}

}
