package com.mmall.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.utill.FTPUtil;


@Service("iFileService")
public class FileServiceImpl implements IFileService {
	
	private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
	
	public String upload(MultipartFile file,String path) {
		String fileName = file.getOriginalFilename();
		//��չ��
		//abc.jpg
		String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
		String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
		logger.info("��ʼ�ϴ��ļ����ϴ��ļ����ļ���{}���ϴ���·����{}�����ļ�����{}",fileName,path,uploadFileName);
		
		File fileDir = new File(path);
		if(!fileDir.exists()) {
			fileDir.setWritable(true);
			fileDir.mkdirs();
		}
		File targetFile = new File(path,uploadFileName);
		
		try {
			file.transferTo(targetFile);
			//�ļ��Ѿ��ϴ��ɹ�
		FTPUtil.uploadFile(Lists.newArrayList(targetFile));
			// �Ѿ��ϴ���ftp��������
		targetFile.delete();	
		//todo �ϴ����ɾ��upload�µ��ļ�
		} catch (IOException e) {
			logger.error("�ϴ��ļ��쳣",e);
			return null;
		}
		
		return fileExtensionName;
	
	}
}
