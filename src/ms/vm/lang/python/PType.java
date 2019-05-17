package ms.vm.lang.python;

import static ms.vm.lang.python.PythonDeclarationHelper.createConstructor;
import static ms.vm.lang.python.PythonDeclarationHelper.createMethod;
import static ms.vm.lang.python.PythonDeclarationHelper.createStaticField;
import static ms.vm.lang.python.PythonRef.entryIterator;
import static ms.vm.lang.python.PythonRef.fromPyType;

import java.util.List;
import java.util.function.BiFunction;

import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyType;

import ms.ipp.Iterables;
import ms.lang.DeclarationHelper;
import ms.lang.Language;
import ms.lang.types.ClassVariable;
import ms.lang.types.IType;
import ms.lang.types.Instance;
import ms.lang.types.MethodType;
import ms.lang.types.Type;

public class PType extends Type {

	private PyType type;

	public PType(PyType type, BiFunction<List<String>, String, IType> typeProvider) {
		super(fromPyType(type), Language.PYTHON, fromPyType(getBase(type)));
		this.update(type, typeProvider);
	}

	public PyType getCompiled() {
		return type;
	}

	@Override
	public boolean isAssignableFrom(IType type) {
		if (!(type instanceof PType)) {
			return false;
		}
		PType pType = (PType) type;
		if (type == null || pType.getCompiled() == null) {
			return false;
		}
		return pType.getCompiled().isSubType(this.getCompiled());
	}

	public void update(PyType type, BiFunction<List<String>, String, IType> typeProvider) {
		if (this.type != null) {
			throw new IllegalArgumentException("Python Type is already set for '" + getFullName() + "'");
		}
		if (!getFullName().endsWith(type.fastGetName())) {
			throw new IllegalArgumentException("Python Type's name '" + type.fastGetName()
					+ "' is not similiar to the type's full name '" + getFullName() + "'");
		}
		Iterables.forEach(entryIterator(type), (s, o) -> updateField(type, s, o));
		// although the base is set using PyType, if it is java-class, it will
		// be looked upon as such and not as PyType
		setAbstract(DeclarationHelper.isAbstract(this, typeProvider));
		this.setBase(fromPyType(getBase(type)));
		this.type = type;
	}

	@Override
	public Instance getTypeRef() {
		PyType type = getCompiled();
		return (type == null) ? null : new PythonRef(type);
	}

	private void updateField(PyType source, String name, PyObject field) {
		if (logger.isTraceEnabled()) {
			logger.debug("Adding " + (field.isCallable() ? "callable" : "non-callable") + " field " + field + " of "
					+ field.getClass());
		}
		if (field.isCallable()) {
			MethodType methodType = PythonDeclarationHelper.getMethodType(name, field);

			if (methodType == MethodType.METHOD) {
				addMethod(createMethod(source, name, field), null);
			} else {
				addConstructor(createConstructor(source, name, field), null);
			}
		} else {
			ClassVariable var = createStaticField(this, name, field);
			addVariable(var, null);
		}
	}

	private static PyType getBase(PyType type) {
		PyType base = (PyType) type.getBase();
		// get rid of the proxy object.
		if (base instanceof PyJavaType) {
			return (PyType) base.getBase();
		} else {
			return base;
		}
	}
}
