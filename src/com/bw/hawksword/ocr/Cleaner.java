package com.bw.hawksword.ocr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cleaner {
	
	int max, max_index;
	Pattern p;
	Matcher m;
	String[] words;
	int []word_length;
	String recText;
	
	public Cleaner(String RecText)
	{
		p = Pattern.compile("[^a-z_']", Pattern.CASE_INSENSITIVE);
		recText = RecText;
	}
	public boolean initparser()
	{
		words = clean(recText); //inside the string, you can put the recognised word.
		if(words != null){
			word_length  = new int[words.length];
			return true;
		}
		else{
			return false;
		
		}
	}
	public String getBestWord(){ 
		
		if(words != null) {
			for(int i=0; i<words.length; i++) {
				//System.out.println(":"+words[i]+":");
				m = p.matcher(words[i]);
				if(m.find()) { //bad word
					words[i]=null;
					word_length[i] = 0;
					//System.out.println(":"+":)"+word_length[i]);
					continue;
				}
				word_length[i] = words[i].length();
				//System.out.println(":"+words[i]+":)"+word_length[i]); //Recognised word
			}
			
			max_index=0;
			max=word_length[0];
			for(int i= 1; i< word_length.length; i++)
				if(word_length[i]>max) {
					max = word_length[i];
					max_index = i;
				}
			return words[max_index];
			//System.out.println("the final word: "+words[max_index]); // Words to find in dictionary
		}
		else
		{
			return null;
		}
	}
	
	private static String[] clean(String string) {
		int length, temp = 0, spaces = 0;
		int space_index[];
		String []words;
		
		if(string == null || string == "" || string == " ")
			return null;
		
		length = string.length();
		for(int i = 0; i < length; i++) {
			if(string.charAt(i) == ' '||
					string.charAt(i) == ',' || 
					string.charAt(i) == '.' ||
					string.charAt(i) == ';' ||
					string.charAt(i) == ':' ||
					string.charAt(i) == '\n' ||
					string.charAt(i) == '-' ||
					string.charAt(i) == '[' ||
					string.charAt(i) == ']' ||
					string.charAt(i) == '(' ||
					string.charAt(i) == ')' ||
					string.charAt(i) == '/' ||
					string.charAt(i) == '\\' ||
					string.charAt(i) == '$' ||
					string.charAt(i) == '&' ||
					string.charAt(i) == '@' ||
					string.charAt(i) == '!' ||
					string.charAt(i) == '?' ||
					string.charAt(i) == '#' ||
					string.charAt(i) == '\'' ||
					string.charAt(i) == '\t') {
				spaces += 1;
			}
		}
		
		space_index = new int[spaces+2];
		words= new String[spaces+1];
		
		space_index[0] = 0;
		for(int i = 0; i < length; i++) {
			if(string.charAt(i) == ' '||
					string.charAt(i) == ',' || 
					string.charAt(i) == '.' ||
					string.charAt(i) == ';' ||
					string.charAt(i) == ':' ||
					string.charAt(i) == '\n' ||
					string.charAt(i) == '-'  ||
					string.charAt(i) == '[' ||
					string.charAt(i) == ']' ||
					string.charAt(i) == '(' ||
					string.charAt(i) == ')' ||
					string.charAt(i) == '/' ||
					string.charAt(i) == '\\' ||
					string.charAt(i) == '$' ||
					string.charAt(i) == '&' ||
					string.charAt(i) == '@' ||
					string.charAt(i) == '!' ||
					string.charAt(i) == '?' ||
					string.charAt(i) == '#' ||
					string.charAt(i) == '\'' ||
					string.charAt(i) == '\t') {
				space_index[++temp] = i;

			}
		}
		space_index[spaces+1]=string.length();
		
		words[0] = string.substring(space_index[0], space_index[1]);
		for(int i = 1; i < spaces+1; i++) {
			words[i] = string.substring(space_index[i]+1, space_index[i+1]);
		}
		return words;	
	}
}
