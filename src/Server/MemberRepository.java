package Server;

import Common.Member;

public interface MemberRepository {
	void insertMember(Member member) throws Member.ExistMember;
	Member getMemberById(String id) throws Member.NotExistUidPwd;
	void updateMember(Member member) throws Member.NotExistUidPwd;
	void deleteMember(String id);
	Boolean isExistMember(String id);
}
