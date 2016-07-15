package pt.uminho.ceb.biosystems.mew.core.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.tree.generictree.GenericTree;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.tree.generictree.TreeNode;

/**
 * The <code>SmartProperties</code> class. Alternative to regular properties
 * with variable assignment capabilities.Moreover, it adds order capability to
 * the properties map. The properties are read and written in the order defined
 * in the file.
 * 
 * @see java.util.Properties
 * 
 * @author pmaia
 * @date Oct 25, 2013
 * @version 1.2
 * @since Metabolic3
 */
public class SmartProperties extends IndexedHashMap<String, String> {
	
	private static final long									serialVersionUID		= -1735087685445992505L;
	
	/**
	 * Variable declaration pattern
	 */
	private static final Pattern								VAR_DEC_PATT			= Pattern.compile("^\\$(.+?)");
	
	/**
	 * Prefix for variable assignments
	 */
	private static final String									VAR_ASSIGN_PREFIX		= "\\$\\{";
	
	/**
	 * Suffix for variable assignments
	 */
	private static final String									VAR_ASSIGN_SUFFIX		= "\\}(%)?";
	
	/**
	 * Variable assignment pattern
	 */
	private static final Pattern								VAR_ASS_PATT			= Pattern.compile(VAR_ASSIGN_PREFIX + "(.+?)" + VAR_ASSIGN_SUFFIX);
		
	/**
	 * Special tag for combination of properties
	 */
	public static final String									SPECIAL_COMBINE			= "@COMBINE";
	
	/**
	 * Special tag for hierarchically dependent properties
	 */
	public static final Pattern									SPECIAL_DEPENDS			= Pattern.compile("^@DEPENDS.*?");
	
	/**
	 * Alias assignment pattern
	 */
	public static final Pattern									ALIAS_PATTERN			= Pattern.compile("^~(.+?)");
	
	/**
	 * Internal method call pattern (only null argument methods for now)
	 */
	public static final Pattern									INTERNAL_METHOD_PATTERN	= Pattern.compile("@FUNC\\[(.+?)\\]");
	
	/**
	 * Combination multiplexer symbol
	 */
	public static final String									COMBINATION_DELIMITER	= "\\*";
	
	/**
	 * Multiple variable values delimiter
	 */
	public static final String									MULTI_VAR_DELIMITER		= "\\|";
	
	/**
	 * Combinations string if defined
	 */
	private String												combinationsString		= null;
	
	/**
	 * List of possible variable map states (when top level combinations are
	 * assigned)
	 */
	public List<HashMap<String, String>>						possibleVarMapStates	= null;
	
	/**
	 * Mapping of possible states to their paths
	 */
	public Map<Integer, List<TreeNode<Pair<String, String>>>>	possibleStatesPaths		= null;
	
	/**
	 * Singleton variable map
	 */
	private Map<String, String>									variableMap				= null;
	
	/**
	 * Aliases mappings
	 */
	public Map<String, String>									aliasesMap				= null;
	
	/**
	 * Structured tree of variable combinations
	 */
	public GenericTree<Pair<String, String>>					variableTree			= null;
	
	/**
	 * The currently assigned state for the variable mappings
	 */
	public int													currentState			= 0;
	
	/**
	 * Debug flag
	 */
	private boolean												_debug					= false;
	
	/**
	 * The loaded properties file that generated this
	 * <code>SmartProperties</code> instance
	 */
	private String												_propertiesFile			= null;
	
	public SmartProperties() {
		super();
	}
	
