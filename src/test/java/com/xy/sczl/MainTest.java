package com.xy.sczl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.ParagraphHeight;
import org.apache.poi.hwpf.usermodel.Bookmark;
import org.apache.poi.hwpf.usermodel.Bookmarks;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;

public class MainTest {
	
	public static void main(String[] args) throws Exception{
		String FilePath = "E:/myworkspace/wordPOI/test1.docx";
		String path =     "E:/myworkspace/wordPOI/result";
		ODocument odoc = new ODocument(FilePath);
		writeHtml(odoc,path);
		//writeXml(odoc,path);
		System.out.println("OK!");
		
//		
//		FileInputStream in=new FileInputStream(new File(FilePath));
//		HWPFDocument doc = new HWPFDocument(in); 
//		System.err.println("------------输出书签--------------begin");
//		printInfo(doc.getBookmarks());
//		System.err.println("------------输出书签--------------结束");
//		
//		
//		System.out.println("------------输出文本--------------begin");
////		System.out.println(doc.getDocumentText());
//		System.out.println("------------输出文本--------------end");
//		
//		
//		Range range = doc.getRange();
//		printInfo(range);
//		
//		readTable(range);
//		
//		doc.write(new FileOutputStream("E:/myworkspace/wordPOI/test5.docx"));
//		
//		closeStream(in);
	}
	
	public static void writeHtml(ODocument doc,String path){
		OTable otable = new OTable(doc.getDocument());
		String htmlData = otable.replaceHtmlTable(doc.readDoc());
		System.out.println(htmlData);
		OImage oimage = new OImage(doc.getDocument(),path);
		htmlData = oimage.replaceImg(htmlData);
		OPrint oprint = new OPrint();
		oprint.printHtml(htmlData, path,"test.html");
		
	}
	public static void writeXml(ODocument doc,String path){
		OTable otable = new OTable(doc.getDocument());
		String xmlData = otable.replaceXmlTable(doc.readDoc());
		OImage oimage = new OImage(doc.getDocument(),path);
		xmlData = oimage.replaceImg(xmlData);
		OPrint oprint = new OPrint();
		oprint.printXml(xmlData, path);
	}
	
	// 测试
	 /**
	  * 输出书签信息
	  * @param bookmarks
	  */
	  private static void printInfo(Bookmarks bookmarks) {
	   int count = bookmarks.getBookmarksCount();
	   System.out.println("书签数量：" + count);
	   Bookmark bookmark;
	   for (int i=0; i<count; i++) {
	     bookmark = bookmarks.getBookmark(i);
	     System.out.println("书签" + (i+1) + "的名称是：" + bookmark.getName());
	     System.out.println("开始位置：" + bookmark.getStart());
	     System.out.println("结束位置：" + bookmark.getEnd());
	   }
	  }
	  
	  /**
	   * 读表格
	   * 每一个回车符代表一个段落，所以对于表格而言，每一个单元格至少包含一个段落，每行结束都是一个段落。
	   * @param range
	   */
	   private static void readTable(Range range) {
	    //遍历range范围内的table。
	    TableIterator tableIter = new TableIterator(range);
	    Table table;
	    TableRow row;
	    TableCell cell;
	    while (tableIter.hasNext()) {
	      table = tableIter.next();
	      int rowNum = table.numRows();
	      for (int j=0; j<rowNum; j++) {
	       row = table.getRow(j);
	       int cellNum = row.numCells();
	       for (int k=0; k<cellNum; k++) {
	         cell = row.getCell(k);
	         //输出单元格的文本
	         System.out.println(cell.text().trim());
	       }
	      }
	    }
	   }
	  
	  /**
	   * 输出Range
	   * @param range
	   */
	   private static void printInfo(Range range) {
	    //获取段落数
	    int paraNum = range.numParagraphs();
	    System.out.println("paraNum:" + paraNum);
	    for (int i=0; i<paraNum; i++) {
//	     this.insertInfo(range.getParagraph(i));
	    	// 起始位置和终止位置
//	    	System.out.println("offset:" +range.getParagraph(i).getStartOffset() + "," + range.getParagraph(i).getEndOffset());
	    	System.out.println("段落" + (i+1) + "：" + range.getParagraph(i).text());
	    	if(range.getParagraph(304).isInTable()) { // 表格
	    		
	    	} else {
	    	}
	      if (i == (paraNum-1)) {
	       insertInfo(range.getParagraph(i));
	      }
	    }
	    int secNum = range.numSections();
	    System.out.println("secNum: " + secNum + ",CharacterRuns:" +range.numCharacterRuns()
	    		+ ",graphs:" + range.numParagraphs() + ","+  range.getParagraph(304).isInTable()
	    );
	    Section section;
	    for (int i=0; i<secNum; i++) {
	      section = range.getSection(i);
	      System.out.println("section" + section.getMarginLeft());
	      System.out.println(section.getMarginRight());
	      System.out.println(section.getMarginTop());
	      System.out.println(section.getMarginBottom());
	      System.out.println(section.getPageHeight());
	      System.out.println(section.text());
	    }
	   }

	   
	   /**
	    * 插入内容到Range，这里只会写到内存中
	    * @param range
	    */
	    private static void insertInfo(Range range) {
	     range.insertAfter("Hello");
	    }
	    
	    private static void closeStream(InputStream is) {
	    	   if (is != null) {
	    	     try {
	    	      is.close();
	    	     } catch (IOException e) {
	    	      e.printStackTrace();
	    	     }
	    	   }
	    	  }
}
