package ms.vm.lang.python;

import static ms.ipp.Iterables.appendList;
import static ms.ipp.iterable.tree.path.StdPathManipulator.separate;
import static ms.utils.IOHelper.loadFromFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.PyClass;
import org.python.core.PyException;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import ms.ipp.base.KeyValue;
import ms.ipp.iterable.tree.path.StdPathManipulator;
import ms.lang.types.IType;
import ms.utils.IOHelper;
import ms.utils.LogFile;

public class PyInterpreter extends PythonInterpreter {
	private static final Logger logger = LogManager.getLogger();

	private final String customLib;
	private final BiFunction<List<String>, String, IType> userTyper;
	// user-defined types
	private final Map<String, PType> pTypes;

	public PyInterpreter(String customLib, String javaClassPath, ClassLoader javaClassLoader,
			BiFunction<List<String>, String, IType> typer) {
		logger.warn("Starting Py-Interpreter...");

		// set java class loader that supports user-defined classes
		this.userTyper = typer;
		if (javaClassLoader != null) {
			getSystemState().setClassLoader(javaClassLoader);
		}

		// user-defined python modules are stored in the custom lib folder
		initCustomLib(customLib, javaClassPath);
		this.customLib = customLib;

		// used to load user-defined classes without polluting the interpreter
		// with 'from ... import ...' statements.
		try {
			String classLoader = loadFromFile("class_loader.py");
			exec(classLoader);
			logger.info("Class loader is loaded successfully");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		pTypes = new HashMap<>();

		logger.warn("Py-Interpreter started successfully...");
	}

	@Override
	public void close() {
		logger.warn("Stopping Py-Interpreter...");
		super.close();
		logger.warn("Py-Interpreter stopped successfully...");
	}

	boolean deleteType(String typeName) {
		PType pType = pTypes.remove(typeName);
		if (pType == null) {
			return false;
		}
		rewriteModule(typeName, null);
		return true;
	}

	void forEachUserType(String packageName, Predicate<IType> pred, Consumer<IType> processor) {
		for (PType t : pTypes.values()) {
			if (t.getFullName().startsWith(packageName) && (pred == null || pred.test(t))) {
				processor.accept(t);
			}
		}
	}

	public PType compile(String source, IType prototype) {
		if (customLib == null) {
			throw new IllegalStateException("The working lib is not set");
		}
		String fullName = prototype.getFullName();
		if (logger.isTraceEnabled()) {
			KeyValue<String, String> separated = separate(fullName, false);
			logger.debug("Compiling type {}: package name={}, simple name={}", fullName, separated.getKey(),
					separated.getValue());
		}

		// save source only if test compilation is alright
		try {
			compile(source, fullName);
			saveModule(fullName, source);
			PType loaded = loadType(fullName);
			if (loaded == null) {
				throw new IllegalStateException("Failed to load Python-type " + fullName);
			}
			// user-defined python types are always fleeting
			loaded.setFleeting(true);
			pTypes.put(fullName, loaded);
			return loaded;
		} catch (PyException e) {
			throw PythonRefHelper.convertError(e, fullName);
		}
	}

	PType loadType(String fullName) {
		try {
			// TODO: We should check if the type is stored in pTypes first
			// (prior to loading the type)?
			PyObject result = eval("load_class('" + fullName + "')");
			if (result instanceof PyNone) {
				return null;
			} else if (result instanceof PyClass) {
				throw new IllegalArgumentException("'" + fullName
						+ "' is an old-style class. Only new-style Python classes (Types) are fully supported");
			}
			return new PType((PyType) result, userTyper);
		} catch (PyException e) {
			throw PythonRefHelper.convertError(e, fullName);
		}
	}

	private void saveModule(String fullName, String sourceCode) {
		String packageName = StdPathManipulator.getPackage(fullName);
		if (packageName.equals("")) {
			return;
		}

		KeyValue<String, String> separated = StdPathManipulator.separate(packageName, false);
		String dirs = separated.getKey();
		// all python-classes from the same module must be stored in one file
		// with the name equals to the name of the last package level.
		File source = new File(customLib);
		if (!dirs.isEmpty()) {
			for (String d : dirs.split("\\.")) {
				File interim = new File(source, "/" + d + ".py");
				if (interim.exists() && interim.isFile()) {
					throw new IllegalArgumentException(
							"Cannot create a module '" + packageName + "' as the node '" + d + "' is terminal");
				}
				source = new File(source, "/" + d);
				if (!source.exists()) {
					logger.trace("Creating dir {}", source.getAbsolutePath());
					source.mkdir();
				}
				// to turn the folder into a py-package
				File init = new File(source, "/__init__.py");
				if (!init.exists()) {
					try {
						init.createNewFile();
					} catch (IOException e) {
						throw new IllegalArgumentException("Error while creating " + init.getAbsolutePath(), e);
					}
				}
			}
		}

		// re-write the source with the code of all existing classes + new one.
		rewriteModule(fullName, sourceCode);
	}

	private void rewriteModule(String fullName, String sourceCode) {
		String packageName = StdPathManipulator.getPackage(fullName);
		List<String> sources = new ArrayList<>();
		// ignore the type we want to rewrite (if it exists at all)
		forEachUserType(packageName, t -> !t.getFullName().equals(fullName), t -> sources.add(t.getSource()));

		if (sourceCode != null && !sourceCode.isEmpty()) {
			sources.add(sourceCode);
		}
		String baseFileName = getPath(packageName);
		if (sources.isEmpty()) {
			new File(baseFileName + ".py").delete();
			new File(baseFileName + "$py.class").delete();
		} else {
			try {
				String bigSource = appendList(sources, "", "", "\n\n", (t, s) -> s.append(t));
				LogFile.save(bigSource, baseFileName + ".py", LogFile.UTF_8);
			} catch (IOException e) {
				throw new IllegalArgumentException(
						"Error while saving type '" + fullName + "' to file '" + baseFileName + ".py" + "': " + e);
			}
		}
	}

	private String getPath(String moduleName) {
		List<String> levels = StdPathManipulator.fromPath(moduleName);
		return new File(customLib).getAbsolutePath()
				+ appendList(levels, "", "", "", (l, s) -> s.append("/").append(l));
	}

	private void initCustomLib(String customLib, String javaClassPath) {
		File file = new File(customLib);
		if (file.exists()) {
			try {
				IOHelper.deleteAll(file.getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalArgumentException("Cannot delete the working lib '" + file.getAbsolutePath() + "'");
			}
		}
		file.mkdirs();
		exec("import sys");
		// internally produced modules
		exec("sys.path.append(\"" + file.getAbsolutePath() + "\")");
		// external modules
		exec("sys.path.append(\"" + javaClassPath + "\")");
	}

}
