package ms.vm.lang.python;

import static java.util.Arrays.asList;

import java.util.List;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyType;

import ms.lang.types.BaseMethod;
import ms.lang.types.MethodType;
import ms.lang.types.Variable;

public class PythonMethod extends BaseMethod {
	private final PyObject compiledMethod;
	private PyObject thisObj;
	private final PyType type;

	public PythonMethod(PythonMethod source) {
		this(source, source.type, source.getCompiledMethod());
	}

	public PythonMethod(BaseMethod source, PyType type, PyObject compiledMethod) {
		super(source);
		if (compiledMethod == null || !compiledMethod.isCallable()) {
			throw new IllegalArgumentException("Cannot initialise method '" + source.getName() + "': compiled method "
					+ compiledMethod + " is either null or not callable");
		}
		this.thisObj = null;
		this.type = type;
		this.compiledMethod = compiledMethod;
	}

	@Override
	public PythonMethod setThis(Object thisObj) {
		if (!(thisObj instanceof PyObject)) {
			throw new IllegalArgumentException("Object " + thisObj + " is not a valid Python-object");
		}
		this.thisObj = (PyObject) thisObj;
		return this;
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
		if (getMethodType() == MethodType.METHOD && isStatic()) {
			return args;
		}
		return mode == EXTERNAL_CALL ? args.subList(1, args.size()) : args;
	}

	@Override
	public Object invoke(Object... params) {
		PyObject bound = compiledMethod;
		if (getMethodType() == MethodType.METHOD && !isStatic()) {
			bound = compiledMethod.__get__(thisObj, type);
		}
		// we assume all params are of type PyObject
		// call method using the field to test the new value
		try {
			PyObject[] pyParams = asList(params).toArray(new PyObject[] {});
			logParams(bound, thisObj, params);
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
