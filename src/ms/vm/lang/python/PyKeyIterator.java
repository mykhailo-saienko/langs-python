package ms.vm.lang.python;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;

class PyKeyIterator implements Iterator<String> {
	private final PyList keys;
	private int cur;

	public PyKeyIterator(PyObject obj) {
		keys = ((PyStringMap) obj.fastGetDict()).keys();
		cur = 0;
	}

	@Override
	public boolean hasNext() {
		return cur < keys.size();
	}

	@Override
	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		String res = (String) keys.get(cur);
		++cur;
		return res;
	}

}