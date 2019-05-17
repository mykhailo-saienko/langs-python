// Generated from PythonHeader.g4 by ANTLR 4.6

	package ms.vm.lang.python.gen;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PythonHeaderLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CLASS=1, ID=2, STRING=3, LINE_COMMENT=4, WS=5, EVERYTHING=6;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"CLASS", "ID", "STRING", "STRINGCHARACTER", "ESCAPESEQUENCE", "OCTALESCAPE", 
		"UNICODEESCAPE", "ZEROTOTHREE", "HEXDIGIT", "OCTALDIGIT", "LINE_COMMENT", 
		"WS", "EVERYTHING"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'class'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "CLASS", "ID", "STRING", "LINE_COMMENT", "WS", "EVERYTHING"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public PythonHeaderLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PythonHeader.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\bn\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\7\3&\n\3"+
		"\f\3\16\3)\13\3\3\4\3\4\7\4-\n\4\f\4\16\4\60\13\4\3\4\3\4\3\5\3\5\5\5"+
		"\66\n\5\3\6\3\6\3\6\3\6\5\6<\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\5\7I\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13"+
		"\3\f\3\f\7\fZ\n\f\f\f\16\f]\13\f\3\f\3\f\3\f\3\f\3\r\6\rd\n\r\r\r\16\r"+
		"e\3\r\3\r\3\16\6\16k\n\16\r\16\16\16l\3l\2\17\3\3\5\4\7\5\t\2\13\2\r\2"+
		"\17\2\21\2\23\2\25\2\27\6\31\7\33\b\3\2\13\5\2C\\aac|\6\2\62;C\\aac|\4"+
		"\2$$^^\n\2$$))^^ddhhppttvv\3\2\62\65\5\2\62;CHch\3\2\629\4\2\f\f\17\17"+
		"\5\2\13\f\17\17\"\"p\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\3\35\3\2\2\2\5#\3\2\2\2\7*\3\2\2\2\t\65\3\2"+
		"\2\2\13;\3\2\2\2\rH\3\2\2\2\17J\3\2\2\2\21Q\3\2\2\2\23S\3\2\2\2\25U\3"+
		"\2\2\2\27W\3\2\2\2\31c\3\2\2\2\33j\3\2\2\2\35\36\7e\2\2\36\37\7n\2\2\37"+
		" \7c\2\2 !\7u\2\2!\"\7u\2\2\"\4\3\2\2\2#\'\t\2\2\2$&\t\3\2\2%$\3\2\2\2"+
		"&)\3\2\2\2\'%\3\2\2\2\'(\3\2\2\2(\6\3\2\2\2)\'\3\2\2\2*.\7$\2\2+-\5\t"+
		"\5\2,+\3\2\2\2-\60\3\2\2\2.,\3\2\2\2./\3\2\2\2/\61\3\2\2\2\60.\3\2\2\2"+
		"\61\62\7$\2\2\62\b\3\2\2\2\63\66\n\4\2\2\64\66\5\13\6\2\65\63\3\2\2\2"+
		"\65\64\3\2\2\2\66\n\3\2\2\2\678\7^\2\28<\t\5\2\29<\5\r\7\2:<\5\17\b\2"+
		";\67\3\2\2\2;9\3\2\2\2;:\3\2\2\2<\f\3\2\2\2=>\7^\2\2>I\5\25\13\2?@\7^"+
		"\2\2@A\5\25\13\2AB\5\25\13\2BI\3\2\2\2CD\7^\2\2DE\5\21\t\2EF\5\25\13\2"+
		"FG\5\25\13\2GI\3\2\2\2H=\3\2\2\2H?\3\2\2\2HC\3\2\2\2I\16\3\2\2\2JK\7^"+
		"\2\2KL\7w\2\2LM\5\23\n\2MN\5\23\n\2NO\5\23\n\2OP\5\23\n\2P\20\3\2\2\2"+
		"QR\t\6\2\2R\22\3\2\2\2ST\t\7\2\2T\24\3\2\2\2UV\t\b\2\2V\26\3\2\2\2W[\7"+
		"%\2\2XZ\n\t\2\2YX\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\^\3\2\2\2]["+
		"\3\2\2\2^_\7\f\2\2_`\3\2\2\2`a\b\f\2\2a\30\3\2\2\2bd\t\n\2\2cb\3\2\2\2"+
		"de\3\2\2\2ec\3\2\2\2ef\3\2\2\2fg\3\2\2\2gh\b\r\2\2h\32\3\2\2\2ik\13\2"+
		"\2\2ji\3\2\2\2kl\3\2\2\2lm\3\2\2\2lj\3\2\2\2m\34\3\2\2\2\13\2\'.\65;H"+
		"[el\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}