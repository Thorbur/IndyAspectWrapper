package de.ecspride.indyaspectwrapper.wrapper;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.LinkedList;

import de.ecspride.indyaspectwrapper.model.MatchingAdviceData;

/**
 * Abstract Wrapper class to provide weaving of MethodHandles. The Method object
 * for matching the pointcut expressions of the advices is retrieved at runtime.
 * 
 */
public abstract class IndyJoinPointWrapperWithRuntimeMethodDispatch extends IndyJoinPointWrapper {

	// for retrieving the Method object
	protected static int callClassIndex;
	protected static int dropArgumentEndIndex;

	/**
	 * The constructor.
	 * 
	 * @param callClassIndex
	 *            the position of the instance onto the method call happens in
	 *            the argument provided at the invocation of the CallSite
	 * @param dropArgumentEndIndex
	 *            the starting position of the arguments provided at the
	 *            invocation of the CallSite to invoke the JoinPoint
	 *            MethodHandle; previous arguments will be dropped
	 * @param exclude
	 *            the identifier/name of the aspect which invokes this wrapper
	 *            class
	 */
	public IndyJoinPointWrapperWithRuntimeMethodDispatch(int callClassIndex, int dropArgumentEndIndex, String exclude) {

		IndyJoinPointWrapperWithRuntimeMethodDispatch.callClassIndex = callClassIndex;
		IndyJoinPointWrapperWithRuntimeMethodDispatch.dropArgumentEndIndex = dropArgumentEndIndex;
		aspectDataList = getAllAspects(exclude);
		self = this;
	}

	// make a static MethodHandle referencing the method
	// wrapHandleWithRuntimeDispatch
	static MethodType wrapHandleWithRuntimeDispatchMethodType = methodType(Object.class, MethodHandle.class, String.class,
			MethodType.class, Object[].class);
	static MethodHandle wrapHandleWithRuntimeDispatchMH;
	static {
		try {
			wrapHandleWithRuntimeDispatchMH = lookup().findVirtual(IndyJoinPointWrapperWithRuntimeMethodDispatch.class,
					"wrapHandleWithRuntimeDispatch", wrapHandleWithRuntimeDispatchMethodType);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// should not happen
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Generates MethodHandle from wrapHandleWithRUntimeDispatch to weave the JoinPoint.
	 * 
	 * @param name
	 *            the method name
	 * @param type
	 *            the method type
	 * @param original
	 *            the JoinPoint MethodHandle
	 * @return a MethodHandle of the dynamically invoked method with injected
	 *         advices
	 */
	protected static MethodHandle getWrappedMethodHandleWithRuntimeDispatch(String name, MethodType type, MethodHandle original) {

		MethodHandle wraphandle = MethodHandles.insertArguments(wrapHandleWithRuntimeDispatchMH, 0, self, original, name, type);
		wraphandle = wraphandle.asCollector(Object[].class, type.parameterCount());
		wraphandle = wraphandle.asType(type);

		return wraphandle;
	}

	/**
	 * Weaves and invokes the JoinPoint MethodHandle.
	 * 
	 * @param original
	 *            the JoinPoint MethodHandle
	 * @param name
	 *            the method name
	 * @param type
	 *            the type of the method call
	 * @param args
	 *            the arguments gathered from the CallSite invocation
	 * @return the result of the invocation
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	private Object wrapHandleWithRuntimeDispatch(MethodHandle original, String name, MethodType type, Object... args) throws Throwable {

		Object mclass = args[callClassIndex];
		LinkedList<MatchingAdviceData> matchingAdviceDataList = new LinkedList<MatchingAdviceData>();
		if (mclass != null) {
			matchingAdviceDataList = getMatchingAdvices(getOriginalMethod(mclass.getClass(), name, type, dropArgumentEndIndex), args);

		}

		return wrapHandle(original, matchingAdviceDataList, args);
	}

	static Hashtable<MethodDescriptor, Method> methodCache = new Hashtable<MethodDescriptor, Method>();

	/**
	 * 
	 * Get Reflection Method object of dynamically invoked method.
	 * 
	 * @param targetClass
	 *            the class of the signature
	 * @param name
	 *            the method name
	 * @param type
	 *            the type of the signature
	 * @param dropArgumentEndIndex
	 *            the starting position of the arguments provided at the
	 *            invocation of the CallSite to invoke the JoinPoint
	 *            MethodHandle; previous arguments will be dropped
	 * @return the Method object retrieved from the provided signature
	 *         information
	 */
	private Method getOriginalMethod(Class<?> targetClass, String name, MethodType type, int dropArgumentEndIndex) {

		Method originalMethod = null;

		MethodDescriptor md = new MethodDescriptor(targetClass, name, type);

		originalMethod = methodCache.get(md);
		if (originalMethod == null) {
			try {
				// Groovy: remove first argument
				if (type.parameterCount() >= dropArgumentEndIndex) {
					originalMethod = targetClass.getDeclaredMethod(name, type.dropParameterTypes(0, dropArgumentEndIndex).parameterArray());
				} else {
					originalMethod = targetClass.getDeclaredMethod(name, type.parameterArray());
				}

				methodCache.put(md, originalMethod);
				// originalMethod.setAccessible(true);

			} catch (NoSuchMethodException | SecurityException e) {

				if (warn) {
					System.err.println("Can't get Method Object of " + targetClass + "." + name + type.toMethodDescriptorString());
				}
			}

		}

		return originalMethod;
	}

	/**
	 * Internal Data Type as key for the cache.
	 * 
	 */
	class MethodDescriptor {

		Class<?> targetClass;
		String name;
		MethodType type;

		/**
		 * The constructor.
		 * 
		 * @param targetClass
		 *            the class of the signature
		 * @param name
		 *            the method name
		 * @param type
		 *            the type of the signature
		 */
		public MethodDescriptor(Class<?> targetClass, String name, MethodType type) {
			super();
			this.targetClass = targetClass;
			this.name = name;
			this.type = type;
		}

		@Override
		public boolean equals(Object obj) {
			MethodDescriptor md;
			if (obj instanceof MethodDescriptor) {
				md = (MethodDescriptor) obj;
			} else {
				return false;
			}

			if (!this.targetClass.equals(md.targetClass)) {
				return false;
			}
			if (!this.name.equals(md.name)) {
				return false;
			}
			if (!this.type.equals(md.type)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + this.targetClass.hashCode();
			result = 31 * result + this.name.hashCode();
			result = 31 * result + this.type.hashCode();
			return result;
		}

	}

}
