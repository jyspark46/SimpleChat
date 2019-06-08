
// https://github.com/jyspark46/SimpleChat

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			String str = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if((str = checkword(line))!= null){
					warning(str);
				}
				else if(line.equals("/userlist")){
					senduserlist();
				}
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
    
    // 기능 1번
    // 1. run()에서 읽은 값이 /userlist 이면 send_userlist()를 호출
    // 2. keyset에 hm의 key를 불러옴
    // 3. hm의 size도 불러옴
    // 4. userlist 정보를 보낼 대상 설정
    // 5. pw 정의 후 keyset 출력 & 사용자 수도 출력
	private void senduserlist(){
		int j = 1;
		PrintWriter pw = null;
		Object obj = null;
		Iterator<String> iter = null;
		synchronized(hm){
			iter = hm.keySet().iterator();
			obj = hm.get(id);
		}
		if(obj != null){
				pw = (PrintWriter)obj;
		}
		pw.println("<User list>");
		while(iter.hasNext()){
				String list = (String)iter.next();
				pw.println(j+". "+list);
				j++;
		}
		j--;
		pw.println("Total : "+j+".");
		pw.flush();
	}
    
    // 기능 3번
    // 1. 만약 입력받은 문장에 word[] 내의 단어가 포함되어있으면 그 단어를 리턴 (checkword 함수)
	public String checkword(String msg){
		int b = 1;
		String[] word ={"shit","bitch","damn","idiot","fool"};
		for(int i=0;i<word.length;i++){
			if(msg.contains(word[i]))
				return word[i];
		}
		return null;
	}
    // 2. 리턴받은 글자를 이용해 경고 메시지 출력
	public void warning(String msg){
		Object obj = hm.get(id);
		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("Don't use "+ msg);
				pw.flush();
		} // if
	}
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
    
    // 기능 2번
    // 1. iter.next() 의 값이 hm.get(id) 의 값과 다르면 즉, 본인이 아니면
    // 2. broadcast 실행 (값이 같으면, 즉 본인이면 실행 x)
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				PrintWriter pw2 = (PrintWriter)hm.get(id);
				if(pw==pw2) continue;
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
}

  	// 기능 1번
  	// 1. run()에서 읽은 값이 /userlist 이면 send_userlist()를 호출
  	// 2. keyset에 hm의 key를 불러옴
  	// 3. hm의 size도 불러옴
  	// 4. userlist 정보를 보낼 대상 설정
  	// 5. pw 정의 후 keyset 출력 & 사용자 수도 출력
  	/*public void send_userlist(String id){
    		Set keyset = hm.keySet();
    		int size = hm.size();
		Object obj = hm.get(id);
		PrintWriter pw = (PrintWriter)obj;
		pw.println(keyset);
		pw.flush();
		pw.println("user count: "+ size);
		pw.flush();
  	}*/

