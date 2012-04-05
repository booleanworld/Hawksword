package com.bw.hawksword.offlinedict;

import java.io.*;
import java.util.*;

public class RealCode {
	String INFILE = "testOP", TYPESFILE="Types",SEARCHFILE="searchinput";
	String types[] = new String[100];
	HashMap<String, ArrayList<word>> words=new HashMap<String, ArrayList<word>>();
	
	public void buildTypesHash()
	{
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(TYPESFILE));
			String line="",token[];
			while((line=in.readLine()) != null)
			{
				token = line.split(":");
				types[Integer.parseInt(token[1])] = token[0];
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
	public void buildHash()
	{
		BufferedReader in = null;
		ArrayList<word> wordList = null;
		word w;  
		String line="",token[];
		try{
			in = new BufferedReader(new FileReader(INFILE));
			while((line=in.readLine()) != null)
			{
				w =new word();
				token = line.split("\t");
				w.type = Integer.parseInt(token[1]);
				w.def = token[2];
				if( (wordList = words.get(token[0])) == null )
					wordList = new ArrayList<word>();
				wordList.add(w);
				words.put(token[0], wordList);
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
	
	void search(String keyword)
	{
		ArrayList<word> wordList = null;
		word w  =new word();
		if( (wordList = words.get(keyword) ) != null )
		{
			System.out.println("---"+keyword+"---");
			for(int i=0;i<wordList.size();i++)
			{
				w = wordList.get(i);
				System.out.println(i+") ["+types[w.type]+"] "+w.def);
			}
		}
		else
		{
			System.out.println("No Result Found");
		}
	}
	public void dummySearch()
	{
		BufferedReader in = null;
		
		String line="";
		try{
			in = new BufferedReader(new FileReader(SEARCHFILE));
			while((line=in.readLine()) != null)
			{
				search(line.toLowerCase());
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
	
	public static void main(String []args)
	{
		RealCode r = new RealCode();
		r.buildTypesHash();
		r.buildHash();
		r.dummySearch();
	}
}
