package de.ecspride.indyaspectwrapper.util;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

import de.ecspride.indyaspectwrapper.model.MemberNameInfo;

/**
 * Abstract Helper class for accessing Java internal fields and methods via
 * Reflection.
 * 
 */
public abstract class ReflectionHelper {

	/**
	 * Retrieve the MemberName stored inside the given MethodHandle and store
	 * the signature information in a MemberNameInfo object.
	 * 
	 * @param mh
	 *            the MethodHandle
	 * @return the MemberNameInfo with the signature of the intrnal method call
	 */
	public abstract MemberNameInfo getInsideMemberNameInfo(MethodHandle mh);

	/**
	 * Checks which concrete types the MethodHandle has. Returns the types in a
	 * list
	 * 
	 * @param mh
	 *            the MethodHandle
	 * @return a list with the types of the MethodHandle
	 */
	public List<String> getMethodHandleType(MethodHandle mh) {

		List<String> typeList = new ArrayList<String>();

		String[] MHClassNames = new String[] { "java.lang.invoke.DirectMethodHandle", "java.lang.invoke.BoundMethodHandle",
				"java.lang.invoke.MethodHandleImpl", "java.lang.invoke.MethodHandleImpl$GuardWithCatch",
				"java.lang.invoke.MethodHandleImpl$GuardWithTest", "java.lang.invoke.MethodHandleImpl$AllocateObject",
				"java.lang.invoke.MethodHandleImpl$FieldAccessor", "java.lang.invoke.FromGeneric$Adapter",
				"java.lang.invoke.FilterGeneric$Adapter", "java.lang.invoke.SpreadGeneric$Adapter", "java.lang.invoke.ToGeneric$Adapter",
				"java.lang.invoke.FilterOneArgument", "java.lang.invoke.AdapterMethodHandle",
				"java.lang.invoke.AdapterMethodHandle$AsVarargsCollector", "java.lang.invoke.CountingMethodHandle" };

		for (String MHClass : MHClassNames) {
			Class<?> mhclass;
			try {
				mhclass = Class.forName(MHClass);
				if (mhclass.isInstance(mh)) {
					typeList.add(mhclass.getSimpleName());
				}

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		return typeList;
	}
}
