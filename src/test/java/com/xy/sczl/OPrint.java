package com.xy.sczl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

public class OPrint {
	
	public String Data = "";   //鏂囨。涓鍑虹殑鍏冩暟鎹�
	public String xmlData = "";  //绋嬪簭澶勭悊鎴� xml 鏍煎紡鏁版嵁
	public String htmlData = "";   //绋嬪簭澶勭悊鎴� html 鏍煎紡鏁版嵁
	public String path;           //杈撳嚭鏂囦欢鐨勮矾寰�
	public String sp;             //绯荤粺鍒嗗壊绾匡紝windows锛宭inux 锛歕  , /
	
	public OPrint(){
		this.sp = File.separator;
	}
	
	public void printHtml(String Data,String path,String fileName){  //灏唄tml鏁版嵁杈撳嚭鍒版枃浠�
		String str= Data.replaceAll("\\r|\\n","</p><p>"); 
		System.out.println(str);
		//System.out.println(htmlData);
		OutputStreamWriter f = null;
		String css1 = "../../../aStyle.css";
		String css2 = "../../../../aStyle.css";
		try{
			f = new OutputStreamWriter(new FileOutputStream(path+sp+fileName), "utf-8");
			f.append("<!DOCTYPE html>");
			f.append("<head>");
			f.append("<meta charset='utf8'>");
			f.append("<title>word to html</title>");
			f.append("<link rel='stylesheet' href='"+css1+"'>");
			f.append("<link rel='stylesheet' href='"+css2+"'>"); 
			f.append("</head>");
			f.append("<body>\n<div>");
			f.append(str);
			f.append("</div>\n</body>");
			f.append("</html>");
			f.close();
		}
		catch(Exception e){
			e.getMessage();
		}
	}
	
	
	public void printXml(String xml,String path){ //澶勭悊鏁版嵁骞舵墦鍗版垚XML 鏂囦欢
		String[][] xmlData = dataToXML(xml);
		writeToXML(xmlData,path);
	}
	
	private String[][] dataToXML(String str){  //鎶婃暟鎹浆鎹负 xml 鏍煎紡
		//System.out.println(Data);
		str = str.replaceAll("\u0005", "");
		str = str.replaceAll("\u0007", "");
		String[] data = str.split("(\\s\\d\u002e)+");
		int i=1;
		if(data[0].charAt(0)=='1') i = 0;
		String[][] subject = new String[data.length-i][4];
		int index = 0;
		for(;i<data.length;i++){
			int tab,ch,an;
			tab = data[i].indexOf("\u0003");
			ch = data[i].indexOf("A.");
			an = data[i].indexOf("绛�:");
			
			if(tab!=-1) subject[index][0] = data[i].substring(0,tab); //鍒囧嚭棰樼洰
			else if(ch!=-1) subject[index][0] = data[i].substring(0,ch);
			else if(an!=-1) subject[index][0] = data[i].substring(0,an);
			else subject[index][0] = data[i];
			
			if(tab!=-1 && ch!=-1) subject[index][1] = data[i].substring(tab,ch); //鍒囧嚭琛ㄦ牸
			else if(tab!=-1 && an!=-1) subject[index][1] = data[i].substring(tab,an);
			else if(tab!=-1) subject[index][1] = data[i].substring(tab,data[i].length());
			
			if(ch!=-1 && an!=-1) subject[index][2] = data[i].substring(ch,an); //鍒囧嚭閫夐」
			else if(ch!=-1) subject[index][2] = data[i].substring(ch,data[i].length());
			
			if(an!=-1) subject[index][3] = data[i].substring(an,data[i].length()); //鍒囧嚭鍥炵瓟
			index++;
		}
		return subject;
	}
	
	private void writeToXML(String[][] xml,String path){ //鍐欏叆鏂囦欢
		FileOutputStream f=null;
		PrintStream ps=null;
		try{
			f = new FileOutputStream(path+sp+"XMLData.xml");
			ps = new PrintStream(f);
		}
		catch(Exception e){
			e.getMessage();
		}
		ps.println("<?xml version='1.0' encoding='UTF-8'?>");
		ps.println("<Word>");
		for(int i=0;i<xml.length;i++){
			ps.println("\t<Data>");
				ps.println("\t\t<Subject>");
				ps.println("\t\t\t"+xml[i][0].trim());
				ps.println("\t\t</Subject>");
				
				if(xml[i][1]!=null) ps.println("\t\t\t"+xml[i][1].trim());
				if(xml[i][2]!=null){
					ps.println("\t\t<Choose>");
					ps.println("\t\t\t"+xml[i][2].trim());
					ps.println("\t\t</Choose>");
				}
				if(xml[i][3]!=null){
					ps.println("\t\t<Anser>");
					ps.println("\t\t\t"+xml[i][3].trim());
					ps.println("\t\t</Anser>");
				}
			ps.println("\t</Data>\n\n");
		}
		ps.println("</Word>");
	}
	
}
