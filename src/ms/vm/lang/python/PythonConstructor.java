package ms.vm.lang.python;

import static java.util.Arrays.asList;

import java.util.List;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyType;

import ms.lang.types.BaseConstructor;
import ms.lang.types.BaseMethod;
import ms.lang.types.Variable;

public class PythonConstructor extends BaseConstructor {
	private final PyObject compiledMethod;
	private final PyType type;

	public PythonConstructor(PythonConstructor source) {
		this(source, source.type, source.getCompiledMethod());
	}

	public PythonConstructor(BaseMethod source, PyType type, PyObject compiledMethod) {
		super(source);
		if (compiledMethod == null || !compiledMethod.isCallable()) {
			throw new IllegalArgumentException("Cannot initialise method '" + source.getName() + "': compiled method "
					+ compiledMethod + " is either null or not callable");
		}
		this.type = type;
		this.compiledMethod = compiledMethod;
	}

	public PyObject getCompiledMethod() {
		return compiledMethod;
	}

	/**
	 * Python methods store the self/cls variables given in their definitions.
	 * If, however, a class method or the type factory (__new__/__init__) or a
	 * member methods are called externally (i.e. outside of the class they are
	 * defined in), the first parameter must be omitted.
	 */
	@Override
	public List<Variable> getArguments(int mode) {
		List<Variable> args = getArguments();
		return mode == EXTERNAL_CALL ? args.subList(1, args.size()) : args;
	}

	@Override
	public Object invoke(Object... params) {
		// if std, then it is the __init__ otherwise it is one of the static
		// methods which do not require bounding
		PyObject bound = isStdConstructor() ? type : compiledMethod;

		// we assume all params are of type PyObject
		// call method using the field to test the new value
		try {
			PyObject[] pyParams = asList(params).toArray(new PyObject[] {});
			logParams(bound, null, params);
			PyObject result = bound.__call__(pyParams);
			logResult(result);
			return result;
		} catch (PyException e) {
			throw PythonRefHelper.convertError(e, type.getName());
		}
	}

	@Override
	public String getSignature() {
		return PythonDeclarationHelper.decoratedDescription(this);
	}
}
