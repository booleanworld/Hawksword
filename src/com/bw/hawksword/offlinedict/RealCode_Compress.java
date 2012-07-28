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
	
	static String giveHyperLinks(String input) {
		StringBuffer in = new StringBuffer(input);
		StringBuffer temp = new StringBuffer();
		int i=0, refCount = 0, start=0, end=0;
		String pattern ="";
		while (i < in.length()) {
			pattern = in.substring(i, i+1);
			if (pattern.equalsIgnoreCase("[") ||
					pattern.equalsIgnoreCase("{")) {
				refCount++;
				temp = new StringBuffer();
				start = i+1-refCount;
				i = i+1;
				continue;
			}
			
			if (pattern.equalsIgnoreCase("]") ||
					pattern.equalsIgnoreCase("}")) {
				refCount--;
				end = i+1;
				in.replace(start, end, generateHyperlink(temp.toString(), temp.toString()));
				i = start + generateHyperlink(temp.toString(), temp.toString()).length();
				continue;
			}
			if (refCount > 0)
				temp.append(pattern);
			i = i+1;
		}
		return in.toString();
	}

	static String generateHyperlink(String stringLink, String stringDisplay) {
		String out = "<a href=\"wiktionary://lookup/"+stringLink+"\">"+stringDisplay+"</a>";
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

