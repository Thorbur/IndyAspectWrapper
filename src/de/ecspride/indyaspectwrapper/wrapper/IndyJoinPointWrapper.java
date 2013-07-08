package de.ecspride.indyaspectwrapper.wrapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AdviceKind;
import org.aspectj.weaver.loadtime.Aj;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.ShadowMatch;

import de.ecspride.indyaspectwrapper.model.AdviceData;
import de.ecspride.indyaspectwrapper.model.AspectData;
import de.ecspride.indyaspectwrapper.model.MatchingAdviceData;

/**
 * Abstract wrapper class for wrapping MethodHandles, matching pointcuts and
 * invoking advices.
 * 
 */
public abstract class IndyJoinPointWrapper {

	protected static boolean warn = true;
	protected static boolean info = true;

	protected static IndyJoinPointWrapper self;
	protected static List<AspectData> aspectDataList;

	// set logging enabled/disabled
	static {

		String envwarn = System.getenv("warn");
		if (envwarn != null) {
			warn = Boolean.parseBoolean(envwarn);
		}
		String envinfo = System.getenv("info");
		if (envinfo != null) {
			info = Boolean.parseBoolean(envinfo);
		}
	}

	/**
	 * Find and get all aspects. Excludes the aspect in the exclude String.
	 * 
	 * @param exclude
	 *            aspect excluded from classpath, intended for the aspect
	 *            invoking the wrapper
	 * @return a list of AspectData from all aspects in ClassPath
	 */
	protected static List<AspectData> getAllAspects(String exclude) {

		List<AspectData> aspectDataList = new ArrayList<AspectData>();
		Aj aj = new Aj();
		String[] aspectclassnames = aj.getNamespace(ClassLoader.getSystemClassLoader()).split(";");
		for (String classname : aspectclassnames) {
			if (!classname.equals(exclude)) {
				aspectDataList.add(new AspectData(classname));
			}
		}
		return aspectDataList;
	}

	/**
	 * 
	 * Wrap the JoinPoint MethodHandle with all appropriate advices.
	 * 
	 * @param JoinPointMH
	 *            MethodHandle at the JoinPoint
	 * @param adviceslist
	 *            list of advices whose pointcuts matched
	 * @param args
	 *            arguments to invoke the JoinPoint MethodHandle
	 * @return the result of the JoinPoint MethodHandle or the applied advices
	 * @throws Throwable
	 */
	protected static Object wrapHandle(MethodHandle JoinPointMH, LinkedList<MatchingAdviceData> adviceslist, Object... args)
			throws Throwable {

		Object result = null;
		Stack<MatchingAdviceData> afterAdvices = new Stack<MatchingAdviceData>();

		if (hasAroundAdvice(adviceslist)) {

			try {
				while (!adviceslist.isEmpty()) {
					MatchingAdviceData iad = adviceslist.pop();

					switch (iad.getAdviceKind()) {
					case BEFORE:
						iad.getAdviceMH().invokeWithArguments(iad.getInvokeArguments());
						break;

					case AROUND:
						AroundClosureImpl aclimpl = new AroundClosureImpl(JoinPointMH, adviceslist, args);
						List<Object> arguments = iad.getInvokeArguments();
						arguments.add(aclimpl);
						result = iad.getAdviceMH().invokeWithArguments(arguments);
						break;

					case AFTER:
						afterAdvices.push(iad);
						break;

					case AFTER_RETURNING:
						afterAdvices.push(iad);
						break;

					case AFTER_THROWING:
						afterAdvices.push(iad);
						break;
					}
				}

				invokeAfterReturningAdvices(result, afterAdvices);

			} catch (Throwable e) {

				invokeAfterThrowingAdvices(afterAdvices, e);
			}

		} else {

			try {
				while (!adviceslist.isEmpty()) {
					MatchingAdviceData iad = adviceslist.pop();

					switch (iad.getAdviceKind()) {
					case BEFORE:
						iad.getAdviceMH().invokeWithArguments(iad.getInvokeArguments());
						break;

					case AFTER:
						afterAdvices.push(iad);
						break;

					case AFTER_RETURNING:
						afterAdvices.push(iad);
						break;

					case AFTER_THROWING:
						afterAdvices.push(iad);
						break;
					}

				}

				// invoke the JoinPoint
				result = JoinPointMH.invokeWithArguments(args);

				invokeAfterReturningAdvices(result, afterAdvices);

			} catch (Throwable e) {

				invokeAfterThrowingAdvices(afterAdvices, e);
			}
		}

		return result;
	}

