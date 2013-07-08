package de.ecspride.indyaspectwrapper.wrapper;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * Weaves an invokedynamic call at the bootstrap method call.
 * 
 */
public class BootstrapWrapper extends IndyJoinPointWrapperWithRuntimeMethodDispatch {

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
	public BootstrapWrapper(int callClassIndex, int dropArgumentEndIndex, String exclude) {

		super(callClassIndex, dropArgumentEndIndex, exclude);
	}

	/**
	 * Wraps the target MethodHandle of the CallSite with appropriate advices.
	 * 
	 * @param caller
	 * @param name
	 * @param type
	 * @return a CallSite wrapped with all matching aspects
	 */
	public java.lang.invoke.CallSite wrapAndWeave(CallSite callSite, String name, MethodType type) {

		// original target
		MethodHandle original = callSite.getTarget();

		// REMOVE logging
		if (info) {
			System.err.println("INFO:	Called Bootstrap for " + name + "!");
		}

		MethodHandle wraphandle = getWrappedMethodHandleWithRuntimeDispatch(name, type, original);

		// return the wrapped CallSite
		return new ConstantCallSite(wraphandle);
		
		// callSite.setTarget(wraphandle);
		// return callSite;
	}

}
