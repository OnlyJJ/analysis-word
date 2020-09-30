package com.gszuoye.analysis.common.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.gszuoye.analysis.common.constants.Constants;

/**
 * 公式图片转换
 *
 */
public class ImageParse {
	int number = 0;
	private String targetDir;
	private String baseUrl;

	public ImageParse(String targetDir, String baseUrl) {
		super();
		this.targetDir = targetDir;
		this.baseUrl = baseUrl;
	}

	public String parse(byte[] data, String extName) {
		return parse(new ByteArrayInputStream(data), extName);
	}

	public String parse(InputStream in, String extName) {
		if (extName.lastIndexOf(".") > -1) {
			extName = extName.substring(extName.lastIndexOf(".") + 1);
		}
		String filename = "mathImg_" + (number++) + "." + extName;
		File target = new File(targetDir);
		if (!target.exists()) {
			target.mkdirs();
		}
		try {
			IOUtils.copy(in, new FileOutputStream(new File(target, filename)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String url = baseUrl + filename;
		url = Constants.DOMAIN + url.replace(Constants.ABSOLUTELY_PATH, "/profile/");
		return url;
	}
}
