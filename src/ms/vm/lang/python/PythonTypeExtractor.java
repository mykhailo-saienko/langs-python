package ms.vm.lang.python;

import static ms.ipp.Iterables.filterMap;
import static ms.lang.Language.PYTHON;
import static ms.parser.ParserHelper.checkErrors;
import static ms.parser.ParserHelper.getParser;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ms.lang.types.IType;
import ms.lang.types.Type;
import ms.parser.error.ParseError;
import ms.vm.lang.python.gen.PythonHeaderBaseListener;
import ms.vm.lang.python.gen.PythonHeaderLexer;
import ms.vm.lang.python.gen.PythonHeaderParser;
import ms.vm.lang.python.gen.PythonHeaderParser.ElementContext;

public class PythonTypeExtractor extends PythonHeaderBaseListener {

	private static final Logger logger = LogManager.getLogger();
	private final List<IType> type;

	public PythonTypeExtractor(String pythonSource, String packageName) {
		PythonHeaderParser parser = getParser(pythonSource, PythonHeaderLexer::new, PythonHeaderParser::new);
		List<ElementContext> classDecls = parser.main().element();
		checkErrors(parser, "Error while recognizing Python class", ParseError.class);
		type = filterMap(classDecls, e -> e.classHeader() != null,
				e -> Type.prototype(packageName, e.classHeader().ID().getText(), PYTHON, pythonSource));
		logger.trace("Recognized new names: {}", type);
	}

	public List<IType> getTypes() {
		return type;
	}
}