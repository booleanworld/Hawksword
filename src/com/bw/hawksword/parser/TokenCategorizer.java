package com.bw.hawksword.parser;


import java.util.HashMap;
import java.util.Map;

/**
 * This class is used for classifying tokens to URL/EMAIL, etc.
 * 
 * @author adyarshyam@gmail.com
 */
public class TokenCategorizer {
	private static final String WORD_REGEXP = "^[a-zA-Z]+";
	private static final String URL_REGEXP =
			"(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
	private static final String EMAIL_REGEXP =
			"[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
	private static final String MOBILE_REGEXP = 
			"^((\\+){0,1}91(\\s){0,1}(\\-){0,1}(\\s){0,1}){0,1}9[0-9](\\s){0,1}(\\-){0,1}(\\s){0,1}[1-9]{1}[0-9]{7}$";
	
	private Map<String, TokenCategory> regexpTokenTypeMapper;
	
	public TokenCategorizer() {
		this.regexpTokenTypeMapper = new HashMap<String, TokenCategory>();
		regexpTokenTypeMapper.put(WORD_REGEXP, TokenCategory.SIMPLE_WORD);
		regexpTokenTypeMapper.put(URL_REGEXP, TokenCategory.URL);
		regexpTokenTypeMapper.put(EMAIL_REGEXP, TokenCategory.EMAIL);
		regexpTokenTypeMapper.put(MOBILE_REGEXP, TokenCategory.MOBILE);
	}
	
	/**
	 * Given a token, classifies it into one of {@link TokenTypes}.
	 * 
	 * @param token which needs to be classified.
	 * @return The TokenType enum value of token.
	 */
	public TokenCategory categorize(String token) {
		for (String regexp : regexpTokenTypeMapper.keySet()) {
			if(token.matches(regexp)) {
				return regexpTokenTypeMapper.get(regexp);
			}
		}
		return TokenCategory.UNKNOWN;
	}
}
