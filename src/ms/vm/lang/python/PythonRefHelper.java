package ms.vm.lang.python;

import java.util.Arrays;

import org.python.core.Py;
import org.python.core.PyBaseException;
import org.python.core.PyException;
import org.python.core.PyTuple;
import org.python.core.PyType;

import ms.parser.error.Error;
import ms.parser.error.ParseError;

public class PythonRefHelper {

	public static RuntimeException convertError(PyException e, String className) {
		PyType eType = (PyType) e.type;
		if (eType.getName().equals("SyntaxError")) {
			return createParseError(className, e);
		} else if (e.value instanceof PyBaseException) {
			PyBaseException pe = (PyBaseException) e.value;
			String message = pe.getMessage().__tojava__(Object.class).toString();
			return new IllegalArgumentException(eType.getName() + " at line " + e.traceback.tb_lineno + ": " + message);
		} else {
			// this comes from invoking python methods which invoke java methods
			Object javaEx = e.value.__tojava__(RuntimeException.class);
			if (javaEx != Py.NoConversion) {
				return (RuntimeException) javaEx;
			}
		}

		return e;
	}

	private static ParseError createParseError(String fullName, PyException e) {
		PyTuple tuple = (PyTuple) e.value;
		if (tuple.size() != 2) {
			throw new IllegalArgumentException("Unrecognized SyntaxError " + e);
		}
		String message = (String) tuple.get(0);
		// arguments
		tuple = (PyTuple) tuple.get(1);
		String symbol = (String) tuple.get(3);
		// remove trailing new line.
		symbol = symbol.substring(0, symbol.length() - 1);
		Integer line = (Integer) tuple.get(1);
		Integer column = (Integer) tuple.get(2);
		Error err = new Error(symbol, line, column, message);
		return new ParseError("Parsing '" + fullName + "' failed", Arrays.asList(err));
	}
}
