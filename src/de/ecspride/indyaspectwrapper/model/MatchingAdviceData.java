package de.ecspride.indyaspectwrapper.model;

import java.lang.invoke.MethodHandle;
import java.util.List;

import org.aspectj.lang.reflect.AdviceKind;

/**
 * Data Type for invoking advices whose pointcut expression matched. Uses a
 * MethodHandle of the advice for invocation.
 * 
 */
public class MatchingAdviceData {

	private MethodHandle adviceMH;
	private List<Object> invokeArguments;
	private AdviceKind adviceKind;

	/**
	 * 
	 * The constructor.
	 * 
	 * @param adviceMH
	 *            the MethodHandle of the advice
	 * @param invokeArguments
	 *            the arguments to invoke the advice MethodHandle
	 * @param adviceKind
	 *            the kind of the advice
	 */
	public MatchingAdviceData(MethodHandle adviceMH, List<Object> invokeArguments, AdviceKind adviceKind) {

		this.adviceMH = adviceMH;
		this.invokeArguments = invokeArguments;
		this.adviceKind = adviceKind;
	}

	/**
	 * @return the adviceMH
	 */
	public MethodHandle getAdviceMH() {
		return adviceMH;
	}

	/**
	 * @param adviceMH
	 *            the adviceMH to set
	 */
	public void setAdviceMH(MethodHandle adviceMH) {
		this.adviceMH = adviceMH;
	}

	/**
	 * @return the invokeArguments
	 */
	public List<Object> getInvokeArguments() {
		return invokeArguments;
	}

	/**
	 * @param invokeArguments
	 *            the invokeArguments to set
	 */
	public void setInvokeArguments(List<Object> invokeArguments) {
		this.invokeArguments = invokeArguments;
	}

	/**
	 * @return the adviceKind
	 */
	public AdviceKind getAdviceKind() {
		return adviceKind;
	}

	/**
	 * @param adviceKind
	 *            the adviceKind to set
	 */
	public void setAdviceKind(AdviceKind adviceKind) {
		this.adviceKind = adviceKind;
	}

}
