package com.bw.hawksword.parser;



/**
 * This is a Data class for a word along with its properties.
 * @author adyarshyam@gmail.com
 *
 */
public class Token {
	/** The actual value as parsed by the parser */
	private String value;
	
	/** {@link TokenCategory} of the token */
	private TokenCategory category;
	
	/** Relevance Score for the word, to be used while sorting */
	private int score;
	
	/** If the {@link value} parsed was wrong, closest dictionary word */
	private String correctWord;

	public Token(String value) {
		this(value, 1);
	}
	
	public Token(String value, int score) {
		this.value = value;
		this.score = score;
	}

	public TokenCategory getCategory() {
		return category;
	}

	public void setCategory(TokenCategory category) {
		this.category = category;
	}
	public String getValue()
	{
		return value;
	}
	@Override
	public String toString() {
		/*String stringVal = "\n";
		stringVal = stringVal.concat("Value = " + value);
		if (category != null) {
			stringVal = stringVal.concat("\t Category = " + category.name());
		}
		stringVal = stringVal.concat("\t Score = " + score);
		if (correctWord != null) {
			stringVal = stringVal.concat("\t Corrected Word = " + correctWord);
		}
		return stringVal;*/
		return value;
	}

}
