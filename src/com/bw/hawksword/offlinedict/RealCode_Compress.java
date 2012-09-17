package com.bw.hawksword.offlinedict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bw.hawksword.wiktionary.LookupActivity;

public class RealCode_Compress {
	private static String INFILE,
	TYPESFILE,
	INFILE_COM,
	INFILE_COM_L1;
	static String types[] = new String[100];
	private String path = "mnt/sdcard/Android/data/com.bw.hawksword.ocr/files/mounted/tessdata";
	public static boolean isbuild = false; 
	//might not be a good idea, but just for now i'm using two arrays
	public static ArrayList<String> wordlist = new ArrayList<String>();
	public static ArrayList<Integer> offsetlist = new ArrayList<Integer>();

	public static Pattern regex; 
	public static Matcher matcher;
	public static URL url;
	public static BufferedReader brURL;
	private static String curruntSearched;
	public static String destinationLang;
	
	public RealCode_Compress()
	{
		INFILE = path+File.separator+"wiktionary";
		TYPESFILE= path+File.separator+"Types";
		INFILE_COM= path+File.separator+"primary-index";
		INFILE_COM_L1= path+File.separator+"secondary-index";
		if(!isbuild) {
			buildTypesHash();
			fill_word_offset_list_lessIO();
			isbuild = true;
		}
	}
	static void buildTypesHash()
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
	static void fill_word_offset_list_lessIO()
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

	static int bsearch(String key)
	{
		//requires that both wordlist and offsetlist are filled and have same size
		int s=0,e=wordlist.size()-1,mid;
		while(s<e)
		{
			mid = (s+e)/2;
			if(wordlist.get(mid).compareTo(key) > 0)
				e=mid-1;
			else if(wordlist.get(mid).compareTo(key) < 0)
				s=mid+1;
			else
				return mid;
		}
		return e;
	}
	