	/**
	 * 
	 * Invoke all appropriate after and after-throwing advices.
	 * 
	 * @param afterAdvices
	 *            all remaining after or after-throwing advices
	 * @param e
	 *            the thrown exception
	 * @throws Throwable
	 */
	private static void invokeAfterThrowingAdvices(Stack<MatchingAdviceData> afterAdvices, Throwable e) throws Throwable {
		
		// invoke after (throwing)
		while (!afterAdvices.isEmpty()) {
			MatchingAdviceData afterAdviceData = afterAdvices.pop();
			if (!afterAdviceData.getAdviceKind().equals(AdviceKind.AFTER_RETURNING)) {
				try {
					afterAdviceData.getAdviceMH().invokeWithArguments(afterAdviceData.getInvokeArguments());
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
			}
		}
		
		// throw exception
		throw e;
	}

	/**
	 * 
	 * Invoke all appropriate after and after-returning advices.
	 * 
	 * @param result
	 *            the result of invoking the JoinPoint and previous advices
	 * @param afterAdvices
	 *            all remaining after or after-returning advices
	 * @throws Throwable
	 */
	private static void invokeAfterReturningAdvices(Object result, Stack<MatchingAdviceData> afterAdvices) throws Throwable {

		while (!afterAdvices.isEmpty()) {
			MatchingAdviceData afterAdviceData = afterAdvices.pop();
			if (!afterAdviceData.getAdviceKind().equals(AdviceKind.AFTER_THROWING)) {

				List<Object> arguments = afterAdviceData.getInvokeArguments();

				// add JoinPoint return to invocation parameters if necessary
				if (afterAdviceData.getAdviceKind().equals(AdviceKind.AFTER_RETURNING)) {
					if (afterAdviceData.getAdviceMH().type().parameterCount() == arguments.size() + 1) {

						MethodType mt = afterAdviceData.getAdviceMH().type();
						if (mt.parameterArray()[mt.parameterCount() - 1].getClass().isInstance(result)) {

							arguments.add(result);
							afterAdviceData.getAdviceMH().invokeWithArguments(arguments);
						}
						// else drop

					} else {

						afterAdviceData.getAdviceMH().invokeWithArguments(arguments);
					}
				} else {

					afterAdviceData.getAdviceMH().invokeWithArguments(arguments);
				}
			}
		}
	}

	/**
	 * 
	 * Checks if an around advice is in the list.
	 * 
	 * @param adviceslist
	 *            list of all remaining appropriate advices
	 * @return Around advice is in the list?
	 */
	private static boolean hasAroundAdvice(LinkedList<MatchingAdviceData> adviceslist) {

		for (MatchingAdviceData iad : adviceslist) {
			if (iad.getAdviceKind().equals(AdviceKind.AROUND)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * Retrieves all matching advices.
	 * 
	 * @param originalMethod
	 *            reflection object of the called method
	 * @param args
	 *            arguments of the invocation
	 * @return a list of InvokeAspectData with all matching advices
	 */
	protected static LinkedList<MatchingAdviceData> getMatchingAdvices(Method originalMethod, Object... args) {

		LinkedList<MatchingAdviceData> matchingAdviceDataList = new LinkedList<MatchingAdviceData>();

		if (originalMethod != null) {
			Class<?> targetClass = originalMethod.getDeclaringClass();

			for (AspectData aspectData : aspectDataList) {

				for (AdviceData adviceData : aspectData.getAdviceData()) {

					ShadowMatch shadowMatch = adviceData.getToolsPointcutExpression().matchesMethodCall(originalMethod, targetClass);

					if (shadowMatch.alwaysMatches()) {

						try {
							// TODO: parameterize
							// remove target object from args
							Object[] matchArgs = Arrays.copyOfRange(args, 1, args.length);
							// TODO get this object
							JoinPointMatch jpMatch = shadowMatch.matchesJoinPoint(null, args[0], matchArgs);

							if (jpMatch.matches()) {

								List<Object> parameterInstances = getAdviceArgumentsFromJoinPointMatch(targetClass,
										aspectData.getAspectinstance(), jpMatch, adviceData.getAdvice());

								matchingAdviceDataList.add(new MatchingAdviceData(adviceData.getAdviceMH(), parameterInstances, adviceData
										.getAdvice().getKind()));
							}
						} catch (Exception e) {
							// should't happen in general
							e.printStackTrace();
						}
					}
				}
			}
		}

		return matchingAdviceDataList;
	}

	/**
	 * 
	 * Retrieves the arguments for invoking the advice MethodHandle.
	 * 
	 * @param targetClass
	 *            the class on which the method is called
	 * @param aspectinstance
	 *            the instance of the aspect containing the advice
	 * @param joinPointMatch
	 *            the resulting instance of the matching
	 * @param advice
	 *            reflection object of the advice
	 * @return a list of all arguments to invoke the advice
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private static List<Object> getAdviceArgumentsFromJoinPointMatch(Class<?> targetClass, Object aspectinstance,
			JoinPointMatch joinPointMatch, Advice advice) throws InstantiationException, IllegalAccessException {

		PointcutParameter[] bindings = joinPointMatch.getParameterBindings();

		// get parameter Instances from bindings and add
		// aspectinstance
		List<Object> parameterInstances = new ArrayList<Object>();
		parameterInstances.add(aspectinstance);
		for (int k = 0; k < bindings.length; k++) {

			parameterInstances.add(bindings[k].getBinding());
		}

		return parameterInstances;
	}

}
