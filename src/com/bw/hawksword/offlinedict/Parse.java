package com.bw.hawksword.offlinedict;


//import java.io.*;
//import java.util.*;

class word{
	public String def;
	public int type;
}

//public class Parse {
//	TreeMap<String, ArrayList<word>> words_and_type=new TreeMap<String, ArrayList<word>>();
//	HashMap<String, Integer> types=new HashMap<String, Integer>();
//	BufferedReader in;
//	BufferedWriter out,out1;
//	String filename = "dump.tsv", opfile = "wiktionary",typefile="Types";
//	
//	public Parse()
//	{
//		
//	}
//	
//	void replace(StringBuffer target,String sourcePattern, String targetPatern)
//	{
//		int s=0;
//		while( (s=target.indexOf(sourcePattern,s)) != -1)
//		{
//			target.replace(s, s+sourcePattern.length(), targetPatern);
//			s+=targetPatern.length();
//		}
//	}
//	
//	boolean isDirty(String str)
//	{
//		char ch;
//		for(int i=0;i<str.length();i++)
//		{
//			ch = str.charAt(i);
//			if(! ( (ch >= 'A' && ch<='Z') || (ch >= 'a' && ch<='z') || ch =='-' || ch=='_' ) )
//				return true;	
//		}
//		return false;
//	}
//	
//public void readToHash()
//	{
//		String line;
//		try {
//			
//			in = new BufferedReader(new FileReader(filename));
//			out = new BufferedWriter(new FileWriter(opfile));
//			out1 = new BufferedWriter(new FileWriter(typefile));
//			
//			int count =0,s,e,i,typeCount;
//			String token[],tempStr="";
//			StringBuffer actDef=null;
//			word w; 
//			ArrayList<word> tempw = new ArrayList<word>();
//			
//			while( (line =in.readLine()) != null )
//			{
//				w= new word();
//				token = line.split("\t");
//				if(! isDirty(token[1]) ) //if word has "[A-Z][a-z][_-]"
//				{
//					token[1]=token[1].toLowerCase();
//					if(token[3].charAt(0)=='#' && token[3].charAt(1)==' ')   //every definition starts with "# "
//						actDef = new StringBuffer(token[3].substring(2));
//					else													 //some definitions start with "#"
//						actDef = new StringBuffer(token[3].substring(1));
//					
//					/**************** parse {{.*}} pattern **********************/
//					s=0;e=0;
//					while( (s = actDef.indexOf("{{",s)) != -1)
//					{
//						e = actDef.indexOf("}}",s);
//						tempStr="( ";
//						String []temp = (actDef.substring(s+2,e)).split("\\|");
//						for(i=0;i<temp.length;i++)
//						{
//							if(temp[i].indexOf("w:")!= -1 || temp[i].indexOf("=") != -1)
//								continue;
//							tempStr+=(temp[i]+" ");
//						}
//						tempStr+=")";
//						actDef.replace(s, e+2, tempStr);
//						s+= tempStr.length();
//					}
//					/*************************** parse [[]] pattern ***********************/
//					s=0;e=0;
//					while( (s = actDef.indexOf("[[",s)) != -1)
//					{
//						e = actDef.indexOf("]]",s);
//						if(e == -1)
//							break;
//						String temp[] = actDef.substring(s+2,e).split("\\|");
//						
//						actDef.replace(s, e+2, temp[0]);
//						s+= temp[0].length();
//					}
//					
//					/***************** prepare word w **********************/
//					if(!types.containsKey(token[2]))
//					{
//						count++;
//						types.put(token[2], count);
//						typeCount = count;
//					}
//					else
//						typeCount = types.get(token[2]);
//					w.type=typeCount;
//					w.def = actDef.toString();
//					if((tempw = words_and_type.get(token[1]) ) == null)
//						tempw = new ArrayList<word>();
//
//					tempw.add(w);
//					words_and_type.put(token[1], tempw);
//					
//				}	
//			}
//			for (Map.Entry<String, Integer> entry : types.entrySet()) {
//			    out1.write(entry.getKey()+":"+entry.getValue().intValue()+"\n");
//			}
//			for (Map.Entry< String, ArrayList<word> > entry : words_and_type.entrySet()) 
//			{
//				 ListIterator<word> litr = entry.getValue().listIterator();
//				    while (litr.hasNext()) {
//				      w = litr.next();
//				      out.write(entry.getKey()+"\t"+w.type+"\t"+w.def+"\n");
//				    }
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally{
//			try{
//				in.close();
//				out.close();
//				out1.close();
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
//		}
//	}
//	
//
//public static void main (String []args)
//	{
//		Parse p = new Parse();
//		p.readToHash();
//		
//	}
//}
