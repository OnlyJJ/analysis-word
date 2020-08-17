package com.xy.sczl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;

public class OTable {   //澶勭悊鏂囨。涓〃鏍肩殑闂锛岃鍙栵紝鏇挎崲绛�
	
	private List<String> tableData; //璁板綍姣忎釜table鐨勬暟鎹�
	private HWPFDocument doc;
	
	public OTable(HWPFDocument document){
		tableData  =  new LinkedList<>();
		this.doc = document;
		readTableMsg();
	}
	
	private void readTableMsg(){  //璇诲彇鏂囨。涓墍鏈塼able淇℃伅
		Range range = doc.getRange(); //Range灞�
		TableIterator tab = new TableIterator(range);   
		while(tab.hasNext()){
			Table table = tab.next();
			readTable(table);
		}
	}
	
	public  String readTable(Table tab){ //鑾峰緱鍏蜂綋鏌愪釜table鐨勫唴瀹�
		String res = "";
		for (int i = 0; i < tab.numRows(); i++) {     
            TableRow tr = tab.getRow(i);     
            //杩唬鍒楋紝榛樿浠�0寮�濮�  
            for (int j = 0; j < tr.numCells(); j++) {     
                TableCell td = tr.getCell(j);//鍙栧緱鍗曞厓鏍�  
                //鍙栧緱鍗曞厓鏍肩殑鍐呭  
                for(int k=0;k<td.numParagraphs();k++){ 
                	//System.out.println("k"+td.numParagraphs());
                	Paragraph paragraph =td.getParagraph(k);
                    res =res+ paragraph.text()+"\u0005"+"@TD@"; //\u0007鍒楀垎闅旂  
                }
            }
            res = res+"@TR@"; //琛屽垎闅旂
         }
		res = res.replaceAll("(\u0013.{1,30}\u0015)+","\u0002");
		tableData.add(res);
		return res;
	}
	
	public List<String> getTableData(){  //杩斿洖鏂囨。涓墍鏈塼able鐨勬暟鎹紝涓�涓猼able涓轰竴涓泦鍚堜竴涓厓绱�
		return tableData;
	}
	
	public String replaceXmlTable(String xmlData){    //灏嗘暟鎹腑鐨� table 鏇挎崲涓簒ml鏍煎紡锛氬皢琛ㄦ牸鏍囪鎹㈡垚瀵瑰簲琛ㄦ牸鍐呭锛屽苟鍦ㄨ〃鏍煎墠浣嶅姞鍏ユ爣璁帮紝鏍囪姝ゅ鏄〃鏍�
		String xml = xmlData.replaceAll("@TABLE@", "\u0003@TABLE@");
		Matcher mm = Pattern.compile("@TABLE@").matcher(xml);
		int index= 0;
		while(mm.find() && index<tableData.size()){
			xml = xmlData.replaceFirst("@TABLE@", xmlTable(tableData.get(index++)));
		}
		return xml;
	}
	
	public String replaceHtmlTable(String htmlData){   //灏嗘墍鏈塼able鏍囪杩樺師鎴恡able
		Matcher mm = Pattern.compile("@TABLE@").matcher(htmlData);
		int index= 0;
		while(mm.find() && index<tableData.size()){
			htmlData = htmlData.replaceFirst("@TABLE@", htmlTable(tableData.get(index++)));
		}
		return htmlData;
	}
	
	public String htmlTable(String table){ //灏嗘煇涓猼able鏁版嵁瑙ｆ瀽鎴恏tml鏍煎紡table
		String res = "";
		res = "<table border='1'>";
		String[] tr = table.split("@TR@"); //table鎸夎鎷嗗垎瀛樺偍;
		for(int j=0;j<tr.length;j++){
			String[] td = tr[j].split("@TD@");
			res = res+"<tr>";
			for(int k=0;k<td.length;k++){
				res = res+"<td>";
				res = res+td[k];
				res = res+"</td>";
			}
			res = res+"</tr>";
		}
		res = res+"</table>";
		return res;
}

	public String xmlTable(String table){ //灏嗘煇涓猼able鏁版嵁瑙ｆ瀽鎴恱ml鏍煎紡table
		String res = "";
		res = "\t\t<table border='1'>\n";
		String[] tr = table.split("@TR@"); //table鎸夎鎷嗗垎瀛樺偍;
		for(int j=0;j<tr.length;j++){
			String[] td = tr[j].split("@TD@");
			res = res+"\t\t\t<tr>\n";
			for(int k=0;k<td.length;k++){
				res = res+"\t\t\t\t<td>";
				res = res+td[k];
				res = res+" </td>\n";
			}
			res = res+"\t\t\t</tr>\n";
		}
		res = res+"\t\t</table>\n";
		return res;
	}
	
}
