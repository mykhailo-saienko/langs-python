package ms.vm.lang.python;

import static java.util.Arrays.asList;
import static ms.ipp.Iterables.appendList;
import static ms.ipp.Iterables.list;
import static ms.ipp.Iterables.map;
import static ms.ipp.Iterables.unique;
import static ms.lang.DeclarationHelper.call;
import static ms.lang.types.BaseMethod.constructor;
import static ms.lang.types.Mod.PUBLIC;
import static ms.lang.types.VarModifier.STANDARD;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.python.core.PyBaseCode;
import org.python.core.PyCode;
import org.python.core.PyFrozenSet;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyMethod;
import org.python.core.PyObject;
import org.python.core.PyReflectedFunction;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.ReflectedArgs;

import ms.ipp.base.KeyValue;
import ms.ipp.base.TriFunction;
import ms.ipp.iterable.tree.path.StdPathManipulator;
import ms.lang.DeclarationHelper;
import ms.lang.types.BaseConstructor;
import ms.lang.types.BaseMethod;
import ms.lang.types.ClassVariable;
import ms.lang.types.Definition;
import ms.lang.types.IType;
import ms.lang.types.MethodType;
import ms.lang.types.TypeName;
import ms.lang.types.Variable;

public class PythonDeclarationHelper {
	public static String decoratedDescription(BaseMethod method) {
		StringBuffer sb = new StringBuffer(1000);
		String deco = getDecoration(method);
		if (!deco.isEmpty()) {
			sb.append(deco).append(" ");
		}
		List<String> varSer = map(method.getArguments(), v -> v.serialize(!isPyObject(v.getTypeName()), true));
		sb.append(call(method.getName(), varSer));
		// the return type is
		if (!isPyObject(method.getReturnType())) {
			sb.append(" -> ").append(method.getReturnType().serialize());
		}
		return sb.toString();
	}

	public static String pythonDeclaration(BaseMethod method, boolean body) {
		StringBuffer sb = new StringBuffer(2000);
		String deco = getDecoration(method);
		if (!deco.isEmpty()) {
			sb.append("\t").append(deco).append("\n");
		}
		sb.append("\tdef ");
		List<String> varSer = map(method.getArguments(), v -> v.serialize(false, true));
		sb.append(call(method.getName(), varSer));
		sb.append(":\n");
		if (body) {
			boolean useDefault = method.getBody() == null || method.getBody().isEmpty();
			String bo = useDefault ? defaultPythonBody(method) : method.getBody();
			sb.append(bo).append("\n");
		}
		return sb.toString();
	}

	public static String defaultPythonBody(BaseMethod m) {
		return "\t\tpass";
	}

	public static String getDecoration(BaseMethod method) {
		if (method.isAuxConstructor()) {
			return "@classmethod";
		} else if (method.isStatic()) {
			return "@staticmethod";
		} else if (method.isAbstract()) {
			return "@abstractmethod";
		}
		return "";
	}

