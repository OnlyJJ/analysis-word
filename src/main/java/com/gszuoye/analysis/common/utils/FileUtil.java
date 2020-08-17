package com.gszuoye.analysis.common.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gszuoye.analysis.common.constants.Constants;
import com.gszuoye.analysis.exception.BusinessException;

public class FileUtil {
	/**
	 * 默认的文件名最大长度 100
	 */
	public static final int DEFAULT_FILE_NAME_LENGTH = 100;

	private static final String IMG_MET_INFO = "data:image/png;base64,";
	
	/**
	 * 上传到oss，单张
	 * 		测试发现，批量（10张）上传，比单张上传，总的耗时更长，所以还是使用单张上传
	 * @param path 图片路径
	 * @return
	 */
	public static String uploadOssFile(String path) {
		return uploadOss(path, null, false);
	}
	
	/**
	 * 上传到oss，单张
	 * 		测试发现，批量（10张）上传，比单张上传，总的耗时更长，所以还是使用单张上传
	 * @param img 图片base64格式
	 * @return
	 */
	public static String uploadOssBase64(String img) {
		return uploadOss(null, img, true);
	}
	
	/**
	 * 文件名路径，日期+文件名hash
	 * @param fileName 文件名
	 * @return
	 */
	public static String genFilePath(String fileName) {
		fileName = fileName.replaceAll("_", "").replaceAll(" ", "");
        return Constants.IMAGE_PATH + DateUtil.datePath() + "/" + Md5Util.hash(fileName + System.nanoTime());
//		return "E://myworkspace//wordPOI//img//" + DateUtil.datePath() + "/" + Md5Util.hash(fileName + System.nanoTime());
	}

	/**
	 * 转换base64（带文件信息）
	 * 
	 * @param filePath 文件路径
	 * @return
	 */
	public static String encodeBase64(String filePath) {
		InputStream in = null;
		byte[] data = null;
		try {
			in = new FileInputStream(filePath);
			data = new byte[in.available()];
			in.read(data);
		} catch (Exception e) {

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
		return IMG_MET_INFO + Base64.getEncoder().encodeToString(data);
	}

	/**
	 * 解码
	 * 
	 * @param encode
	 * @return
	 */
	public static byte[] decodeBase64(byte[] encode) {
		return Base64.getDecoder().decode(encode);
//        System.out.println(new String(decode, "UTF-8"));
	}
	
	

	/**
	 * 上传文件
	 * 
	 */
	public static final String upload(String baseDir, MultipartFile file) throws Exception {
		int fileNamelength = file.getOriginalFilename().length();
		if (fileNamelength > DEFAULT_FILE_NAME_LENGTH) {
			throw new BusinessException("文件最大不能超过" + DEFAULT_FILE_NAME_LENGTH);
		}

		assertAllowed(file,  MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

		String fileName = extractFilename(file);

		File desc = getAbsoluteFile(baseDir, fileName);
		file.transferTo(desc);
		return Constants.FILE_PATH + fileName;
	}

	/**
	 * 编码文件名
	 */
	public static final String extractFilename(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String extension = getExtension(file);
		fileName = DateUtil.datePath() + "/" + encodingFilename(fileName) + "." + extension;
		return fileName;
	}

	public static void assertAllowed(MultipartFile file, String[] allowedExtension) throws Exception {
		if (allowedExtension != null && !isAllowedExtension(getExtension(file), allowedExtension)) {
			throw new BusinessException("不支持该文件类型上传");
		}
	}

	/**
	 * 获取文件名的后缀
	 * 
	 * @param file 表单文件
	 * @return 后缀名
	 */
	public static final String getExtension(MultipartFile file) {
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		if (StringUtils.isEmpty(extension)) {
			extension = MimeTypeUtils.getExtension(file.getContentType());
		}
		return extension;
	}

	public static final boolean isAllowedExtension(String extension, String[] allowedExtension) {
		for (String str : allowedExtension) {
			if (str.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 批量上传，每次10张
	 * @param imgList
	 * @return
	 */
	public static Map<String, String> batchLoadImg(List<String> imgList) {
		Map<String, String> ossMap = new LinkedHashMap<String, String>();
		if(CollectionUtils.isEmpty(imgList)) {
			return ossMap;
		}
		JSONArray param = new JSONArray();
		int index = 0;
		int imgSize = imgList.size();
		for(String s : imgList) {
			String base64Img = FileUtil.encodeBase64(s);
			JSONObject img = new JSONObject();
			img.put("type", ".png");
			img.put("data", base64Img);
			param.add(img);
			 // 每次提交10张图片，以及最后不足10张的
			if(index >= 10 || index == imgSize) {
				JSONObject res = HttpUtil.post(Constants.UPLOAD_OSS_URL, param.toJSONString());
				if(res.containsKey("extend") && res.getJSONArray("extend") != null) {
					JSONArray arrays = res.getJSONArray("extend");
					if(arrays.size() > 0) {
						for(int i=0; i< arrays.size(); i++) {
							ossMap.put(param.getString(i), arrays.getString(i));
						}
					}
				}
				index = 0;
			} else {
				index++;
			}
			imgSize--;
		};
		return ossMap;
	}
	
	private static final String encodingFilename(String fileName) {
		fileName = fileName.replace("_", " ");
		fileName = Md5Util.hash(fileName + System.nanoTime());
		return fileName;
	}

	
	private static final File getAbsoluteFile(String uploadDir, String fileName) throws IOException {
		File desc = new File(uploadDir + File.separator + fileName);
		if (!desc.getParentFile().exists()) {
			desc.getParentFile().mkdirs();
		}
		if (!desc.exists()) {
			desc.createNewFile();
		}
		return desc;
	}
	
	private static String uploadOss(String path, String base64, boolean base64Encode) {
		String ossPath = "";
		String base64Img = base64;
		if(!base64Encode) {
			base64Img = FileUtil.encodeBase64(path);
		}
		JSONArray param = new JSONArray();
		JSONObject img = new JSONObject();
		img.put("type", ".png");
		img.put("data", base64Img);
		param.add(img);
		JSONObject res = HttpUtil.post(Constants.UPLOAD_OSS_URL, param.toJSONString());
		if(res.containsKey("extend") && res.getJSONArray("extend") != null) {
			ossPath = res.getJSONArray("extend").getString(0);
		}
		return ossPath;
	}
	
	
	 /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } 

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        } 

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流  
        FileInputStream fis = new FileInputStream(filePath);  
        // 创建一个长度为10240的Buffer
        byte[] bbuf = new byte[10240];  
        // 用于保存实际读取的字节数  
        int hasRead = 0;  
        while ( (hasRead = fis.read(bbuf)) > 0 ) {  
            sb.append(new String(bbuf, 0, hasRead));  
        }  
        fis.close();  
        return sb.toString();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            BufferedInputStream in = null;

            try {
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                byte[] var7 = bos.toByteArray();
                return var7;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }

                bos.close();
            }
        }
    }
}
