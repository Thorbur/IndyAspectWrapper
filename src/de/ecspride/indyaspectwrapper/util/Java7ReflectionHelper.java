package de.ecspride.indyaspectwrapper.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import de.ecspride.indyaspectwrapper.model.MemberNameInfo;

/**
 * Helps to retrieve data from Java 7 internal classes. Makes the access easier.
 * Uses Reflection.
 * 
 */
public class Java7ReflectionHelper extends ReflectionHelper {

	static Class<?> boundMH = null;
	static Class<?> mhn = null;
	static Class<?> memberName = null;

	static Constructor<?> mhnCon = null;
	static Object mhnInstance = new Object();

	static Method getTarget = null;
	static Method getTargetMethod = null;
	static Method getDeclaringClass = null;
	static Method getName = null;
	static Method getMethodType = null;

	// initialize all fields
	static {

		try {

			boundMH = Class.forName("java.lang.invoke.BoundMethodHandle");
			mhn = Class.forName("java.lang.invoke.MethodHandleNatives");
			memberName = Class.forName("java.lang.invoke.MemberName");

			mhnCon = mhn.getDeclaredConstructor();
			mhnCon.setAccessible(true);
			mhnInstance = mhnCon.newInstance();

			getTarget = mhn.getDeclaredMethod("getTarget", new Class<?>[] { MethodHandle.class, int.class });
			getTarget.setAccessible(true);
			getTargetMethod = mhn.getDeclaredMethod("getTargetMethod", new Class<?>[] { MethodHandle.class });
			getTargetMethod.setAccessible(true);
			getDeclaringClass = memberName.getDeclaredMethod("getDeclaringClass", new Class[] {});
			getDeclaringClass.setAccessible(true);
			getName = memberName.getDeclaredMethod("getName", new Class[] {});
			getName.setAccessible(true);
			getMethodType = memberName.getDeclaredMethod("getMethodType", new Class[] {});
			getMethodType.setAccessible(true);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the internal stored method call of a MethodHandle as Method object.
	 * 
	 * @param mh
	 *            the MethodHandle
	 * @return the method
	 */
	public static Method getInsideMethod(MethodHandle mh) {

		Method ret = null;
		try {

			ret = (Method) getTargetMethod.invoke(mhnInstance, mh);

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}

	public MemberNameInfo getInsideMemberNameInfo(MethodHandle mh) {

		MemberNameInfo memberNameDescriptor = new MemberNameInfo();
		MethodHandle inside = mh;

		try {

			while (inside instanceof MethodHandle) {

				Object target = getTarget.invoke(mhnInstance, inside, 0);
				if (MethodHandle.class.isInstance(target)) {
					inside = (MethodHandle) target;

				} else if (memberName.isInstance(target)) {

					memberNameDescriptor.setDeclaringClass((Class<?>) getDeclaringClass.invoke(target));
					memberNameDescriptor.setName((String) getName.invoke(target));
					memberNameDescriptor.setMethodType((MethodType) getMethodType.invoke(target));
					break;

				} else {
					break;
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return memberNameDescriptor;
	}

}