	public static String pythonDeclaration(IType type) {
		StringBuffer sb = new StringBuffer(10000);
		List<String> imports = type.collectImports(false);
		imports = list(imports, i -> !i.startsWith("org.python"));
		if (!imports.isEmpty()) {
			appendList(sb, imports, "", "\n\n", "\n", (i, s) -> {
				KeyValue<String, String> pair = StdPathManipulator.separate(i, false);
				if (!pair.getKey().isEmpty()) {
					sb.append("from ").append(pair.getKey()).append(" ");
				}
				sb.append("import ").append(pair.getValue());
			});
		}
		sb.append("class ").append(type.getSimpleName());
		if (type.getBase() != null) {
			sb.append("(").append(type.getBase().getName()).append(")");
		}
		sb.append(":\n\n");
		for (Definition d : type.definitions(null)) {
			if (d instanceof BaseMethod) {
				BaseMethod m = (BaseMethod) d;
				sb.append(pythonDeclaration(m, true));
			} else {
				Variable v = (Variable) d;
				v.serialize(sb, true, true);
				sb.append("\n");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Returns the representation of object in the Python-source code. <br>
	 * Mainly created for the sake of PyString retaining the single quotation marks
	 * which are lost if we just say object.toString()
	 * 
	 * @param object
	 * @return
	 */
	public static String eval(PyObject object) {
		String interim = new PyList(Arrays.asList(object)).toString();
		return interim.substring(1, interim.length() - 1); // remove brackets
	}

	public static ClassVariable createStaticField(IType targetType, String key, PyObject field) {
		return new ClassVariable(PUBLIC, STANDARD, true, PythonDeclarationHelper.createPyObject(), key, eval(field));
	}

	public static MethodType getMethodType(String name, PyObject method) {
		if (name.equals("__init__")) {
			return MethodType.STD_CONSTRUCTOR;
		}
		if (!(method instanceof PyMethod)) {
			return MethodType.METHOD;
		}
		PyMethod meth = (PyMethod) method;
		PyObject self = meth.__self__;
		if (self == null) {
			return MethodType.METHOD;
		}
		return (self instanceof PyType) ? MethodType.CONSTRUCTOR : MethodType.METHOD;
	}

	public static boolean isStaticMethod(PyObject method) {
		if (method instanceof PyFunction) {
			return true;
		}
		if (method instanceof PyReflectedFunction) {
			return ((PyReflectedFunction) method).argslist[0].isStatic;
		}
		return false;

	}

	public static boolean isAbstractMethod(PyType type, String name, PyObject method) {
		if (isStaticMethod(method)) {
			return false;
		}
		// it is an abstract method from java-interface
		if (method instanceof PyReflectedFunction) {
			PyReflectedFunction func = (PyReflectedFunction) method;
			ReflectedArgs args = func.argslist[0];
			// if a python class only inherits from interfaces,
			// abstract methods are placed here.
			if (args.declaringClass.getCanonicalName().startsWith("org.python.proxies")) {
				return true;
			}

			// otherwise it is stored in the original interface/abstract class
			// and marked abstract
			return Modifier.isAbstract(((Method) args.data).getModifiers());
		} else if (method instanceof PyMethod) {
			// it comes from the ABC metaclass or its subclasses
			PyObject abstracts = type.__findattr__("__abstractmethods__");
			if (abstracts == null || !(abstracts instanceof PyFrozenSet)) {
				return false;
			}
			PyFrozenSet abstractSet = (PyFrozenSet) abstracts;
			return abstractSet.contains(new PyString(name));
		}
		return false;
	}

	public static Variable createSelfArg(List<Variable> args) {
		String self = "self";
		int i = 1;
		Predicate<String> argIsKnown = s -> unique(args, v -> v.getName().equals(s)) != null;
		while (argIsKnown.test(self)) {
			self = "self_" + i++;
		}
		Variable selfVar = new Variable(createPyObject(), self, null);
		return selfVar;
	}

	static PythonMethod createMethod(PyType type, String name, PyObject method) {
		return createMethod(type, name, method, PythonMethod::new);
	}

	static PythonConstructor createConstructor(PyType type, String name, PyObject method) {
		return createMethod(type, name, method, PythonConstructor::new);
	}

	private static <T extends BaseMethod> T createMethod(PyType type, String name, PyObject method,
			TriFunction<BaseMethod, PyType, PyObject, T> gen) {
		MethodType methodType = getMethodType(name, method);
		boolean isStatic = isStaticMethod(method);
		TypeName returnType = methodType == MethodType.METHOD ? createPyObject() : PythonRef.fromPyType(type);
		BaseMethod result = new BaseMethod(PUBLIC, isStatic, returnType, name, "");
		result.setMethodType(methodType);
		result.setArguments(args(method));
		if (isAbstractMethod(type, name, method)) {
			result.setAbstract();
		}

		return gen.apply(result, type, method);
	}

	public static List<Variable> args(Object field) {
		if (field instanceof PyMethod) {
			return args(((PyMethod) field).__func__);
		}

		if (field instanceof PyReflectedFunction) {
			List<Variable> result = new ArrayList<>();
			Method m = (Method) ((PyReflectedFunction) field).argslist[0].data;
			Parameter[] params = m.getParameters();
			for (int i = 0; i < params.length; ++i) {
				result.add(new Variable(createPyObject(), params[i].getName(), null));
			}
			return result;
		}

		if (field instanceof PyFunction) {
			PyCode code = ((PyFunction) field).__code__;
			if (code instanceof PyBaseCode) {
				PyBaseCode bCode = (PyBaseCode) code;
				List<String> varNames = bCode.co_varnames == null ? new ArrayList<>() : asList(bCode.co_varnames);
				PyObject[] defs = ((PyFunction) field).__defaults__;
				List<Variable> vars = map(varNames, s -> new Variable(createPyObject(), s, null));
				// the last defs.length args have default values
				if (defs != null) {
					for (int i = 0; i < defs.length; ++i) {
						vars.get(vars.size() - defs.length + i).setExpression(eval(defs[i]));
					}
				}
				return vars;
			}
		}
		if (field instanceof BaseMethod) {
			List<Variable> args = new ArrayList<>(((BaseMethod) field).getArguments());
			if (!(field instanceof PythonMethod)) {
				args.add(0, createSelfArg(args));
			}
			return args;
		}
		throw new IllegalArgumentException("Unknown method " + field);
	}

	public static List<BaseConstructor> createPythonConstructor(IType type, BaseConstructor baseCr) {
		// in python, we may only call the super type's standard constructor
		// (i.e. __init__). The auxiliary constructors (class methods) won't do
		if (!baseCr.isStdConstructor()) {
			throw new IllegalArgumentException("Cannot create standard constructor for Python-type '"
					+ type.getFullName() + "' the given constructor " + baseCr + " is not standard constructor");
		}

		// no fields in python, hence, we have only one constructor by default.
		// All member methods contain "self" by default
		List<Variable> args = baseCr == null ? asList(createSelfArg(null)) : args(baseCr);
		String body = baseCr == null ? "pass"
				: DeclarationHelper.varCall(baseCr.getReturnType().getName() + ".__init__", args);
		return asList(constructor(PUBLIC, true, type, "__init__", args, "\t\t" + body));
	}

	public static TypeName createPyObject() {
		Class<PyObject> clazz = PyObject.class;
		return new TypeName(clazz.getSimpleName(), clazz.getCanonicalName(), false);
	}

	public static boolean isPyObject(TypeName typeName) {
		return typeName.getFullName().equals(PyObject.class.getCanonicalName());
	}

}
