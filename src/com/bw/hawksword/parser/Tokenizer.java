package com.bw.hawksword.parser;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class is used to split a string into tokens based on delimitters,
 * remove stopwords and categorize them into different categories.
 * 
 * @author team@booleanworld.com
 */
public class Tokenizer {
	
	private static final String[] DEFAULT_DELIMITERS = new String[] {
		" ", "\\. ", ": ", ",", "!", ";", "\\?", "\\(", "\\)", "\"", "\'", "\\\\", "#", "%",
		"&", "^", "\\*", "[", "]", "\\+", "<", ">", "$", "\\|", "\\t", "\\{", "\\}", "\\n"
	};
	
	/**
	 * This is a set of stopwords. We decided to use {@link HashSet} because
	 * this list is huge and we just want to check if a word belongs to this
	 * set or not and this DS seems to be ideal for this purpose.
	 */
	private final HashSet<String> stopWords;
	private final TokenCategorizer tokenCategorizer;
	
	private final String delimRegex;
	
	public Tokenizer(String stopWordFilePath) throws IOException {
		this(Lists.newArrayList(DEFAULT_DELIMITERS), stopWordFilePath);
	}
	
	public Tokenizer(ArrayList<String> delims, String stopWordFilePath) throws IOException {
		String delimRegex = "";
		for (String delim : delims) {
			delimRegex = delimRegex.concat(delim);
			delimRegex = delimRegex.concat("|");
		}
		this.delimRegex = delimRegex.substring(0, delimRegex.length() - 1);
		this.stopWords = prepareStopWords(stopWordFilePath);
		this.tokenCategorizer = new TokenCategorizer();
	}
	
	/**
	 * Given a rawString, return a list of tokens based on delimiters and excluding stopwords.
	 * @param rawString
	 * @param delims
	 * @return
	 */
	
	@SuppressLint("NewApi")
	public ArrayList<Token> tokenize(String rawString) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		for (String tokenValue : rawString.split(delimRegex)) {
			if ((tokenValue.length() != 0) && !stopWords.contains(tokenValue)) {
				Token token = new Token(tokenValue);
				token.setCategory(tokenCategorizer.categorize(tokenValue));
				tokens.add(token);
			}
		}
		return tokens;
	}
	
	private HashSet<String> prepareStopWords(String filePath) throws IOException {
		HashSet<String> stopWords = new HashSet<String> ();BufferedReader br;
		br = new BufferedReader(new FileReader(filePath));
		for (String word = br.readLine(); word != null; word = br.readLine()) {
			stopWords.add(word);
		}
		br.close();
		return stopWords;
	}
}