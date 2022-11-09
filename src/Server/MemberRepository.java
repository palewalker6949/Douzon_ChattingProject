package Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Common.Member; 
public class MemberRepository
{
	List<Member> memberList = null;
	Map<String, Member> memberMap = null;
	
	public MemberRepository()
	{
		loadMember();
	}
	
	private void loadMember()
	{
		try {
			File file = new File(Env.getMemberFileName());
		
			if (file.exists() && file.length() != 0) {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				memberList = (List<Member>) in.readObject();
				memberMap = memberList.stream()
						.collect(Collectors.toMap(
						m -> m.getUid(),
						m -> m));
				in.close();
			} else {
				memberList = new ArrayList<>();
				memberMap = new HashMap<>();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveMemeber()
	{
		try
		{
			File file = new File(Env.getMemberFileName());
			System.out.println(Env.getMemberFileName());
			ObjectOutputStream out = 
					new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(memberList);
			out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized void insertMember(Member member)
	{
		//회원가입
		memberMap.put(member.getUid(), member);
		memberList.add(member);
		saveMemeber();
	}
	
	public synchronized void updateMember(Member member)
	{
		int index = memberList.indexOf(member);
		//회원 정보 수정
		memberMap.put(member.getUid(), member);
		memberList.set(index,member);
		saveMemeber();
		
	}
	
	public Member getMemberById(String id)
	{
		Member findMember = memberMap.get(id);
		if(findMember == null)
		{
			System.out.println("findById : 아이디가 존재하지 않습니다");
			return null;
		}
		//id 통해 검색
		return findMember;
	}
	
	public synchronized void deleteMember(String id)
	{
		//회원 탈퇴
		if( memberMap.get(id) == null)
		{
			System.out.println("delete member : 아이디가 존재하지 않습니다");
			return;
		}
		int index = memberList.indexOf(memberMap.get(id));
		memberList.remove(index);
		memberMap.remove(id);
		saveMemeber();
	}
	
	public Boolean isExistMember(String id)
	{
		if(memberMap.get(id) == null)
			return false;
		else
			return true;
	}
	
	private void testShowMemberList()
	{
		System.out.println(memberMap.toString());
	}
	
	
	public static void main(String[] args)
	{
		MemberRepository memberRepository = new MemberRepository();
		memberRepository.testShowMemberList();
		
	}
}