	public SmartProperties(String properties) {
		super();
		_propertiesFile = properties;
		try {
			this.load(new FileReader(properties));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void load(InputStream stream) throws IOException {
		load0(new LineReader(stream));
		try {
			processProperties();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void load(Reader reader) throws IOException {
		load0(new LineReader(reader));
		try {
			processProperties();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processProperties() throws Exception {
		loadDependencies();
		loadAliases();
		loadSpecialVariables();
		loadSystemEnvironmentVariables();
		loadVariables();
		generateCombinationsTree();
	}
	
	public String matchVariable(String varAssignString) throws Exception {
		final Matcher m = VAR_ASS_PATT.matcher(varAssignString);
		if (m.matches()) {
			return m.group(1);
		} else
			throw new Exception("Can't map [" + varAssignString + "] to any available variables.");
	}
	
	private void loadDependencies() throws Exception {
		for (String dep : stringPropertyNames()) {
			Matcher m = SPECIAL_DEPENDS.matcher(dep);
			if (m.matches()) {
				String file = getProperty(dep);
				remove(dep);
				SmartProperties dependencies = new SmartProperties(file);
				putAll(dependencies);
				getVariableMap().putAll(dependencies.getVariableMap());
			}
		}
	}
	
	private void loadAliases() throws Exception {
		for (String dep : stringPropertyNames()) {
			Matcher m = ALIAS_PATTERN.matcher(dep);
			if (m.matches()) {
//				String value = getProperty(dep);
				String value = super.get(dep);
				remove(dep);
				String alias = m.group(1).trim();
				getAliasesMap().put(alias, value);
			}
		}
	}
	
	private void loadSpecialVariables() {
		if (_propertiesFile != null && !_propertiesFile.isEmpty()) {
			File pFile = new File(_propertiesFile);
			if (pFile.exists()) {
				getVariableMap().put("THIS", pFile.getAbsolutePath());
				getVariableMap().put("CDIR", pFile.getParent());

			} else
				System.out.println("[" + getClass().getSimpleName() + "]: properties file ["+_propertiesFile+"] does not exist");
			
		} else 
			System.out.println("[" + getClass().getSimpleName() + "]: properties file is null or empty.");
		
		
	}
	
	private void loadSystemEnvironmentVariables() {
		
		Map<String, String> env = System.getenv();
		
		for (String var : env.keySet()) {
			String value = System.getenv(var);
			if (value != null && !value.isEmpty()){
//				System.out.println("[" + getClass().getSimpleName() + "]: variable [" + var + "] = ["+value+"]");
				getVariableMap().put(var, value);
			}
			else
				System.out.println("[" + getClass().getSimpleName() + "]: warning, variable [" + var + "] is undefined... ignoring...");
		}
	}
	
	private void loadVariables() throws Exception {
		
		for (String key : stringPropertyNames()) {
			final Matcher m = VAR_DEC_PATT.matcher(key);
			if (m.matches()) {
				String var = m.group(1);
				if (getVariableMap().containsKey(key))
					throw new Exception("Variable [" + key + "] already declared. Invalid SmartProperties file.");
				else {
					getVariableMap().put(var, getProperty((key)));
					remove(key);
					if (_debug) {
						System.out.println("Added variable [" + var + "]=" + getVariableMap().get(var));
					}
				}
			}
		}
	}
	
	public synchronized String setProperty(String key, String value, int state) {
		return put(key, value);
	}
	
	public String getProperty(String key, int state, boolean replaceAliases) {
		Object oval = super.get(key);
		String sval = (oval instanceof String) ? (String) oval : null;
		if (sval != null && !key.equals(SPECIAL_COMBINE) && !key.equals(SPECIAL_DEPENDS)) {
			try {
				sval = replaceVariableMatches(sval, state);
				if (replaceAliases) sval = replaceAliasesMatches(sval);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return sval;
	}
	
	public String getProperty(String key, int state) {
		return this.getProperty(key, state, false);
	}
	
	public String getProperty(String key) {
		return this.getProperty(key, currentState);
	}
	
	public synchronized Object setProperty(String key, String value) {
		return put(key, value);
	}
	
	private String replaceVariableMatches(String value, int state) throws Exception {
		final Matcher m = VAR_ASS_PATT.matcher(value);
		while (m.find()) {
			String var = m.group(1);
			boolean repAlias = !(m.group(2)!=null);
			String replacement = null;
			if (possibleVarMapStates!=null && possibleVarMapStates.get(state).containsKey(var))
				replacement = possibleVarMapStates.get(state).get(var);
			else
				replacement = variableMap.get(var);
			
			if(repAlias)
				replacement = replaceAliasesMatchesStatewise(replacement,state);
			
			if (replacement == null) throw new Exception("Attempted to replace variable assignment [" + var + "] but the variable isn't assigned.");
			String varRegexp = VAR_ASSIGN_PREFIX + var + VAR_ASSIGN_SUFFIX;
			value = value.replaceAll(varRegexp, replacement);
		}
		return value;
	}
	
	public String replaceAliasesMatches(String value) {
		
		if (aliasesMap != null && !aliasesMap.isEmpty() && aliasesMap.containsKey(value)) value = aliasesMap.get(value);
		return value;
	}
	
	public String replaceAliasesMatchesStatewise(String value, int state) throws Exception{
		if (aliasesMap != null && !aliasesMap.isEmpty() && aliasesMap.containsKey(value)){
			value = aliasesMap.get(value);
			
			value = replaceVariableMatches(value, state);
		}
		
		return value;
	}
	
	public GenericTree<Pair<String, String>> buildCombinationTreeFor(String combinationsString) throws Exception {
		
		if (combinationsString != null) {
			GenericTree<Pair<String, String>> variableTree = new GenericTree<Pair<String, String>>();
			String[] vars = combinationsString.split(COMBINATION_DELIMITER);
			ArrayList<String> varsList = new ArrayList<String>(vars.length);
			for (int i = 0; i < vars.length; i++) {
				String varAssign = vars[i].trim();
				String variable = matchVariable(varAssign);
				varsList.add(i, variable);
			}
			
			String currentVar = varsList.get(0);
			varsList.remove(0);
			String[] options = getVariableMap().get(currentVar).split(MULTI_VAR_DELIMITER);
			TreeNode<Pair<String, String>> root = new TreeNode<Pair<String, String>>();
			for (int i = 0; i < options.length; i++) {
				String opt = options[i].trim();
				Pair<String, String> pair = new Pair<String, String>(currentVar, opt);
				root.addChildNode(getTreeNode(root, pair, varsList));
			}
			variableTree.setRootNode(root);
			return variableTree;
		} else
			return null;
		
	}
	
	public GenericTree<Pair<String, String>> buildCombinationsTreeUntilVariable(String cutVariable) throws Exception {
		if (combinationsString != null) {
			GenericTree<Pair<String, String>> variableTree = new GenericTree<Pair<String, String>>();
			String[] vars = combinationsString.split(COMBINATION_DELIMITER);
			ArrayList<String> varsList = new ArrayList<String>(vars.length);
			for (int i = 0; i < vars.length; i++) {
				String varAssign = vars[i].trim();
				String variable = matchVariable(varAssign);
				varsList.add(i, variable);
				if (variable.equals(cutVariable)) break;
			}
			
			String currentVar = varsList.get(0);
			varsList.remove(0);
			String[] options = getVariableMap().get(currentVar).split(MULTI_VAR_DELIMITER);
			TreeNode<Pair<String, String>> root = new TreeNode<Pair<String, String>>();
			for (int i = 0; i < options.length; i++) {
				String opt = options[i].trim();
				Pair<String, String> pair = new Pair<String, String>(currentVar, opt);
				root.addChildNode(getTreeNode(root, pair, varsList));
			}
			variableTree.setRootNode(root);
			return variableTree;
		} else
			return null;
	}
	
	private void generateCombinationsTree() throws Exception {
		
		combinationsString = getProperty(SPECIAL_COMBINE);
		remove(SPECIAL_COMBINE);
		
		GenericTree<Pair<String, String>> variableTree = buildCombinationTreeFor(combinationsString);
		
		if (variableTree != null && !variableTree.isEmpty()) {
			this.variableTree = variableTree;
			populateVarMapStates();
		}
	}
	
	private void populateVarMapStates() {
		if (variableTree != null) {
			ArrayList<ArrayList<TreeNode<Pair<String, String>>>> validPaths = variableTree.getPathsFromRootToAnyLeaf();
			possibleVarMapStates = new ArrayList<HashMap<String, String>>(validPaths.size());
			possibleStatesPaths = new IndexedHashMap<Integer, List<TreeNode<Pair<String, String>>>>();
			
			for (int i = 0; i < validPaths.size(); i++) {
				List<TreeNode<Pair<String, String>>> path = validPaths.get(i);
				HashMap<String, String> map = new HashMap<String, String>();
				for (TreeNode<Pair<String, String>> pair : path) {
					if (pair.getElement() != null) map.put(pair.getElement().getA(), pair.getElement().getB());
				}
				possibleVarMapStates.add(i, map);
				possibleStatesPaths.put(i, path);
			}
		}
	}
	
	private TreeNode<Pair<String, String>> getTreeNode(TreeNode<Pair<String, String>> parentNode, Pair<String, String> pair, ArrayList<String> varsList) throws Exception {
		TreeNode<Pair<String, String>> node = new TreeNode<Pair<String, String>>();
		node.setParent(parentNode);
		node.setElement(pair);
		if (varsList.size() > 0) {
			String currentVar = varsList.get(0);
			ArrayList<String> subList = new ArrayList<String>(varsList);
			subList.remove(0);
			String[] options = getVariableMap().get(currentVar).split(MULTI_VAR_DELIMITER);
			for (int i = 0; i < options.length; i++) {
				String opt = options[i].trim();
				Pair<String, String> childPair = new Pair<String, String>(currentVar, opt);
				node.addChildNode(getTreeNode(node, childPair, subList));
			}
			
		}
		return node;
	}
	
	public Map<String, String> getVariableMap() {
		if (variableMap == null) variableMap = new HashMap<String, String>();
		return variableMap;
	}
	
	public Map<String, String> getAliasesMap() {
		if (aliasesMap == null) aliasesMap = new HashMap<String, String>();
		return aliasesMap;
	}
	
	/**
	 * @return the _debug
	 */
	public boolean isDebug() {
		return _debug;
	}
	
	/**
	 * @param _debug
	 *            the _debug to set
	 */
	public void setDebug(boolean _debug) {
		this._debug = _debug;
	}
	
	public int getNumberOfStates() {
		return possibleVarMapStates.size();
	}
	
	public List<HashMap<String, String>> getPossibleStates() {
		return possibleVarMapStates;
	}
	
	public Map<Integer, List<TreeNode<Pair<String, String>>>> getPossibleStatesPaths() {
		return possibleStatesPaths;
	}
	
	/**
	 * @return the variableTree
	 */
	public GenericTree<Pair<String, String>> getVariableTree() {
		return variableTree;
	}
	
	/**
	 * @param variableTree
	 *            the variableTree to set
	 */
	public void setVariableTree(GenericTree<Pair<String, String>> variableTree) {
		this.variableTree = variableTree;
	}
	
	/**
	 * @return the combinationsString
	 */
	public String getCombinationsString() {
		return combinationsString;
	}
	
	/********************************************************
	 ******************************************************** 
	 ** 
	 ** 
	 ** 
	 ** All code bellow copied from default java Properties
	 ** 
	 * @see java.util.Properties
	 ** 
	 ** 
	 ** 
	 ******************************************************** 
	 ********************************************************/
	
	private void load0(LineReader lr) throws IOException {
		char[] convtBuf = new char[1024];
		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;
		
		while ((limit = lr.readLine()) >= 0) {
			c = 0;
			keyLen = 0;
			valueStart = limit;
			hasSep = false;
			
			// System.out.println("line=<" + new String(lineBuf, 0, limit) +
			// ">");
			precedingBackslash = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				// need check if escaped.
				if ((c == '=' || c == ':') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				}
				if (c == '\\') {
					precedingBackslash = !precedingBackslash;
				} else {
					precedingBackslash = false;
				}
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != ' ' && c != '\t' && c != '\f') {
					if (!hasSep && (c == '=' || c == ':')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
			put(key, value);
		}
	}
	
	/*
	 * Read in a "logical line" from an InputStream/Reader, skip all comment and
	 * blank lines and filter out those leading whitespace characters ( , and )
	 * from the beginning of a "natural line". Method returns the char length of
	 * the "logical line" and stores the line in "lineBuf".
	 */
	class LineReader {
		public LineReader(InputStream inStream) {
			this.inStream = inStream;
			inByteBuf = new byte[8192];
		}
		
		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[8192];
		}
		
		byte[]		inByteBuf;
		char[]		inCharBuf;
		char[]		lineBuf	= new char[1024];
		int			inLimit	= 0;
		int			inOff	= 0;
		InputStream	inStream;
		Reader		reader;
		
		int readLine() throws IOException {
			int len = 0;
			char c = 0;
			
			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;
			
			while (true) {
				if (inOff >= inLimit) {
					inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						return len;
					}
				}
				if (inStream != null) {
					// The line below is equivalent to calling a
					// ISO8859-1 decoder.
					c = (char) (0xff & inByteBuf[inOff++]);
				} else {
					c = inCharBuf[inOff++];
				}
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}
				
				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					// flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}
	
	/*
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved
	 * chars to their original forms
	 */
	private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;
		
		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
							case '0':
							case '1':
							case '2':
							case '3':
							case '4':
							case '5':
							case '6':
							case '7':
							case '8':
							case '9':
								value = (value << 4) + aChar - '0';
								break;
							case 'a':
							case 'b':
							case 'c':
							case 'd':
							case 'e':
							case 'f':
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A':
							case 'B':
							case 'C':
							case 'D':
							case 'E':
							case 'F':
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default:
								throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f') aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = aChar;
			}
		}
		return new String(out, 0, outLen);
	}
	
	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes special characters
	 * with a preceding slash
	 */
	private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);
		
		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
				case ' ':
					if (x == 0 || escapeSpace) outBuffer.append('\\');
					outBuffer.append(' ');
					break;
				case '\t':
					outBuffer.append('\\');
					outBuffer.append('t');
					break;
				case '\n':
					outBuffer.append('\\');
					outBuffer.append('n');
					break;
				case '\r':
					outBuffer.append('\\');
					outBuffer.append('r');
					break;
				case '\f':
					outBuffer.append('\\');
					outBuffer.append('f');
					break;
				case '=': // Fall through
				case ':': // Fall through
				case '#': // Fall through
				case '!':
					outBuffer.append('\\');
					outBuffer.append(aChar);
					break;
				default:
					if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
						outBuffer.append('\\');
						outBuffer.append('u');
						outBuffer.append(toHex((aChar >> 12) & 0xF));
						outBuffer.append(toHex((aChar >> 8) & 0xF));
						outBuffer.append(toHex((aChar >> 4) & 0xF));
						outBuffer.append(toHex(aChar & 0xF));
					} else {
						outBuffer.append(aChar);
					}
			}
		}
		return outBuffer.toString();
	}
	
	private static void writeComments(BufferedWriter bw, String comments) throws IOException {
		bw.write("#");
		int len = comments.length();
		int current = 0;
		int last = 0;
		char[] uu = new char[6];
		uu[0] = '\\';
		uu[1] = 'u';
		while (current < len) {
			char c = comments.charAt(current);
			if (c > '\u00ff' || c == '\n' || c == '\r') {
				if (last != current) bw.write(comments.substring(last, current));
				if (c > '\u00ff') {
					uu[2] = toHex((c >> 12) & 0xf);
					uu[3] = toHex((c >> 8) & 0xf);
					uu[4] = toHex((c >> 4) & 0xf);
					uu[5] = toHex(c & 0xf);
					bw.write(new String(uu));
				} else {
					bw.newLine();
					if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
						current++;
					}
					if (current == len - 1 || (comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!')) bw.write("#");
				}
				last = current + 1;
			}
			current++;
		}
		if (last != current) bw.write(comments.substring(last, current));
		bw.newLine();
	}
	
	public void store(Writer writer, String comments, int state) throws Exception {
		store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer), comments, false, true, state);
	}
	
