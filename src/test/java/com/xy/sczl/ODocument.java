package com.xy.sczl;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;

public class ODocument {
	private HWPFDocument doc;
	private String path;         //鏂囨。璺緞
	private List<Integer> fontSize;  //鏂囨。瀛椾綋
	private List<Integer> color;     //鏂囨。棰滆壊
	
	public ODocument(String path){
		this.doc = null;
		fontSize = new LinkedList<>();
		color = new LinkedList<>();
		this.path = path;
		this.loadDoc();
	}
	
	private void loadDoc(){  //鍔犺浇鏂囨。
		try{
			FileInputStream in=new FileInputStream(new File(this.path));
			this.doc = new HWPFDocument(in); 
			in.close();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public HWPFDocument getDocument(){  // 获取文档
		return this.doc;
	}
	
	public String readDoc(){  // 获取文档的总字节长度，然后遍历每个字节
		String Data = "";
		int length = doc.characterLength();
		String str="";
		char ch;
		int len;
		for (int i = 0; i < length - 1; i++) {
			  Range r = new Range(i,i+1,doc); // 范围
			  CharacterRun cr = r.getCharacterRun(0); // 共同属性的文本
			  str = cr.text();
			  Data = Data+str;
			  len = cr.text().length();
			  color.add(cr.getColor());
			  fontSize.add(cr.getFontSize());
			  while(len-->0) ch = str.charAt(len);
		}
		System.out.println(Data);
		return DataPretreatment(Data);
	}
	
	private String DataPretreatment(String Data){  // 鏁版嵁棰勫鐞嗭紝鏇挎崲鍥剧墖锛岃〃鏍硷紝鍏紡绛変负鐗规畩瀛楃
		Data = Data.replaceAll("(\u0013.{1,30}\u0015)+","\u0002");  //鍏紡鏇挎崲鎴� \u0002
		Data = Data.replaceAll("\\b.+\u0007","@TABLE@");   //琛ㄦ牸鏇挎崲涓� @TABLE@ 鐗规畩鏍囪
		return Data;										//鍥剧墖鐨勬爣璁版槸\u0001 锛堣鍙栧埌鐨勬暟鎹氨鏄畠锛屼笉鐢ㄦ浛鎹紝涓�涓爣璁颁唬琛ㄤ竴寮犲浘锛�
	}
	
	
}
