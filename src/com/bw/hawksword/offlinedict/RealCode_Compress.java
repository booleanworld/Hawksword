package com.bw.hawksword.offlinedict;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class RealCode_Compress {
	String INFILE,
		   TYPESFILE,
		   INFILE_COM,
		   INFILE_COM_L1;
	String types[] = new String[100];
	//might not be a good idea, but just for now i'm using two arrays
	ArrayList<String> wordlist = new ArrayList<String>();
	ArrayList<Integer> offsetlist = new ArrayList<Integer>();
	public RealCode_Compress(String path)
	{
		   INFILE = path+File.separator+"wiktionary";
		   TYPESFILE= path+File.separator+"Types";
		   INFILE_COM= path+File.separator+"primary-index";
		   INFILE_COM_L1= path+File.separator+"secondary-index";
		   buildTypesHash();
		   fill_word_offset_list_lessIO();
	}
	public void buildTypesHash()
	{
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(TYPESFILE));
			String line="";
			int i=0;
			while((line=in.readLine()) != null)
			{
				types[i] = line;
				i++;
			}
		}
		catch(Exception e)
		{
			Log.d("Dictionary", "Exception in Types file");
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void fill_word_offset_list()
	{
		BufferedReader in=null;
		String line;
		int split_pos;
		try{
			in = new BufferedReader(new FileReader(INFILE_COM_L1));
			while((line=in.readLine()) != null)
			{
				split_pos = line.indexOf('#');
				wordlist.add( line.substring(0,split_pos) );
				offsetlist.add( Integer.parseInt(line.substring(split_pos+1),16) );
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in!=null)	
					in.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void fill_word_offset_list_lessIO()
	{
		BufferedReader in=null;
		RandomAccessFile in1 = null;
		String line;
		int num_bytes=50*1024,offset=0,right=0,left=0,read_bytes=0,split_pos=0;
		char buf[] = new char[num_bytes];
		try{
			in1 = new RandomAccessFile(INFILE_COM_L1,"r");
			in1.seek(offset);
			in = new BufferedReader( new FileReader(in1.getFD()) );
			while((read_bytes = in.read(buf,0,num_bytes)) != -1)
			{
				left=right=0;
				for(right=0;right<read_bytes;)
				{
					if(buf[right]=='\n')
					{
						line = new String(buf,left,right-left);
						split_pos=line.indexOf('#');
						wordlist.add( line.substring(0,split_pos) );
						offsetlist.add( Integer.parseInt(line.substring(split_pos+1),16) );
						right++;
						left=right;
						continue;
					}
					right++;
				}
				offset+=left;
				in1.seek(offset);
				in = new BufferedReader( new FileReader(in1.getFD()) );
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in!=null)
					in.close();
				if(in1!=null)
					in1.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	
	public int bsearch(String key)
	{
		//requires that both wordlist and offsetlist are filled and have same size
		int s=0,e=wordlist.size()-1,mid;
		while(s<e)
		{
			mid = (s+e)/2;
			if(wordlist.get(mid).compareToIgnoreCase(key) > 0)
				e=mid-1;
			else if(wordlist.get(mid).compareToIgnoreCase(key) < 0)
				s=mid+1;
			else
				return mid;
		}
		return e;
	}
	
	public ArrayList<word> search_primary_index(int offset, String key)
	{
		RandomAccessFile rin = null,rin1=null;
		BufferedReader in = null,in1 = null;
		ArrayList<word> result = null;
		try {
			
			rin = new RandomAccessFile(INFILE_COM, "r");
			rin.seek(offset);
			in = new BufferedReader(new FileReader(rin.getFD()));
			
			int count = 0,split_pos;
			String line,token[];
			ArrayList<Integer> tempoffset = new ArrayList<Integer>();
			word w;
			
			/************ searching in primary index ***********/
			while(count < 50 && ( line=in.readLine() )!=null)
			{
				split_pos=line.indexOf('#');
				if( line.substring(0,split_pos).compareToIgnoreCase(key) == 0 )
					tempoffset.add(Integer.parseInt(line.substring(split_pos+1), 16));
				count++;
			}
			
			/*********** searching in main file ***********/
			if(tempoffset.size()>0)
			{
				rin1 = new RandomAccessFile(INFILE, "r");
				rin1.seek(tempoffset.get(0));
				in1 = new BufferedReader(new FileReader(rin1.getFD()));
				result = new ArrayList<word>();
				for(int i = 0 ;i<tempoffset.size();i++)
				{
					line = in1.readLine();
					w =new word();
					token = line.split("\t");
					w.type = Integer.parseInt(token[1]);
					w.def = token[2];
					result.add(w);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in1 != null)
					in1.close();
				if(in != null)
					in.close();
				if(rin1 != null)
					rin1.close();
				if(rin != null)
					rin.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public boolean spell_checker(int offset, String key)
	{
		RandomAccessFile rin = null,rin1=null;
		BufferedReader in = null,in1 = null;
		boolean lock = false;
		try {
			
			rin = new RandomAccessFile(INFILE_COM, "r");
			rin.seek(offset);
			in = new BufferedReader(new FileReader(rin.getFD()));
			
			int count = 0,split_pos;
			String line,token[];
			ArrayList<Integer> tempoffset = new ArrayList<Integer>();
			word w;
			
			/************ searching in primary index ***********/
			while(count < 50 && ( line=in.readLine() )!=null)
			{
				split_pos=line.indexOf('#');
				if( line.substring(0,split_pos).compareToIgnoreCase(key) == 0 )
					tempoffset.add(Integer.parseInt(line.substring(split_pos+1), 16));
				count++;
			}
			
			/*********** searching in main file ***********/
			if(tempoffset.size()>0)
			{
				lock = true;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(in1 != null)
					in1.close();
				if(in != null)
					in.close();
				if(rin1 != null)
					rin1.close();
				if(rin != null)
					rin.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return lock;
	}
	/* this function is one-entry api to convert raw string into nice looking and
	 * properly hyperlinked string. Now for that, we may use square braces rules
	 * and/or curly braces rule. Also, the hyperlinking if required at places.
	 * 
	 * so, scan through the raw string and pass on the sub-string enclosed under
	 * square or curly braces to be processed via their respective functions. and
	 * replace the processed string.
	 */
	static String giveHyperLinks(String input) {
		StringBuffer in = new StringBuffer(input);
		String temp = "";
		int i=0;
		int squareStrart =0, squareEnd=0, curlyStart=0, curlyEnd=0;
		boolean curly = false, square = false;
		
		/* this loop will run over whole input string */
		for (i=0; i<in.length(); i++) {
			if (!(square)) {
				if (in.charAt(i) == '[') {
					squareStrart = i;
					square = true;
				}
			} else if (square) {
				if (in.charAt(i) == ']') {
					squareEnd = i + 2; 		// squareend is exclusive + ']' should come twice, so +2
					temp = parseSquareBrackates(in.substring(squareStrart, squareEnd));
					in.replace(squareStrart, squareEnd, temp);
					i = i + temp.length() + 1  - (squareEnd - squareStrart);	//adjust 'i' based on the replacement
					square = false; 			//as curly ended, new curly can start 	
				}
			} 
			
			if (!curly) {
				if (in.charAt(i) == '{') {
					curlyStart = i;
					curly = true;
				}
			} else if (curly) { 			//Now if curly is true, look for only curly ends and not in-between squares
				if (in.charAt(i) == '}') {
					curlyEnd = i + 2; 		// Curlyend is exclusive + '}' should come twice, so +2
					temp = parseCurlyBrackates(in.substring(curlyStart, curlyEnd));
					in.replace(curlyStart, curlyEnd, temp);
					i = i + temp.length() + 1  - (curlyEnd - curlyStart);	//adjust 'i' based on the replacement
					curly = false; 			//as curly ended, new curly can start 
				}
			} 
		}
		
		return in.toString();
	}

	/* Curly braces can have following things
	 * no pipe, hyperlink it
	 * a pipe (|) separated words, both needs to be printed if not same and second should be hyperlinked
	 * a pipe (|) separated words with second word be enclosed under square braces, that need to be hyperlinked
	 * more than one pipes. We will take care of that later as it is becoming too complex and non-generic in nature.
	 */
	static String parseCurlyBrackates(String unparsedString) {
		String parsedString = "";
		unparsedString = unparsedString.substring(2, unparsedString.length() - 2);	//remove surrounded curly braces
		
		String tokens[] = unparsedString.split("\\|");		//now start applying rules

		if (tokens.length == 1) {							//only one word, hyper link it
			parsedString = generateHyperlink(tokens[0], tokens[0]);
		} else if (tokens.length == 2) {
			tokens[0] = tokens[0].trim();
			tokens[1] = tokens[1].trim();
			
			if (tokens[0].equalsIgnoreCase(tokens[1])) { 	//two words, but same
				parsedString = generateHyperlink(tokens[0].toLowerCase(), tokens[1]);
			} else {				// if both words are not same, then print both with hyper link to the second
				if (tokens[0] == "w") 	// 'w' stands for wikipedia, which we will ignore 
					tokens[0] = "";
				else
					tokens[0] = "(" + tokens[0] + ") ";
				parsedString = tokens[0] + generateHyperlink(tokens[1].toLowerCase(), tokens[1]);
			}
		} else {
			for (int i = 0; i < tokens.length; i++)
				parsedString = parsedString + " " + tokens[i];
		}
		return parsedString;
	}
	
	/* Square braces can have following things
	 * just a word, hyperlink it
	 * two words separated with pipe (|), hyperlink second word and display first word, if not same as second word.
	 */
	static String parseSquareBrackates(String unparsedString) {
		String parsedString = "";
		unparsedString = unparsedString.substring(2, unparsedString.length() - 2);	//remove surrounded square braces
		
		String tokens[] = unparsedString.split("\\|");		//now start applying rules
		if(tokens.length == 1) {							//only one word, hyper link it
			parsedString = generateHyperlink(tokens[0].toLowerCase(), tokens[0]);
		} else if(tokens.length == 2) {
			tokens[0] = tokens[0].trim();
			tokens[1] = tokens[1].trim();
			
			if (tokens[0].equalsIgnoreCase(tokens[1])) { 	//two words, but same
				parsedString = generateHyperlink(tokens[0].toLowerCase(), tokens[1]);
			}
			else {
				//send report via google analytics
			}
		} else {
			//send report via google analytics
		}

		return parsedString;
	}
	
	static String generateHyperlink(String stringLink, String stringDisplay) {
		String out;
		
		if (stringLink.contains("<a href="))	//if string is already a link, then return
			return stringLink;
		
		String tokens[] = stringLink.split("\\:");
		if (tokens.length > 1)	//ignore few cases like "wikipedia: xyz"
			stringLink = tokens[tokens.length - 1]; 
		/*presently we are not supporting space separated words to be hyperlinked
		 */
		if(stringLink.contains(" "))
			out = stringLink;
		else
			out = "<a href=\"wiktionary://lookup/"+stringLink+"\">"+stringDisplay+"</a>";
		
		return out;
	}
	
	public String[] search(String keyword) //static
	{
		String[] list = null;
		int index=bsearch(keyword);
		if(index > 0)		//in case the word is also there in previous block
		{
			index--;
			ArrayList<word> result = search_primary_index(offsetlist.get(index),keyword); //can be made a class attribute
			//System.out.println("---"+keyword+"---");
			
			if(result == null){
				list = null;
				System.out.println("No Result Found");
			}
			else{
				list = new String[result.size()];
				for(int i=0;i<result.size();i++){
					//System.out.println((i+1)+") ["+types[result.get(i).type]+"] "+result.get(i).def);
					list[i] = "["+types[result.get(i).type]+"]" + giveHyperLinks(result.get(i).def);
				}
			}
		}
		return list;
	}
	
	public boolean spellSearch(String keyword){
		int index=bsearch(keyword);
		if(index > 0)		//in case the word is also there in previous block
		{
			index--;
			if(spell_checker(offsetlist.get(index),keyword)) //can be made a class attribute
				return true;
		}
		return false;
	}
	/*public void dummySearch()
	{
		BufferedReader in = null;
		
		String line="";
		try{
			in = new BufferedReader(new FileReader(SEARCHFILE));
			
			while((line=in.readLine()) != null)
			{
				long t0 = System.currentTimeMillis();
				search(line.toLowerCase());
				long t1 = System.currentTimeMillis();
				System.out.println(t1-t0);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
					in.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String []args)
	{
		RealCode_Compress r = new RealCode_Compress();
		r.buildTypesHash();
		long t0 = System.currentTimeMillis();
		r.fill_word_offset_list_lessIO();
		long t1 = System.currentTimeMillis();
		System.out.println(t1-t0);
		r.dummySearch();
	}
	*/
}

