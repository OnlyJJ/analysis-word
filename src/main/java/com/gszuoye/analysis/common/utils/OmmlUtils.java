package com.gszuoye.analysis.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.gszuoye.analysis.common.constants.Constants;

import fr.opensagres.xdocreport.core.utils.StringUtils;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.converter.Converter;

/**
 * 公式转换
 *
 */
public class OmmlUtils {
	/**
	 * 插件，不要加相对路径
	 */
	private static final String XSLFILE = "/OMML2MML.XSL";
	private static String CN_REG = "[，。？、．：‘’“”＂＂；＜＞［］｛｝％＊／｜《》【】｜]+";

	/**
	 * 直接转node有等号会出问题，先转成xml的string，再转成mathML的字符串
	 *
	 * @param node
	 * @return
	 * @throws IOException
	 * @throws TransformerException
	 */
	private static String getMathMLFromNode(Node node) throws IOException, TransformerException {
		StreamSource streamSource = new StreamSource(OmmlUtils.class.getResourceAsStream(XSLFILE));
		String s = W3cNodeUtil.node2XmlStr(node);
		String mathML = W3cNodeUtil.xml2Xml(s, streamSource);
		mathML = mathML.replaceAll("xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"", "");
		mathML = mathML.replaceAll("xmlns:mml", "xmlns");
		mathML = mathML.replaceAll("mml:", "");
		return mathML;
	}

	public static String convertOmathToPng(XmlObject xmlObject, ImageParse imageParser) {
		Document document = null;
		try {
			String mathMLStr = getMathMLFromNode(xmlObject.getDomNode());
			// 因为omm是windows的，linux中文会乱码，这里把公式中，带有中文符号的，都替换回英文
			Pattern pt = Pattern.compile(CN_REG);
			Matcher mt = pt.matcher(mathMLStr);
			while (mt.find()) {
				String group = mt.group();
				if (StringUtils.isNotEmpty(group)) {
					// 把这个字符串中的中文括号换成英文括号
					if (group.equals("（")) {
						mathMLStr = mathMLStr.replaceAll("（", "(");
					} else if (group.equals("）")) {
						mathMLStr = mathMLStr.replaceAll("）", ")");
					} else if (group.equals("，")) {
						mathMLStr = mathMLStr.replaceAll("，", ", ");
					} else if (group.equals("＠")) {
						mathMLStr = mathMLStr.replaceAll("＠", "@");
					} else if (group.equals("；")) {
						mathMLStr = mathMLStr.replaceAll("；", "; ");
					} else if (group.equals("？")) {
						mathMLStr = mathMLStr.replaceAll("？", "? ");
					} else if (group.equals("“")) {
						mathMLStr = mathMLStr.replaceAll("“", "\"");
					} else if (group.equals("”")) {
						mathMLStr = mathMLStr.replaceAll("”", "\"");
					} else if (group.equals("、")) {
						mathMLStr = mathMLStr.replaceAll("、", ". ");
					} else if (group.equals("＂")) {
						mathMLStr = mathMLStr.replaceAll("＂", "\"");
					} else if (group.equals("：")) {
						mathMLStr = mathMLStr.replaceAll("：", ": ");
					} else if (group.equals("。")) {
						mathMLStr = mathMLStr.replaceAll("。", ". ");
					} else if (group.equals("＊")) {
						mathMLStr = mathMLStr.replaceAll("＊", "*");
					} else if (group.equals("％")) {
						mathMLStr = mathMLStr.replaceAll("％", "%");
					} else if (group.equals("＜")) {
						mathMLStr = mathMLStr.replaceAll("＜", "<");
					} else if (group.equals("＞")) {
						mathMLStr = mathMLStr.replaceAll("＞", ">");
					} else if (group.equals("‘")) {
						mathMLStr = mathMLStr.replaceAll("‘", "'");
					} else if (group.equals("’")) {
						mathMLStr = mathMLStr.replaceAll("’", "'");
					} else if (group.equals("［")) {
						mathMLStr = mathMLStr.replaceAll("［", "[");
					} else if (group.equals("］")) {
						mathMLStr = mathMLStr.replaceAll("］", "]");
					} else if (group.equals("｛")) {
						mathMLStr = mathMLStr.replaceAll("｛", "{");
					} else if (group.equals("｝")) {
						mathMLStr = mathMLStr.replaceAll("｝", "}");
					} else if (group.equals("／")) {
						mathMLStr = mathMLStr.replaceAll("／", "/");
					} else if (group.equals("｜")) {
						mathMLStr = mathMLStr.replaceAll("｜", "|");
					} else if (group.equals("《")) {
						mathMLStr = mathMLStr.replaceAll("《", "<");
					}  else if (group.equals("》")) {
						mathMLStr = mathMLStr.replaceAll("》", ">");
					}  else if (group.equals("【")) {
						mathMLStr = mathMLStr.replaceAll("【", "[");
					}    else if (group.equals("】")) {
						mathMLStr = mathMLStr.replaceAll("】", "]");
					}                     
				}
			}
			document = W3cNodeUtil.xmlStr2Node(mathMLStr, "utf-16");
			return documentToImageHTML(document, imageParser);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String documentToImageHTML(Document document, ImageParse imageParser) {
		try {
			Converter mathMLConvert = Converter.getInstance();
			LayoutContextImpl localLayoutContextImpl = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
			localLayoutContextImpl.setParameter(Parameter.MATHSIZE, 18);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			mathMLConvert.convert(document, os, "image/png", localLayoutContextImpl);
			String pngName = imageParser.parse(os.toByteArray(), ".png");
			os.close();
			return "<img src=\"" + pngName + "\" align=\"absmiddle\"/>";
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
