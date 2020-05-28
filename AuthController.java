package jp.co.internous.phoenix.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.phoenix.model.domain.MstUser;
import jp.co.internous.phoenix.model.form.UserForm;
import jp.co.internous.phoenix.model.mapper.MstUserMapper;
import jp.co.internous.phoenix.model.mapper.TblCartMapper;
import jp.co.internous.phoenix.model.session.LoginSession;

@RestController
@RequestMapping("/phoenix/auth")
public class AuthController {
	
	private Gson gson = new Gson();
	
	@Autowired
	private MstUserMapper userMapper;
	
	@Autowired
	private LoginSession loginSession;
	
	@Autowired
	private TblCartMapper cartMapper;
	

	
	@RequestMapping("/login")
	public String Login(@RequestBody UserForm userForm) {
		MstUser user = userMapper.findByUserNameAndPassword(userForm.getUserName(), userForm.getPassword());
		int tmpUserId = loginSession.getTmpUserId();
		if(user != null && tmpUserId != 0) {
			int count = cartMapper.findCountByUserId(tmpUserId) ;
			if(count > 0) {
				cartMapper.updateUserId(user.getId(), tmpUserId);
			}
		}
		if(user != null) {
			loginSession.setUserId(user.getId());
			loginSession.setTmpUserId(0);
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());
			loginSession.setLogined(true);
		}else {
			loginSession.setUserId(0);
			loginSession.setUserName(null);
			loginSession.setPassword(null);
			loginSession.setLogined(false);
		}
		return gson.toJson(user);
	}
	
	@RequestMapping("/logout")
	public String Logout() {
		loginSession.setTmpUserId(0);
		loginSession.setUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		loginSession.setLogined(false);
		return gson.toJson("");
	}
	
	@RequestMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm f) {
		String message = "パスワードが再設定されました。";
		String newPassword = f.getNewPassword();
		String newPasswordConfirm = f.getNewPasswordConfirm();
		MstUser user = userMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());
		
		if(user == null) {
			return "現在のパスワードが正しくありません。";
		}
		if(user.getPassword().equals(newPassword)) {
			return "現在のパスワードと同一文字列が入力されました。";
		}
		if(!newPassword.equals(newPasswordConfirm)) {
			return "新パスワードと確認用パスワードが一致しません。";
		}
		userMapper.updatePassword(user.getUserName(), f.getNewPassword());
		loginSession.setPassword(f.getNewPassword());
		return message;
	}
}