package com.bw.hawksword.offlinedict;

import java.io.*;

public class CompressInput {
	String INFILE="wiktionary",OPFILE="primary-index", OPLEVEL1="secondary-index";
	
	public void gen_comp_testOP()
	{
		RandomAccessFile in = null;
		BufferedWriter out = null;
		try{
			in = new RandomAccessFile(INFILE, "r");
			out = new BufferedWriter(new FileWriter(OPFILE));
			String line="",token[];
			long offset=0;
			while((line=in.readLine()) != null)
			{
				token = line.split("\t");
				out.write(token[0]+"#"+Long.toHexString(offset)+"\n" );
				offset  = in.getFilePointer();
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
				out.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public void gen_comp_testOP_l1()
	{
		BufferedReader in = null;
		BufferedWriter out = null;
		try{
			in = new BufferedReader(new FileReader(OPFILE));
			out = new BufferedWriter(new FileWriter(OPLEVEL1));
			String line="",token[];
			int offset=0,count=0;
			while((line=in.readLine()) != null)
			{
				if(count%10 == 0)
				{
					token = line.split("#");
					out.write(token[0]+"#"+Integer.toHexString(offset)+"\n" );
				}
				count++;
				offset+=(line.length()+1);
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
				out.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public static void main(String []arg)
	{
		CompressInput temp = new CompressInput();
		temp.gen_comp_testOP();
		temp.gen_comp_testOP_l1();
	}
}