	static ArrayList<word> search_primary_index(int offset, String key)
	{
		RandomAccessFile rin = null,rin1=null;
		BufferedReader in = null,in1 = null;
		ArrayList<word> result = null;
		try {
			rin = new RandomAccessFile(INFILE_COM, "r");
			rin.seek(offset);
			in = new BufferedReader(new FileReader(rin.getFD()));
			int count = 0;
			String line,token[];
			ArrayList<Integer> tempoffset = new ArrayList<Integer>();
			word w;
			boolean firstEncounter = false;
			/************ searching in primary index ***********/
			/* Search for first occurrence */
			while(count < 100 && ( line=in.readLine() )!=null)
			{
				String[] word = line.split("#");
				if (word[0].compareToIgnoreCase(key) == 0) {
					tempoffset.add(Integer.parseInt(word[1], 16));
					firstEncounter = true;
				} else if (firstEncounter) {
					break;
				}
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
	
	public static String search(String keyword) //static
	{
		curruntSearched = keyword;
		int index=bsearch(keyword);
		ArrayList<word> result = null;
		if(index > 0)	//in case the word is also there in previous block
		{
			index--;
			result = search_primary_index(offsetlist.get(index),keyword); //can be made a class attribute
			//System.out.println("---"+keyword+"---");
			if(result == null){
				result = search_primary_index(offsetlist.get(index),keyword.toLowerCase()); 
				if(result == null)
					return null;
			}	
		}
		return generateWebText(result);	
	}
	static boolean spell_checker(int offset, String key)
	{
		RandomAccessFile rin = null,rin1=null;
		BufferedReader in = null,in1 = null;
		boolean lock = false;
		try {
			rin = new RandomAccessFile(INFILE_COM, "r");
			rin.seek(offset);
			in = new BufferedReader(new FileReader(rin.getFD()));
			String line;
			/************ searching in primary index ***********/
			while(( line=in.readLine() )!=null)
			{
				String word = line.split("#")[0];
				if (word.compareToIgnoreCase(key) == 0) {
					return true;
				}
				if (word.compareTo(key) > 0) {
					return false;
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
		return lock;
	}
	
	public static boolean spellSearch(String keyword){
		int index=bsearch(keyword);
		if(index > 0)	//in case the word is also there in previous block
		{
			index--;
			if(spell_checker(offsetlist.get(index),keyword)) //can be made a class attribute
				return true;
		}
		return false;
	}
	
	static ArrayList<String> matchingPrefixes(int offset, String key)
	{
		RandomAccessFile rin = null,rin1=null;
		BufferedReader in = null,in1 = null;
		ArrayList<String> result = new ArrayList<String>();
		try {
			rin = new RandomAccessFile(INFILE_COM, "r");
			rin.seek(offset);
			in = new BufferedReader(new FileReader(rin.getFD()));
			int count = 0;
			String line, lastWord="";
			boolean firstEncounter = false;
			/************ searching in primary index ***********/
			/* Search for first occurrence */
			while(count < 30 && ( line=in.readLine() )!=null)
			{
				String[] word = line.split("#");
				if (word[0].startsWith(key)) {
					if (!word[0].contentEquals(lastWord)) {
						lastWord = word[0];
						result.add(word[0]);
					}
					firstEncounter = true;
				} else if (firstEncounter) {
					break;
				}
				count++;
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
	
	public static ArrayList<String> realTimeSearch(String keyword){
		ArrayList<String> matchingWords = new ArrayList<String>();
		int index=bsearch(keyword);
		if(index > 0)	//in case the word is also there in previous block
		{
			index--;
			matchingWords = matchingPrefixes(offsetlist.get(index), keyword);
		}
		return matchingWords;
	}
	
	/* 
	 * HTML tag parser
	 */
	static String removeHTMLTags(String input) {
		StringBuffer in = new StringBuffer(input);
		int i=0;
		int tagStrart =0, tagEnd=0;
		boolean tag = false;

		/* this loop will run over whole input string */
		for (i=0; i<in.length(); i++) {
			if (!(tag)) {
				if (in.charAt(i) == '<') {
					tagStrart = i;
					tag = true;
				}
			} else {
				if (in.charAt(i) == '>') {
					tagEnd = i + 1; 

					if (in.substring(tagStrart, tagEnd).contentEquals("<BR>"))
						in.replace(tagStrart, tagEnd, "\n");
					else
						in.replace(tagStrart, tagEnd, "");
					i = i - (tagEnd - tagStrart);	//adjust 'i' based on the replacement
					tag = false;
				}
			} 
		}
		return in.toString();
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
			} 

			if (!curly) {
				if (in.charAt(i) == '{') {
					curlyStart = i;
					curly = true;
				}
			}

			if (square) {
				if (in.charAt(i) == ']') {
					if (i+1 < in.length()) { //a safe check if raw_string has only one square bracket
						squareEnd = i + 2; 		// squareend is exclusive + ']' should come twice, so +2
						temp = parseSquareBrackates(in.substring(squareStrart, squareEnd));
						in.replace(squareStrart, squareEnd, temp);
						i = i + temp.length() + 1  - (squareEnd - squareStrart);	//adjust 'i' based on the replacement
						square = false; 			//as curly ended, new curly can start 	
					}
				}
			} 

			if (curly) { 			//Now if curly is true, look for only curly ends and not in-between squares
				if (in.charAt(i) == '}') {
					if (i+1 < in.length()) { //a safe check if raw_string has only one square bracket
						curlyEnd = i + 2; 		// Curlyend is exclusive + '}' should come twice, so +2
						temp = parseCurlyBrackates(in.substring(curlyStart, curlyEnd));
						in.replace(curlyStart, curlyEnd, temp);
						i = i + temp.length() + 1  - (curlyEnd - curlyStart);	//adjust 'i' based on the replacement
						curly = false; 			//as curly ended, new curly can start 
					}
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
			parsedString = "("+generateHyperlink(tokens[0], tokens[0])+")";
		} else if (tokens.length == 2) {
			tokens[0] = tokens[0].trim();
			tokens[1] = tokens[1].trim();
			tokens[0] = tokens[0].replace("=", " ");	//for cases like from=ancient Greek
			if (!tokens[1].contains("<a href"))
				tokens[1] = tokens[1].replace("=", " ");	//for cases like from=ancient Greek


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
			for (int i = 0; i < tokens.length; i++) {
				if(i==0)
					tokens[i] = "("+tokens[i].trim()+")";

				else
					tokens[i] = tokens[i].trim();

				if(tokens[i].equalsIgnoreCase("en")) {
					continue;
				}

				tokens[i] = tokens[i].replace("=", " ");	//for cases like from=ancient Greek
				parsedString = parsedString + tokens[i] + " ";
			}
		}
		return parsedString;
	}

	/* Square braces can have following things
	 * just a word, hyperlink it
	 * two words separated with pipe (|), hyperlink second word and display first word, if not same as second word.
	 */
	static String parseSquareBrackates(String unparsedString) {
		String parsedString = "";
		int length;
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
			else if ((length = tokens[0].split("\\:").length) > 1) {	//if there are cases like wikipedia:xyz|Xyz will print Xyz
				parsedString = generateHyperlink(tokens[1], tokens[0].split("\\:")[length -1]);
			}
			else {
				parsedString = generateHyperlink(tokens[1], tokens[1]);
			}
		} else {
			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].trim();

				if(tokens[i].equalsIgnoreCase("en")) {
					continue;
				}

				tokens[i] = tokens[i].replace("=", " ");
				parsedString = parsedString + tokens[i] + " ";
			}
		}

		return parsedString;
	}

	static String generateHyperlink(String stringLink, String stringDisplay) {
		String out;

		if (stringLink.contains("<a href="))	//if string is already a link, then return
			return stringLink;

		String tokens[] = stringLink.split(":");
		if (tokens.length > 1)	//ignore few cases like "wikipedia: xyz"
			stringLink = tokens[tokens.length - 1]; 

		/*presently we are not supporting space separated words to be hyperlinked
		 * and also eliminating few junk
		 */
		if(!stringLink.matches("^[a-zA-Z]+$"))
			out = stringLink;
		else if(stringLink.contains("countable") || stringLink.contains("transitive") ||
				stringLink.contains("figuratively"))
			out = "<a href=\"wiktionary://lookup/"+stringLink+"\" style=\"color:#6666ff; text-decoration:none\">"+stringDisplay+"</a>";
		else
			out = "<a href=\"wiktionary://lookup/"+stringLink+"\" style=\"color:#6666ff; font-style:oblique; font-weight:bold; text-decoration:none\">"+stringDisplay+"</a>";

		return out;
	}

	private static String generateWebText(ArrayList<word> result) {
		String type = "";
		String translatorText = "";
		String webText = "";
		webText = "<html>" +
				"<head>" +
				"</head>" +
				"<body>";

		if(LookupActivity.isNetwork && false) {
			try {
				destinationLang = "hi"; // This will be removed 
				url = new URL("http://www.syslang.com/frengly/controller?action=translateREST&src=en&dest=" +
								destinationLang+"&text="+curruntSearched+"&username=chintanpandya89" +
								"&password=bhargav_8fg");
				URLConnection urlConnection = url.openConnection();
				brURL = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream()));
				String temp="";
				translatorText="";
				while((temp = brURL.readLine()) !=null)
					translatorText += temp;
				brURL.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			regex = Pattern.compile("<translation>(.*?)</translation>", Pattern.DOTALL);
			matcher = regex.matcher(translatorText);
			if (matcher.find()) {
				String DataElements = matcher.group(1);
				webText+=DataElements+"<br>";

			}
		}

		if(result == null) {
			webText += "Offline dictionary couldn't find this word.";
		} else {
			webText +=	"<ol>";
			for (int i = 0; i < result.size(); i++) {
				if (!types[result.get(i).type].equalsIgnoreCase(type)) {
					type = types[result.get(i).type];
					webText += "<h3>" + type + "</h3>" +
							"</ol><ol>";
				}
				webText += "<li> " + giveHyperLinks(result.get(i).def);
			}
			webText +=	"</ol>";
		}
		webText += "</body>" +
				"</html>";
		return webText;
	}
}