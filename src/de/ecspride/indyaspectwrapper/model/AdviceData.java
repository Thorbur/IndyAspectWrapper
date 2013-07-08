package de.ecspride.indyaspectwrapper.model;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * Data Type to store information for distinguishing if an advice should be
 * applied and data to invoke the advice.
 * 
 */
public class AdviceData implements Comparable<AdviceData> {

	private int lineNumber;
	private Advice advice;
	private MethodHandle adviceMH;
	private PointcutExpression pointcutExpression;
	private Method adviceMethod;
	private org.aspectj.weaver.tools.PointcutExpression toolsPointcutExpression;

	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @param lineNumber
	 *            the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * @return the advice
	 */
	public Advice getAdvice() {
		return advice;
	}

	/**
	 * @param advice
	 *            the advice to set
	 */
	public void setAdvice(Advice advice) {
		this.advice = advice;
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
	 * @return the pointcutExpression
	 */
	public PointcutExpression getPointcutExpression() {
		return pointcutExpression;
	}

	/**
	 * @param pointcutExpression
	 *            the pointcutExpression to set
	 */
	public void setPointcutExpression(PointcutExpression pointcutExpression) {
		this.pointcutExpression = pointcutExpression;
	}

	/**
	 * @return the adviceMethod
	 */
	public Method getAdviceMethod() {
		return adviceMethod;
	}

	/**
	 * @param adviceMethod
	 *            the adviceMethod to set
	 */
	public void setAdviceMethod(Method adviceMethod) {
		this.adviceMethod = adviceMethod;
	}

	/**
	 * @return the toolsPointcutExpression
	 */
	public org.aspectj.weaver.tools.PointcutExpression getToolsPointcutExpression() {
		return toolsPointcutExpression;
	}

	/**
	 * @param toolsPointcutExpression
	 *            the toolsPointcutExpression to set
	 */
	public void setToolsPointcutExpression(org.aspectj.weaver.tools.PointcutExpression toolsPointcutExpression) {
		this.toolsPointcutExpression = toolsPointcutExpression;
	}

	@Override
	public int compareTo(AdviceData ad) {

		if (ad != null) {
			return lineNumber - ad.getLineNumber();
		}

		return 0;
	}

}
