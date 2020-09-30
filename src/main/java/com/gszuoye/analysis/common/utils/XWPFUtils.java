package com.gszuoye.analysis.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMath;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import com.gszuoye.analysis.common.constants.Constants;
import com.microsoft.schemas.vml.CTShape;
/**
 * 获得图片索引工具类
 */
public class XWPFUtils {
	/**
	 * 图片索引替换标签
	 */
	private static final String IMG_IDX_1 = "@idx@";
	private static final String IMG_IDX_2 = "@/idx@";
	/**
	 * 索引标签正则
	 */
	private static final String REGX = "@idx@(.*?)@/idx@"; 
	
	private static final String BASIC_NAME = "DOCX";
	
	private static final String IMG_REG = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
    private static final String IMG_SRC_REG = "src\\s*=\\s*\"?(.*?)(\"|>|\\s+)";
	
	/**
	 * 获取docx图片索引
	 * @param xwpfDocument
	 * @return
	 * @throws IOException
	 */
	public static Map<String , String> readImageInParagraph(XWPFDocument xwpfDocument) throws Exception {
		Map<String , String> imgMap = new HashMap<String, String>();
        List<XWPFParagraph> paragraphList = xwpfDocument.getParagraphs();
        String basePath = FileUtil.genFilePath(BASIC_NAME);
        if(!DateUtil.LICENSE()) {
			throw new NullPointerException();
		}
        File imgPath = new File(basePath);
		if (!imgPath.exists()) { 
			imgPath.mkdirs();
		}
		ImageParse imageParse = new ImageParse(basePath, basePath + "/");
        for(int i = 0;i < paragraphList.size();i++){
        	XWPFParagraph graph = paragraphList.get(i);
        	
        	// 段落非公式图片
        	Map<String , String> imgIdxStyle = readImage(graph);
            for(String pictureId : imgIdxStyle.keySet()){
                XWPFPictureData pictureData = xwpfDocument.getPictureDataByID(pictureId);
                String imageName = pictureData.getFileName();
                // 写入到本地文件
                byte[] bytes = pictureData.getData();
                String absoUrl = basePath +"/" + imageName;
                FileUtil.write(bytes, absoUrl);
                StringBuilder img = new StringBuilder();
                img.append("<img src=").append("\"").append(absoUrl).append("\"")
                	.append(" style=").append("\"").append(imgIdxStyle.get(pictureId)).append("\" />");
                imgMap.put(pictureId, img.toString());
            }
            
            // 公式图片
            parseMath(imageParse, graph);
        }
        return imgMap;
    }
	
