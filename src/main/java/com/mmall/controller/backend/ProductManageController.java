package com.mmall.controller.backend;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.utill.PropertiesUtil;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

	@Autowired
	private IUserService iUserService;
	@Autowired
	private IProductService iProductService;
	@Autowired
	private IFileService IFileService;

	@RequestMapping("save.do")
	@ResponseBody
	public ServerResponse productSave(HttpSession session, Product product) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "�û�δ��½�����½����Ա");
		}
		if (iUserService.checkAdminRole(user).isSusscess()) {
			// ����������Ӳ�Ʒ��ҵ���߼�
			return iProductService.saveOrUpdateProduct(product);
		} else {
			return ServerResponse.createByErrorMessage("��Ȩ�޲���");
		}
	}

	@RequestMapping("set_sale_status.do")
	@ResponseBody
	public ServerResponse setSalesStatus(HttpSession session, Integer productId, Integer status) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "�û�δ��½�����½����Ա");
		}
		if (iUserService.checkAdminRole(user).isSusscess()) {
			// ����������Ӳ�Ʒ��ҵ���߼�
			return iProductService.setSaleStatus(productId, status);
		} else {
			return ServerResponse.createByErrorMessage("��Ȩ�޲���");
		}
	}

	// ��ȡ��Ʒ����
	@RequestMapping("detail.do")
	@ResponseBody
	public ServerResponse getDetail(HttpSession session, Integer productId) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "�û�δ��½�����½����Ա");
		}
		if (iUserService.checkAdminRole(user).isSusscess()) {
			// ���ҵ��
			return iProductService.manageProductDetail(productId);
		} else {
			return ServerResponse.createByErrorMessage("��Ȩ�޲���");
		}
	}

	@RequestMapping("list.do")
	@ResponseBody
	public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "�û�δ��½�����½����Ա");
		}
		if (iUserService.checkAdminRole(user).isSusscess()) {
			// ���ҵ��
			return iProductService.getProductList(pageNum, pageSize);
		} else {
			return ServerResponse.createByErrorMessage("��Ȩ�޲���");

		}
	}

	@RequestMapping("search.do")
	@ResponseBody
	public ServerResponse productSearch(HttpSession session, String productName, Integer productId,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "�û�δ��½�����½����Ա");
		}
		if (iUserService.checkAdminRole(user).isSusscess()) {
			// ���ҵ��
			return iProductService.searchProduct(productName, productId, pageNum, pageSize);
		} else {
			return ServerResponse.createByErrorMessage("��Ȩ�޲���");
		}
	}

	@RequestMapping("upload.do")
	@ResponseBody
	public ServerResponse upload(HttpSession session,
			@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "�û�δ��½�����½����Ա");
		}
		if (iUserService.checkAdminRole(user).isSusscess()) {
			String path = request.getSession().getServletContext().getRealPath("upload");
			String targetFileName = IFileService.upload(file, path);
			String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

			Map fileMap = Maps.newHashMap();
			fileMap.put("uri", targetFileName);
			fileMap.put("url", url);
			return ServerResponse.createBySuccess(fileMap);

		} else {
			return ServerResponse.createByErrorMessage("��Ȩ�޲���");
		}
	}

	@RequestMapping("richtext_img_upload.do")
	@ResponseBody
	public Map richtextImgupload(HttpSession session,@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
		Map resultMap = Maps.newHashMap();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null) {
		resultMap.put("success", false);
		resultMap.put("msg", "���½����Ա");
		return resultMap;
	}
		//���ı��ж��ڷ���ֵ���Լ���Ҫ������ʹ�õ���simditor���԰���simditor��Ҫ�����
	if (iUserService.checkAdminRole(user).isSusscess()) {
			String path = request.getSession().getServletContext().getRealPath("upload");
			String targetFileName = IFileService.upload(file, path);
		if(StringUtils.isBlank(targetFileName)) {
				resultMap.put("success", false);
				resultMap.put("msg", "�ϴ�ʧ��");
				return resultMap;
			}
			String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
		resultMap.put("success", true);
		resultMap.put("msg", "�ϴ��ɹ�");
			response.addHeader("Access-conroll-Allow-hHeads","X-File-Name");
		return resultMap;
		} else {
			resultMap.put("success", false);
			resultMap.put("msg", "��Ȩ�޲���");
			return resultMap;
		}
	}

}