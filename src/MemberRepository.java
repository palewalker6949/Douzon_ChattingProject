import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemberRepository {

	List<Member> memberList = null;
	Map<String, Member> memberMap = null;
	private static final String MEMBER_FILE_NAME = "C:\\temp\\member.db";
	
	public void loadMember() {
		try {
			File file = new File(MEMBER_FILE_NAME);
			if (file.exists() && file.length() != 0) {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				memberList = (List<Member>) in.readObject();
				memberMap = memberList.stream()
						.collect(Collectors.toMap(
						m -> m.getid(),
						m -> m));
				in.close();
			} else {
				memberList = new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void saveMember() {
		try {
			File file = new File(MEMBER_FILE_NAME);
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(memberList);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void insertMember(Member member) throws Member.JoinMember {
		if (true == memberList.stream().anyMatch(m -> member.getid().equals(m.getid))) {
			
		}
	}
}
