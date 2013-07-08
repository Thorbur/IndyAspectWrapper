package de.ecspride.indyaspectwrapper.wrapper;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.LinkedList;

import de.ecspride.indyaspectwrapper.model.MatchingAdviceData;

public class MethodHandleWrapper extends IndyJoinPointWrapper {

	/**
	 * The constructor.
	 * 
	 * @param exclude
	 *            the identifier/name of the aspect which invokes this wrapper
	 *            class
	 */
	public MethodHandleWrapper(String exclude) {
		aspectDataList = getAllAspects(exclude);
		self = this;
	}

	// make a static MethodHandle referencing the method wrapHandleWithMethod
	static MethodType wrapHandleWithMethodMethodType = methodType(Object.class, MethodHandle.class, Method.class, Object[].class);
	static MethodHandle wrapHandleWithMethodMH;
	static {
		try {
			wrapHandleWithMethodMH = lookup()
					.findVirtual(MethodHandleWrapper.class, "wrapHandleWithMethod", wrapHandleWithMethodMethodType);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// should not happen
			e.printStackTrace();
		}
	}

	/**
	 * Generates MethodHandle from wrapHandleWithMethodh to weave the JoinPoint.
	 * 
	 * @param originalMethod
	 *            the JoinPoint Method
	 * @param original
	 *            the JoinPoint MethodHandle
	 * @return a MethodHandle of the dynamically invoked method with injected
	 *         advices
	 */
	public MethodHandle getWrappedMethodHandleWithMethod(Method originalMethod, MethodHandle original) {

		MethodHandle wraphandle = MethodHandles.insertArguments(wrapHandleWithMethodMH, 0, self, original, originalMethod);
		wraphandle = wraphandle.asCollector(Object[].class, original.type().parameterCount());
		wraphandle = wraphandle.asType(original.type());

		return wraphandle;
	}

	/**
	 * Weaves and invokes the JoinPoint MethodHandle.
	 * 
	 * @param original
	 *            the JoinPoint MethodHandle
	 * @param originalMethod
	 *            the JoinPoint Method
	 * @param args
	 *            the arguments gathered from the CallSite invocation
	 * @return the result of the invocation
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	private Object wrapHandleWithMethod(MethodHandle original, Method originalMethod, Object... args) throws Throwable {

		LinkedList<MatchingAdviceData> matchingAdviceDataList = getMatchingAdvices(originalMethod, args);

		return wrapHandle(original, matchingAdviceDataList, args);
	}

}
