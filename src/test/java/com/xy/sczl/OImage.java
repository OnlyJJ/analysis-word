package com.xy.sczl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Picture;
import org.w3c.dom.Document;

//import net.arnx.wmf2svg.gdi.svg.SvgGdi;
//import net.arnx.wmf2svg.gdi.wmf.WmfParser;

public class OImage {

	public List<String> ImgPath; //椤哄簭璁板綍鏂囨。涓墍鏈夊浘鐗囩殑璺緞
	public List<String> ImgSize;  //鍥剧墖澶у皬
	public List<String> wmfPath;  //鍏紡鍥剧墖璺緞
	public List<String> wmfSize;  //鍏紡鍥剧墖澶у皬
	private HWPFDocument doc;
	public String ProjectPath;
	public String sp;
	
	public OImage(HWPFDocument document,String path){
		this.ProjectPath = path;
		this.sp = File.separator;
		this.doc = document;
		ImgPath  =  new LinkedList<>();
		ImgSize  =  new LinkedList<>();
		wmfPath = new LinkedList<>();
		wmfSize  =  new LinkedList<>();
		this.readImg();
	}
	
	private void readImg(){ //閬嶅巻鏂囨。锛岃鍙栨枃妗ｇ殑鍥剧墖淇℃伅
		int id = 0;
		String name = "";
		PicturesTable pTable = doc.getPicturesTable();
		List<Picture> pic = pTable.getAllPictures();
		//System.out.println("鍏辨湁锛�"+pic.size());
		for(Picture img : pic) {
			name = "articleImg"+id;
			String afileName=img.suggestFullFileName();
			String suffix = afileName.substring(afileName.lastIndexOf(".") + 1);//后缀名
		    try{
		    	OutputStream out=new FileOutputStream(new File(ProjectPath+sp+name+"."+suffix));
			    img.writeImageContent(out);
			    out.close();
		    }
		    catch(Exception e){
		    	e.getMessage();
		    }
		    if(suffix.equals("wmf")) convert(ProjectPath+sp+name+"."+suffix,ProjectPath+sp+name+".svg");
		    if(suffix.equals("wmf")){
		    	wmfPath.add(name+".svg"); 
		    	wmfSize.add(img.getWidth()+"@"+img.getHeight());
		    }
		    else {
		    	ImgPath.add(name+"."+suffix);
		    	ImgSize.add(img.getWidth()+"@"+img.getHeight());
		    }
		    id++;
		}
	}
	
	public String replaceImg(String data){  //灏嗘暟鎹腑鐨勫浘鐗囨爣璁版浛鎹负鍥剧墖閾炬帴
		String res = "<p>"+data;
		for(String path:ImgPath){
		path = "\n<img class='image' src='"+path+"'>";
		res = res.replaceFirst("\u0001",path);
		}
//		String[] xxx = res.split("(\\s\\d\u002e)+");  //鎶婃瘡閬撻鎷嗗紑鏉ユ浛鎹㈠浘鐗囷紝鐩殑鏄皢棰樼洰涓殑鍥剧墖鏀惧埌棰樺悗鍘�
//		int i=1;
//		if(xxx[0].charAt(0)=='.') i = 0;  //濡傛灉寮�澶村墠涓夊瓧绗︽病鏈�.鍙凤紝閭ｄ箞涓�瀹氭槸鏍囬锛屾晠鍘绘帀
//		int index = 0; //ImaPath鐨勬寚閽�
//		String path = "";
//		res = "";
//		for(;i<xxx.length;i++){
//			xxx[i] = i+xxx[i];
//			Matcher m = Pattern.compile("\u0001").matcher(xxx[i]);
//			xxx[i] = xxx[i].trim();
//			while(m.find()){
//				xxx[i] = xxx[i].replaceFirst("\u0001","");
//				path = "<img class='img' src='"+ImgPath.get(index++)+"'></img>";
//				xxx[i] = xxx[i]+path;
//			}
//			res = res+xxx[i]+"\n\n\n\n\n\n\n";
//		}
		return res;
	}
	
	
	//##################浠ヤ笅鏄皢 .wmf 鏍煎紡鐨勫叕寮忓浘鐗囪浆鎹负 .svg鏍煎紡####################################
	
	public void convert(String file,String dest){ 
		try{
			InputStream in = new FileInputStream(new File(file));
//	        WmfParser parser = new WmfParser();
//	        final SvgGdi gdi = new SvgGdi(false);
//	        parser.parse(in, gdi);
//	        Document doc = gdi.getDocument();
	        OutputStream out = new FileOutputStream(dest);
	        if (dest.endsWith(".svgz")) {
	            out = new GZIPOutputStream(out);
	        }
//	        output(doc, out);
		}
		catch(Exception e){
			System.out.println("edn?????"+e.getMessage());
		}
   }

   public void output(Document doc, OutputStream out) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"-//W3C//DTD SVG 1.0//EN");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(bos));
        out.flush();
        out.close();
}
	
}
