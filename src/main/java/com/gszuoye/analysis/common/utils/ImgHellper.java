package com.gszuoye.analysis.common.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFRenderer;
import org.w3c.dom.Document;

import com.gszuoye.analysis.common.constants.Constants;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jed.fpx.KernelerJAI;

import net.arnx.wmf2svg.gdi.svg.SvgGdi;
import net.arnx.wmf2svg.gdi.wmf.WmfParser;

/**
 * 图片处理类
 *
 */
public class ImgHellper {
	
	private static final int width = 680;
	private static final int height = 720;
	private static final int xOrigin = 102;
	private static final int yOrigin = 102;
	
	/**
	 * wmf转svg
	 * @param src
	 * @return
	 */
	public static String wmfToSvg(String src) {
		// 替换回来
		String absoUrl = src.replace(Constants.DOMAIN, "").replace("/profile/", Constants.ABSOLUTELY_PATH);
		File file = new File(absoUrl);
		boolean compatible = false;
		OutputStream out = null;
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			WmfParser parser = new WmfParser();
			final SvgGdi gdi = new SvgGdi(compatible);
			parser.parse(in, gdi);

			Document doc = gdi.getDocument();
			String dest = absoUrl.substring(0,
					absoUrl.lastIndexOf(".wmf"))
	                + ".svg";
			out = new FileOutputStream(dest);
			output(doc, out);
			return Constants.DOMAIN + dest.replace(Constants.ABSOLUTELY_PATH, "/profile/");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
	
	/**
	 * tiff转jpg
	 * @param file
	 * @return
	 */
	public static String tiffToJpeg(String file) {
		OutputStream os = null;
		try {
			String absoUrl = file.replace(Constants.DOMAIN, "").replace("/profile/", Constants.ABSOLUTELY_PATH);
			String dest = absoUrl.substring(0, absoUrl.lastIndexOf(".tiff")) + ".jpg";
			RenderedOp rd = JAI.create("fileload", absoUrl);
			os = new FileOutputStream(dest);
			JPEGEncodeParam param = new JPEGEncodeParam();
			// 指定格式类型，jpg 属于 JPEG 类型
			ImageEncoder enc = ImageCodec.createImageEncoder("JPEG", os, param);
			enc.encode(rd);
			return Constants.DOMAIN + dest.replace(Constants.ABSOLUTELY_PATH, "/profile/");
		} catch (Exception e) {
//			
		} finally {
			try {
				if(os != null) {
					os.close();
				}
			} catch (IOException e1) {
//				e1.printStackTrace();
			}
		}
		return "";
	}
	
	public static String emfTopng(String saveUrl) {
		try {
			InputStream is = new FileInputStream(saveUrl);
			EMFInputStream eis = new EMFInputStream(is, EMFInputStream.DEFAULT_VERSION);
			EMFRenderer emfRenderer = new EMFRenderer(eis);
			final int width = (int) eis.readHeader().getBounds()
					.getWidth();
			final int height = (int) eis.readHeader().getBounds()
					.getHeight();
			// 设置图片的大小和样式
			final BufferedImage result = new BufferedImage(width + 60,
					height + 40, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2 = (Graphics2D) result.createGraphics();
			emfRenderer.paint(g2);
			String url = saveUrl.replace(
					saveUrl.substring(saveUrl.length() - 3), "png");
			File outputfile = new File(url);
			// 写入到磁盘中(格式设置为png背景不会变为橙色)
			ImageIO.write(result, "png", outputfile);
			// 当前的图片写入到磁盘中后，将流关闭
			if (eis != null) {
				eis.close();
			}
			if (is != null) {
				is.close();
			}
			System.err.println(url);
			return url;
		}catch (Exception e) {
			// TODO: handle exception
		}
		return saveUrl;
	} 
	
	public static void calculate(String file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			new KernelerJAI(width, height, xOrigin, yOrigin, true).calculate(is);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
	}

	private static void output(Document doc, OutputStream out) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD SVG 1.0//EN");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
				"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
		transformer.transform(new DOMSource(doc), new StreamResult(out));
	}
}
