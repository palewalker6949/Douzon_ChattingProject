import java.io.File;
import java.util.Scanner;

public class Test
{
	public static void main(String[] args)
	{
		
		String dirPath = "C:\\Users\\pale\\Desktop\\d";
		Scanner sc = new Scanner(System.in);
		
		System.out.println("조회 하실 파일명을 입력해주세요..");
		String fn = sc.nextLine();
		boolean hasFile = false;
		
		File dir = new File(dirPath);
		File[] files = dir.listFiles(); //dir 경로에 있는 모든 파일을 배열에 넣기 
		for(File f : files) {
			if(f.isFile() && f.getName().toUpperCase().startsWith(fn) ) {
				hasFile = true;
			} if (!(f.getName().toUpperCase().startsWith(fn))  ) {
				hasFile = false;
			}  	
		}  
		int a=0;
		if(hasFile = true) {
			System.out.println(fn + "이란 파일이 있습니다. ");
		}  else System.out.println("그런 파일은 없습니다.. ");
	}
	
}
