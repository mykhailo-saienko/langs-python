package ms.vm.lang.python;

import static ms.ipp.Iterables.filtered;
import static ms.ipp.Iterables.mapped;
import static ms.ipp.Iterables.toBiIt;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.python.core.PyObject;
import org.python.core.PyType;

import ms.ipp.base.KeyValue;
import ms.ipp.iterable.BiIterable;
import ms.ipp.iterable.tree.DelegatingTree;
import ms.ipp.iterable.tree.SyntheticTree;
import ms.ipp.iterable.tree.path.StdPathManipulator;
import ms.lang.types.Instance;
import ms.lang.types.TypeName;

public class PythonRef extends DelegatingTree<Instance> implements Instance {

	private final PyObject obj;

	/// Python Member Ref
	public PythonRef(PyObject obj) {
		super(Instance.class);
		this.obj = obj;

		// TODO: We should not be able to delete members that are types!
		Function<String, String> ident = Function.identity();
		Function<String, Instance> converter = s -> new PythonRef(obj.__findattr__(s));
		BiConsumer<String, Instance> setter = (s, r) -> obj.__setattr__(s, (PyObject) r.getValue());
		SyntheticTree<Instance> memberEntity = new SyntheticTree<>(converter, setter, ident, ident, keys(obj),
				Instance.class);
		memberEntity.setDeleter(s -> {
			boolean result = obj.__findattr__(s) != null;
			obj.__delattr__(s);
			return result;
		});
		add(memberEntity);
	}

	@Override
	public Object getValue() {
		return obj;
	}

	@Override
	public TypeName getTypeName() {
		return fromPyType(obj.getType());
	}

	static String getFullName(PyType type) {
		return type.getModule() + "." + type.getName();
	}

	static TypeName fromPyType(PyType type) {
		String fullName = PythonRef.getFullName(type);
		return new TypeName(StdPathManipulator.toSimpleName(fullName), fullName, false);
	}

	public static Iterable<String> keys(PyObject obj) {
		return filtered(() -> new PyKeyIterator(obj), PythonRef::isEssential);
	}

	public static BiIterable<String, PyObject> entryIterator(PyObject obj) {
		// we use source.__findattr__ since it returns static-/class-methods
		// which are already bound to the type (as opposed to bare
		// PyClass/StaticMethods returned by dict.get)
		return toBiIt(mapped(keys(obj), s -> new KeyValue<>(s, obj.__findattr__(s))));
	}

	private static boolean isEssential(String key) {
		return !key.startsWith("_") || key.equals("__init__");
	}
}
