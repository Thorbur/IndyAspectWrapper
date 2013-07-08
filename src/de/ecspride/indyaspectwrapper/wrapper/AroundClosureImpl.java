package de.ecspride.indyaspectwrapper.wrapper;

import java.lang.invoke.MethodHandle;
import java.util.LinkedList;

import org.aspectj.runtime.internal.AroundClosure;

import de.ecspride.indyaspectwrapper.model.MatchingAdviceData;

/**
 * Implementation of the AroundClosure Interface. Invokes the proceed call of an
 * around advice.
 * 
 */
class AroundClosureImpl extends AroundClosure {

	MethodHandle JoinPointMH;
	Object[] args;
	LinkedList<MatchingAdviceData> iadlist;

	/**
	 * The constructor.
	 * 
	 * @param JoinPointMH
	 *            the MethodHandle of the JoinPoint
	 * @param adviceslist
	 *            the list of the remaining advices to invoke
	 * @param args
	 *            the arguments to invoke the JoinPoint MethodHandle
	 */
	public AroundClosureImpl(MethodHandle JoinPointMH, LinkedList<MatchingAdviceData> adviceslist, Object... args) {
		this.JoinPointMH = JoinPointMH;
		this.iadlist = adviceslist;
		this.args = args;
	}

	@Override
	public Object run(Object[] arg0) throws Throwable {

		return IndyJoinPointWrapper.wrapHandle(JoinPointMH, iadlist, args);
	}

}
