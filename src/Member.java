import java.io.Serializable;
import java.util.Objects;

import org.json.JSONObject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Member implements Serializable {
	private static final long serialVersionId = 13863215L;

	public String id;
	public String pw;
	public String name;
	public String gender;
	public String phone;

	public Member(String id, String pw, String name, String gender, String phone) {
		super();
		this.id = id;
		this.pw = pw;
		this.name = name;
		this.gender = gender;
		this.phone = phone;
	}
	
	public Member(JSONObject jsonObject) {
		id = jsonObject.getString("id");
		pw = jsonObject.getString("pw");
		name = jsonObject.getString("name");
		gender = jsonObject.getString("gender");
		phone = jsonObject.getString("phone");
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		Member other = (Member) object;
		return Objects.equals(id, other.id);		
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	public static class JoinMember extends Exception {
		public JoinMember(String reason) {
			super(reason);
		}
		
	}

	public static class WithdrawalMember extends Exception {
		public WithdrawalMember(String reason) {
			super(reason);
		}
		
	}
	
	public static class WithdrawalMemberPwd extends Exception {
		public WithdrawalMemberPwd (String reason) {
			super(reason);
		}
		
	}
}
