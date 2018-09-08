package com.mmall.service.impl;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.utill.MD5Util;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

	@Autowired
	private UserMapper userMapper;

	public ServerResponse<User> login(String username, String password) {
		int resultCount = userMapper.checkUsername(username);
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("�û���������");
		}
		// todo �����½MD5
		String md5Password = MD5Util.MD5EncodeUtf8(password);
		User user = userMapper.selectLogin(username, md5Password);
		if (user == null) {
			return ServerResponse.createByErrorMessage("�������");
		}

		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess("��½�ɹ�", user);
	}

	public ServerResponse<String> register(User user) {

		ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
		if (!validResponse.isSusscess()) {
			return validResponse;
		}

		validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
		if (!validResponse.isSusscess()) {
			return validResponse;
		}

		user.setRole(Const.Role.ROLE_CUSTOMER);
		// md5����
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
		int resultCount = userMapper.insert(user);
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("ע��ʧ��");
		}
		return ServerResponse.createByErrorMessage("ע��ɹ�");
	}

	public ServerResponse<String> checkValid(String str, String type) {
		if (StringUtils.isNoneBlank(type)) {
			// ��ʼУ��
			if (Const.USERNAME.equals(type)) {
				int resultCount = userMapper.checkUsername(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("�û����Ѵ���");
				}
			}
			if (Const.EMAIL.equals(type)) {
				int resultCount = userMapper.checkEmail(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("email�Ѵ���");
				}
			}

		} else {
			return ServerResponse.createByErrorMessage("��������");
		}
		return ServerResponse.createSuccessMessage("У��ɹ�");

	}

	public ServerResponse selectQuestion(String username) {
		ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
		if (validResponse.isSusscess()) {
			return ServerResponse.createByErrorMessage("�û�������");

		}
		String question = userMapper.selectQuestionByUsername(username);
		if (StringUtils.isNoneBlank(question)) {
			return ServerResponse.createBySuccess(question);
		}
		return ServerResponse.createByErrorMessage("�һ���������Ϊ��");
	}

	public ServerResponse<String> checkAnswer(String username, String question, String answer) {
		int resultCount = userMapper.checkAnswer(username, question, answer);
		if (resultCount > 0) {
			// ˵�����⼰�����������û���������ȷ
			String forgetToken = UUID.randomUUID().toString();
			TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
			return ServerResponse.createBySuccess(forgetToken);
		}
		return ServerResponse.createByErrorMessage("����Ĵ𰸴���");

	}

	public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
		if (StringUtils.isBlank(forgetToken)) {
			return ServerResponse.createByErrorMessage("��������token��Ҫ����");
		}
		ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
		if (validResponse.isSusscess()) {
			return ServerResponse.createByErrorMessage("�û�������");

		}
		String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
		if (StringUtils.isBlank(token)) {
			return ServerResponse.createByErrorMessage("token��Ч�����");
		}
		if (StringUtils.equals(forgetToken, token)) {
			String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
			int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

			if (rowCount > 0) {
				return ServerResponse.createSuccessMessage("�޸ĳɹ�");
			}
		} else {
			return ServerResponse.createByErrorMessage("token���������»�ȡ���������token");
		}
		return ServerResponse.createByErrorMessage("�޸�����ʧ��");
	}

	public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
		// ��ֹ����ԽȨ��ҪУ��һ������û��ľ����룬һ��Ҫָ��������û�����Ϊ���ǻ��ѯһ��count(1),�����ָ��id����ô�������true��count>0
		int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("���������");
		}
		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		int updateCount = userMapper.updateByPrimaryKeySelective(user);
		if (updateCount > 0) {
			return ServerResponse.createSuccessMessage("������³ɹ�");
		}
		return ServerResponse.createByErrorMessage("�������ʧ��");

	}
	public ServerResponse<User> updateInformation(User user){
		//username�ǲ��ܱ����µ�
		//emailҲҪ����һ��У�飬У���µ�email�ǲ����Ѵ��ڣ����Ҵ���email�����ͬ�Ļ������������ǵ�ǰ������û��ġ�
		int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
		if(resultCount > 0) {
			return ServerResponse.createByErrorMessage("email�Ѵ���,�����email�ٳ��Ը���");
		}
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setEmail(user.getEmail());
		updateUser.setPhone(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());
	
		int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
		if(updateCount > 0) {
			return ServerResponse.createBySuccess("���¸�����Ϣ�ɹ�",updateUser);
		}
		return ServerResponse.createByErrorMessage("���¸�����Ϣʧ��");
	
	}

	public ServerResponse<User> getInformation(Integer userId){
		User user = userMapper.selectByPrimaryKey(userId);
		if(user == null) {
			return ServerResponse.createByErrorMessage("�Ҳ�����ǰ�û�");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess(user);
	}
}