	/**
	 * @see Properties#store(Writer, String)
	 * @param writer
	 * @param comments
	 * @throws Exception
	 */
	public void store(Writer writer, String comments) throws Exception {
		store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer), comments, false, false, 0);
	}
	
	public void store(OutputStream out, String comments, int state) throws Exception {
		store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")), comments, true, true, state);
	}
	
	/**
	 * @see Properties#store(OutputStream, String)
	 * @param out
	 * @param comments
	 * @throws Exception
	 */
	public void store(OutputStream out, String comments) throws Exception {
		store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")), comments, true, false, 0);
	}
	
	private void store0(BufferedWriter bw, String comments, boolean escUnicode, boolean replace, int state) throws Exception {
		if (comments != null) {
			writeComments(bw, comments);
		}
		bw.write("#" + new Date().toString());
		bw.newLine();
		synchronized (this) {
			for (Iterator<String> e = keySet().iterator(); e.hasNext();) {
				String key = e.next();
				String val = get(key);
				if (replace) {
					val = replaceVariableMatches(val, state);
				}
				
				key = saveConvert(key, true, escUnicode);
				/*
				 * No need to escape embedded and trailing spaces for value,
				 * hence pass false to flag.
				 */
				val = saveConvert(val, false, escUnicode);
				bw.write(key + "=" + val);
				bw.newLine();
			}
		}
		bw.flush();
	}
	
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}
	
	/** A table of hex digits */
	private static final char[]	hexDigit	= { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	public Set<String> stringPropertyNames() {
		IndexedHashMap<String, String> h = new IndexedHashMap<String, String>();
		iterateProperties(h);
		return h.keySet();
	}
	
	private synchronized void iterateProperties(IndexedHashMap<String, String> h) {
		for (Iterator<String> e = keySet().iterator(); e.hasNext();) {
			String k = e.next();
			String v = get(k);
			h.put((String) k, (String) v);
		}
	}
	
	
	/**
	 * @return the currentState
	 */
	public int getCurrentState() {
		return currentState;
	}
	
	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	
//	@Test
	public void testVarAssPattern() throws Exception {
		String test = "${LALA}%";
		
		Matcher m = VAR_ASS_PATT.matcher(test);
		
		if (m.matches()) {
			System.out.println("match");
		} else {
			System.out.println("no match");
		}
		
	}
	
//	@Test
//	public static void main(String... args) throws Exception {
//		String test = "@FUNC[getContainer]";
//		
//		Matcher m = INTERNAL_METHOD_PATTERN.matcher(test);
//		
//		if (m.matches()) {
//			String group1 = m.group(1);			
//			System.out.println("match = "+group1);
//		} else {
//			System.out.println("no match");
//		}
//		
//	}
	
}
