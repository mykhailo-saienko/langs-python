package ms.vm.lang.python;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ms.ipp.Iterables;
import ms.lang.AbstractLS;
import ms.lang.java.JType;
import ms.lang.types.BaseMethod;
import ms.lang.types.IType;
import ms.lang.types.TypeName;

public class PythonLS extends AbstractLS {
	public static final String PACKAGE_NAME_OPTION = "PackageName";

	private static final Logger logger = LogManager.getLogger();

	private final PyInterpreter interpreter;

	public PythonLS(String customLib, String javaClassPath, ClassLoader javaClassLoader,
			BiFunction<List<String>, String, IType> typer) {
		interpreter = new PyInterpreter(customLib, javaClassPath, javaClassLoader, typer);
	}

	@Override
	public boolean isSupported(BaseMethod method) {
		return method instanceof PythonMethod;
	}

	@Override
	public boolean isSupported(IType type) {
		return type instanceof PType || type instanceof JType;
	}

	@Override
	public boolean isNative(IType type) {
		return type instanceof PType;
	}

	@Override
	public Object eval(String source, TypeName result) {
		if (!PythonDeclarationHelper.isPyObject(result)) {
			throw new IllegalArgumentException("TypeName " + result + " is not supported");
		}
		return interpreter.eval(source);
	}

	@Override
	public void forEachUserType(String packageName, Predicate<IType> pred, Consumer<IType> processor) {
		interpreter.forEachUserType(packageName, pred, processor);
	}

	@Override
	public void forEachType(String packageName, Predicate<IType> pred, Consumer<IType> processor) {
		// TODO: Maybe we will find a means of listing all types in a
		// package/module. As of 2018-12-22, iterating over user-defined
		// types has sufficed for all projects.
		forEachUserType(packageName, pred, processor);
	}

	@Override
	public boolean deleteType(String typeName) {
		return interpreter.deleteType(typeName);
	}

	@Override
	public void close() {
		super.close();
		interpreter.close();
	}

	@Override
	protected List<IType> extract(String source, Map<String, Object> options) {
		String packageName = Iterables.get(PACKAGE_NAME_OPTION, options);
		List<IType> types = new PythonTypeExtractor(source, packageName).getTypes();
		if (types.size() != 1) {
			throw new IllegalArgumentException("Only one type per module is supported. Found types " + types);
		}
		logger.debug("Extracted type {}", () -> types.get(0).getFullName());
		return types;
	}

	@Override
	protected void compile(String source, List<IType> types, Map<String, Object> options) {
		types.set(0, interpreter.compile(source, types.get(0)));
	}

	@Override
	protected List<TypeLoader> initTypeLoaders() {
		return Arrays.asList(new TypeLoader(this::loadType));
	}

	private IType loadType(String typeName) {
		return interpreter.loadType(typeName);
	}

	@Override
	protected boolean isCrossLSAssignableFrom(IType base, IType target) {
		// types are from different LS -> use external checkers
		// target must be python, base must be java
		if (!(target instanceof PType) || !(base instanceof JType)) {
			return false;
		}

		PType pTarget = (PType) target;
		if (logger.isTraceEnabled()) {
			logger.trace("pTarget: {} with class {}", pTarget, pTarget.getCompiled());
		}
		if (pTarget.getCompiled() == null) {
			return false;
		}
		Class<?> targetClass = (Class<?>) pTarget.getCompiled().__tojava__(Class.class);
		Class<?> baseClass = ((JType) base).getCompiled();
		logger.trace("targetClass: {}, base: {}", targetClass.getSimpleName(), baseClass.getSimpleName());
		return baseClass != null && baseClass.isAssignableFrom(targetClass);
	}

	/**
	 * This is a temporary method so that de.ms.compiler.TestPython compiles
	 * 
	 * @return
	 */
	public PyInterpreter getInterpreter() {
		return interpreter;
	}
}