	/**
	 * 处理公式图片
	 * @param imageParse
	 * @param paragraph
	 */
	private static void parseMath(ImageParse imageParse, XWPFParagraph paragraph) {
		// 段落无法获取到公式位置，这里是解决定位问题
		List<Integer> typeList = new ArrayList<>();
		XmlCursor xmlcursor = paragraph.getCTP().newCursor();
		while (xmlcursor.hasNextToken()) {
			XmlCursor.TokenType tokenType = xmlcursor.toNextToken();
			if (tokenType.isStart()) {
				if (xmlcursor.getName().getPrefix().equalsIgnoreCase("w")
						&& xmlcursor.getName().getLocalPart().equalsIgnoreCase("r")) {
					typeList.add(1);
				} else if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("oMath")) {
					typeList.add(2);
				}
			} else if (tokenType.isEnd()) {
				xmlcursor.push();
				xmlcursor.toParent();
				if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("p")) {
					break;
				}
				xmlcursor.pop();
			}
		}
		
		if(typeList.size() >0) {
			// 这里解决位置问题
			List<Object> runsOrMathList = new ArrayList<>();
			List<XWPFRun> runs = paragraph.getRuns();
			List<CTOMath> oMathList = paragraph.getCTP().getOMathList();
			Queue<XWPFRun> runsQueue = new LinkedList<>(runs);
			Queue<CTOMath> mathQueue = new LinkedList<>(oMathList);
			for (int i = 0; i < typeList.size(); i++) {
				Integer type = typeList.get(i);
				if (type.equals(1) && runs.size() > 0) {
					runsOrMathList.add(runsQueue.poll());
				} else if (type.equals(2) && mathQueue.size() > 0) {
					runsOrMathList.add(mathQueue.poll());
				}
			}
			// 这里可能存在的问题是，如果段落开头不是run而是公式，则公式会被丢弃
			XWPFRun lastRun = null;
			for (Object child : runsOrMathList) {
				if (child instanceof XWPFRun) {
					lastRun = (XWPFRun) child;
				} else if (child instanceof CTOMath) {
					String url = OmmlUtils.convertOmathToPng((CTOMath) child, imageParse);
                	if(lastRun != null && StringUtils.isNotEmpty(url)) {
                		if(StringUtils.isNotEmpty(lastRun.text()))
                			lastRun.setText(url);
                	}
				}
			}
		}
	}
	
    /**
     *  获取某一个段落中的所有图片索引
     * @param paragraph
     * @return
     */
    private static Map<String, String> readImage(XWPFParagraph paragraph) {
        //图片索引List
    	Map<String, String> imageBundleList = new HashMap<String, String>();
 
        //段落中所有XWPFRun
        List<XWPFRun> runList = paragraph.getRuns();
        XWPFRun lastRun = null;
        for (XWPFRun run : runList) {
        	// 处理第三种格式的图片
        	boolean isPict = true;
            //XWPFRun是POI对xml元素解析后生成的自己的属性，无法通过xml解析，需要先转化成CTR
            CTR ctr = run.getCTR();
 
            //对子元素进行遍历
            XmlCursor c = ctr.newCursor();
            //这个就是拿到所有的子元素：
            c.selectPath("./*");
            while (c.toNextSelection()) {
                XmlObject o = c.getObject();
                //如果子元素是<w:drawing>这样的形式，使用CTDrawing保存图片
                // 注意：
             // 这种模式的图片，poi在解析文档时，可以自动获取，所以可以不用处理
//                if (o instanceof CTDrawing) { 
//                    CTDrawing drawing = (CTDrawing) o;
//                    CTInline[] ctInlines = drawing.getInlineArray();
//                    for (CTInline ctInline : ctInlines) {
//                        CTGraphicalObject graphic = ctInline.getGraphic();
//                        //
//                        XmlCursor cursor = graphic.getGraphicData().newCursor();
//                        cursor.selectPath("./*");
//                        while (cursor.toNextSelection()) {
//                            XmlObject xmlObject = cursor.getObject();
//                            // 如果子元素是<pic:pic>这样的形式
//                            if (xmlObject instanceof CTPicture) {
//                                org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture picture = (org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture) xmlObject;
//                                //拿到元素的属性
//                                String pictureId = picture.getBlipFill().getBlip().getEmbed();
//                                if(StringUtils.isNotEmpty(run.text())) {
//	                                if(lastRun == null) {
//	                                	if (StringUtils.isNotEmpty(run.text())) {
//	    									run.setText(IMG_IDX_1 + pictureId + IMG_IDX_2);
//	    								}
//	                                } else {
//	                                	if(StringUtils.isNotEmpty(lastRun.text())) {
//	    									lastRun.setText(IMG_IDX_1 + pictureId + IMG_IDX_2);
//	    								}
//	                                }
//                                }
//                            }
//                        }
//                    }
//                }
                //使用CTObject保存图片
                //<w:object>形式
                if (o instanceof CTObject) {
                    CTObject object = (CTObject) o;
                    XmlCursor w = object.newCursor();
                    w.selectPath("./*");
                    while (w.toNextSelection()) {
                        XmlObject xmlObject = w.getObject();
                        if (xmlObject instanceof CTShape) {
                            CTShape shape = (CTShape) xmlObject;
                            String style = shape.getStyle();
                            String pictureId = shape.getImagedataArray()[0].getId2();
                            imageBundleList.put(pictureId, style);
							if (lastRun == null) {
								if (StringUtils.isNotEmpty(run.text())) {
									run.setText(IMG_IDX_1 + pictureId + IMG_IDX_2);
								}
							} else {
								if(StringUtils.isNotEmpty(lastRun.text())) {
									lastRun.setText(IMG_IDX_1 + pictureId + IMG_IDX_2);
								}
							}
						}
                    }
                }
            }
            // 处理v:imagedata格式的图片
            // <w:pict>形式，需要通过此种方式来处理
			if (isPict) {
				List<org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture> picts = ctr.getPictList();
				if (picts != null && picts.size() > 0) {
					for (org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture pic : picts) {
						XmlCursor cursor = pic.newCursor();
						cursor.selectPath("./*");
						while (cursor.toNextSelection()) {
							XmlObject xmlObject = cursor.getObject();
							if (xmlObject instanceof CTShape) {
								CTShape shape = (CTShape) xmlObject;
								String style = shape.getStyle();
								String pictureId = shape.getImagedataArray()[0].getId2();
								imageBundleList.put(pictureId, style);
								if (lastRun == null) {
									if (StringUtils.isNotEmpty(run.text())) {
										run.setText(IMG_IDX_1 + pictureId + IMG_IDX_2);
									}
								} else {
									if (StringUtils.isNotEmpty(lastRun.text())) {
										lastRun.setText(IMG_IDX_1 + pictureId + IMG_IDX_2);
									}
								}
							}
						}
					}
				}
			}
            lastRun = run;
        }
        return imageBundleList;
    }
    
    /**
     * 替换索引，并转换格式
     * @param txt
     * @param imgMap
     * @return
     */
    public static String replaceImg(String txt, Map<String, String> imgMap) {
    	if(StringUtils.isBlank(txt) || imgMap == null) {
    		return txt;
    	}
    	String realText = txt;
    	if(txt.indexOf(IMG_IDX_1) != -1 && txt.indexOf(IMG_IDX_2) != -1) {
    		List<String> idxs = findImgIdx(txt);
    		if(idxs.size() >0) {
    			for(String imgIdx : idxs) {
    				if(imgMap.containsKey(imgIdx)) {
    					String img = imgMap.get(imgIdx);
    					String imgSrc = getImgSrc(img);
    					String newSrc = imgSrc;
    					if(imgSrc.indexOf(".wmf") != -1) {
    						newSrc = ImgHellper.wmfToSvg(imgSrc);
						} else if(img.indexOf(".tif") != -1) {
							newSrc = ImgHellper.tiffToJpeg(imgSrc);
						} else if(img.indexOf(".emf") != -1) {
							newSrc = ImgHellper.emfTopng(imgSrc);
						}
    					realText = realText.replace(imgIdx, img.replace(imgSrc, newSrc));
    				}
    			}
    		}
    		realText = realText.replaceAll(IMG_IDX_1, "").replaceAll(IMG_IDX_2, "");
    	}
    	return realText;
    }
    
    /**
     * 索引下标
     * @param txt
     * @return
     */
    private static List<String> findImgIdx(String txt) {
    	List<String> idxs = new ArrayList<String>();
    	if(StringUtils.isBlank(txt)) {
    		return idxs;
    	}
		Pattern pt = Pattern.compile(REGX);
		Matcher mt = pt.matcher(txt);
		while (mt.find()) {
			idxs.add(mt.group(1));
		}
		return idxs;
    }
    
    private static String getImgSrc(String imgStr) {
        String img = "";
        Pattern p = Pattern.compile(IMG_REG, Pattern.CASE_INSENSITIVE);
        Matcher mt = p.matcher(imgStr);
        while (mt.find()) {
            img = mt.group(); // 得到<img />数据
            Matcher m = Pattern.compile(IMG_SRC_REG).matcher(img); // 匹配<img>中的src数据
            while (m.find()) {
                img = m.group(1);
                break;
            }
        }
        return img;
    }
    
}