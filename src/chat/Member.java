package chat;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import org.json.JSONObject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Member implements Serializable {
	private static final long serialVersionUID = 1449132512754742285L;
	private String uid;
	private String pwd;
	private String name;
	private String gender;
	private String phone;
	private String email;
	private LocalDateTime loginDateTime;
	
	public Member(String uid, String pwd, String name, String sex, String address, String phone, LocalDateTime loginDateTime) {
		super();
		this.uid = uid;
		this.pwd = pwd;
		this.name = name;
		this.gender = gender;
		this.phone = phone;
		this.email = email;
		this.loginDateTime = loginDateTime;
	}
	
	public Member(JSONObject jsonObject) {
		uid = jsonObject.getString("uid");
		pwd = jsonObject.getString("pwd");
		name = jsonObject.getString("name");
		gender = jsonObject.getString("gender");
		phone = jsonObject.getString("phone");
		email = jsonObject.getString("email");
        loginDateTime = null;
	}
	
	public Member(String memuid, String mempwd) {
    }

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		return Objects.equals(uid, other.uid);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(uid);
	}

	public static class ExistMember extends Exception {
		public ExistMember(String reason) {
			super(reason);
		}
	}
	
	public static class NotExistMember extends Exception {
		public NotExistMember(String reason) {
			super(reason);
		}
	}
	
	public static class NotExistUidPwd extends Exception {
		public NotExistUidPwd(String reason) {
			super(reason);
		}
	}

    public JSONObject getJsonObject() {
        JSONObject jsonMember = new JSONObject();
        jsonMember.put("Uid", uid);
        jsonMember.put("Pwd", pwd);
        jsonMember.put("Name", name);
        jsonMember.put("Gender", gender);
        jsonMember.put("Phone", phone);
        jsonMember.put("Email", email);
        return jsonMember;
    }

    public boolean login(String loginuid, String loginpwd) {
        return loginuid.equals(uid) && loginpwd.equals(pwd);
    }

    public String LoginPw() {
		return pwd;
	}

    public void setLoginPw(String pwd) {
		this.pwd = pwd;
	}
    
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}


	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}	
}
