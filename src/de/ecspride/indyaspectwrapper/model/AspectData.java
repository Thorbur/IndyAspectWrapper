package de.ecspride.indyaspectwrapper.model;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;

import org.aspectj.internal.lang.reflect.AdviceImpl;
import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.Pointcut;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.UnsupportedPointcutPrimitiveException;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * Data Type to store information about the advice. The constructor retrieves
 * all data and initializes all fields. The access via the AspectJ Reflection
 * API happens here.
 * 
 */
public class AspectData {

	private Object aspectinstance = null;
	private Pointcut[] decPointcuts = null;
	private AdviceData[] adviceData = null;

	static ClassPool pool = ClassPool.getDefault();

	// Pointcut Parser
	static PointcutParser pparser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();

	static boolean warn = true;

	static {
		// disable warnings for not found types while parsing pointcut
		// expressions
		Properties props = new Properties();
		props.put("invalidAbsoluteTypeName", "ignore");
		pparser.setLintProperties(props);

		// activate/ deactivate logging
		String envwarn = System.getenv("warn");
		if (envwarn != null) {
			warn = Boolean.parseBoolean(envwarn);
		}
	}

	PairMap<String, String, PointcutParameter[]> pointcutMap;

	/**
	 * 
	 * Initializes all fields. Retrieves data from the aspect via reflection.
	 * 
	 * @param aspectClassName
	 *            the name of the aspect class
	 */
	public AspectData(String aspectClassName) {

		try {

			Class<?> c = Class.forName(aspectClassName);
			AjType<?> ajType = AjTypeSystem.getAjType(c);
			MethodHandle aspectof = MethodHandles.lookup().findStatic(c, "aspectOf", methodType(c));
			aspectinstance = aspectof.invoke();

			Advice[] advices = ajType.getAdvice();
			decPointcuts = ajType.getDeclaredPointcuts();
			pointcutMap = getPointcutInformation(decPointcuts);

			adviceData = new AdviceData[advices.length];

			for (int i = 0; i < advices.length; i++) {

				AdviceData ad = new AdviceData();
				ad.setAdvice(advices[i]);

				Field field = AdviceImpl.class.getDeclaredField("adviceMethod");
				field.setAccessible(true);
				Method advicemethod = (Method) field.get(advices[i]);
				ad.setAdviceMethod(advicemethod);

				ad.setAdviceMH(MethodHandles.lookup().unreflect(advicemethod));
				ad.setPointcutExpression(advices[i].getPointcutExpression());

				// get line number from Javassist
				CtClass cc = pool.get(advicemethod.getDeclaringClass().getCanonicalName());
				ad.setLineNumber(cc.getDeclaredMethod(advicemethod.getName()).getMethodInfo().getLineNumber(0));

				ad.setToolsPointcutExpression(getPex(ad));

				adviceData[i] = ad;
			}

			// sort adviceData by line numbers for having the advices ordered
			// like in source code
			Arrays.sort(adviceData);

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * Retrieves the pointcut expression from the advice data.
	 * 
	 * @param adviceData
	 *            the data of the advice
	 * @return the pointcut expression of the advice
	 */
	private PointcutExpression getPex(AdviceData adviceData) {

		org.aspectj.weaver.tools.PointcutExpression pex = null;

		// get Pointcut Signature
		// pointcut expressions are always in brackets
		// pointcut names aren't in brackets
		String potentialName = adviceData.getPointcutExpression().asString().replaceAll("\\(.*\\)", "");
		String pcex = "because of error getting Expression String";
		try {

			if (pointcutMap.containsKey(potentialName)) {
				pcex = pointcutMap.getFirst(potentialName);

				// remove <clinit()> in staticinitialization pointcut expression
				pcex = pcex.replace(".<clinit>()", "");

				pex = pparser.parsePointcutExpression(pcex, aspectinstance.getClass(), pointcutMap.getSecond(potentialName));

			} else {
				pcex = adviceData.getPointcutExpression().asString();

				// remove <clinit()> in staticinitialization pointcut expression
				pcex = pcex.replace(".<clinit>()", "");

				PointcutParameter[] pointcutParameters = resolvePointcutParameters(adviceData.getAdvice(), adviceData.getAdviceMethod(),
						pcex);

				pex = pparser.parsePointcutExpression(pcex, aspectinstance.getClass(), pointcutParameters);

			}
		} catch (IllegalArgumentException | ClassNotFoundException | UnsupportedPointcutPrimitiveException e) {

			// REMOVE logging
			if (warn) {
				System.err.println("Can't parse " + pcex);
				System.err.println(e.getMessage());
			}
		}
		return pex;
	}

	/**
	 * 
	 * Get information of all declared pointcuts and store them in a PairMap.
	 * 
	 * @param pointcuts
	 *            all declared pointcuts of the aspect
	 * @return a PairMap with the Pointcut name as key, PointcutExpression
	 *         String and PointcutParameter[] as values
	 */
	private PairMap<String, String, PointcutParameter[]> getPointcutInformation(Pointcut[] pointcuts) {

		PairMap<String, String, PointcutParameter[]> pointcutMap = new PairMap<String, String, PointcutParameter[]>();
		for (Pointcut pc : pointcuts) {
			String[] parameterNames = pc.getParameterNames();
			AjType<?>[] parameterTypes = pc.getParameterTypes();
			PointcutParameter[] pointcutParameters = new PointcutParameter[parameterNames.length];
			for (int k = 0; k < parameterNames.length; k++) {
				pointcutParameters[k] = pparser.createPointcutParameter(parameterNames[k], parameterTypes[k].getJavaClass());
			}
			pointcutMap.put(pc.getName(), pc.getPointcutExpression().asString(), pointcutParameters);
		}
		return pointcutMap;
	}

	static Paranamer paranamer = new BytecodeReadingParanamer();

	/**
	 * 
	 * Resolve all pointcut parameters with the help of Paranamer.
	 * 
	 * @param advice
	 *            the advice
	 * @param adviceMethod
	 *            the Method object of the advice
	 * @param pcex
	 *            the pointcut expression of the advice
	 * @return an array with all pointcut parameters for the given advice
	 * @throws ClassNotFoundException
	 */
	private PointcutParameter[] resolvePointcutParameters(Advice advice, Method adviceMethod, String pcex) throws ClassNotFoundException {
		AjType<?>[] adviceParameterTypes = advice.getParameterTypes();

		String[] parameterNames = paranamer.lookupParameterNames(adviceMethod);

		List<PointcutParameter> pointcutParameters = new ArrayList<PointcutParameter>();
		int k = 0;
		for (String parameterName : parameterNames) {
			if (isParameterNameInPointcutExpression(pcex, parameterName)) {
				pointcutParameters.add(pparser.createPointcutParameter(parameterName, Class.forName(adviceParameterTypes[k].getName())));
			}
			k++;
		}

		return pointcutParameters.toArray(new PointcutParameter[pointcutParameters.size()]);
	}

	private static final Pattern argsPattern = Pattern.compile("args\\([a-zA-Z0-9]+(,\\s*[a-zA-Z0-9]+)*\\)");
	private static final Pattern thisPattern = Pattern.compile("this\\([a-zA-Z0-9]+\\)");
	private static final Pattern targetPattern = Pattern.compile("target\\([a-zA-Z0-9]+\\)");

	/**
	 * 
	 * Check if a parameter name is used in a pointcut expression.
	 * 
	 * @param pointcutExpression
	 *            a pointcut expression as String
	 * @param parameterName
	 *            the name of a parameter
	 * @return Does the pointcut expression contain the parameter name?
	 */
	private boolean isParameterNameInPointcutExpression(String pointcutExpression, String parameterName) {

		Matcher argsMatcher = argsPattern.matcher(pointcutExpression);
		if (argsMatcher.find()) {
			String plainArgs = argsMatcher.group().replaceAll("args\\(", "").replaceAll("\\)", "");
			String[] args = plainArgs.split(",");

			for (String arg : args) {
				if (arg.trim().equals(parameterName)) {
					return true;
				}
			}
		}

		Matcher thisMatcher = thisPattern.matcher(pointcutExpression);
		if (thisMatcher.find()) {
			String plainThis = thisMatcher.group().replaceAll("this\\(", "").replaceAll("\\)", "").trim();

			if (plainThis.equals(parameterName)) {
				return true;
			}

		}

		Matcher targetMatcher = targetPattern.matcher(pointcutExpression);
		if (targetMatcher.find()) {
			String plainTarget = targetMatcher.group().replaceAll("target\\(", "").replaceAll("\\)", "").trim();

			if (plainTarget.equals(parameterName)) {
				return true;
			}

		}

		return false;
	}

	/**
	 * @return the aspect instance
	 */
	public Object getAspectinstance() {
		return aspectinstance;
	}

	/**
	 * @param aspectinstance
	 *            the aspect instance to set
	 */
	public void setAspectinstance(Object aspectinstance) {
		this.aspectinstance = aspectinstance;
	}

	/**
	 * @return the decPointcuts
	 */
	public Pointcut[] getDecPointcuts() {
		return decPointcuts;
	}

	/**
	 * @param decPointcuts
	 *            the decPointcuts to set
	 */
	public void setDecPointcuts(Pointcut[] decPointcuts) {
		this.decPointcuts = decPointcuts;
	}

	/**
	 * @return the adviceData
	 */
	public AdviceData[] getAdviceData() {
		return adviceData;
	}

	/**
	 * @param adviceData
	 *            the adviceData to set
	 */
	public void setAdviceData(AdviceData[] adviceData) {
		this.adviceData = adviceData;
	}

}
