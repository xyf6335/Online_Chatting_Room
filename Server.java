import java.io.*;
import java.net.*;
import java.util.*; 


public class Server
{
	public static ServerSocket serverSocket;	//server socket
	public static Socket server;				//socket on the server side for each connection
	
	//valid username and corresponding password
	public static Map<String, String> nameToPassword = new HashMap<>();

	//online users' name, thread and login time
	public static Map<String, ServerThread> nameToThread = new HashMap<>();
	public static Map<String, Long> nameToLoginTime = new HashMap<>();

	//blacklist: name, address and time
	public static Map<String, InetAddress> blacklistAddress = new HashMap<>();
	public static Map<String, Long> blacklistTime = new HashMap<>();

	//offline message: send to user once online
	//key is receiver, value is [sender, massage]
	public static Map<String, List<String[]>> offlineMessage = new HashMap<>();

	public static void main(String args[])
	{
		int portNumber = Integer.parseInt(args[0]); 	//get server port number
		LoadUsers();									//load valid users' info
		SetupServer(portNumber);						//set up server
		
		//listen and accept coonnection from client
		//once accept, create a thread
		while(true)
		{
			try
			{
				server = serverSocket.accept();
				ServerThread thread = new ServerThread(server);
				thread.start();
			}
			catch (IOException e)
			{
				System.err.println("error: server thread");
			}
		}	
	}


	//get info from the giving user_pass.txt
	public static void LoadUsers()
	{
		try
		{
			BufferedReader txtReader = new BufferedReader(new FileReader("./user_pass.txt"));
			String lineBuffer = txtReader.readLine();
			while(lineBuffer != null)
			{
				String[] userInfo = lineBuffer.split(" ");		//split the name and password
				nameToPassword.put(userInfo[0], userInfo[1]);
				lineBuffer = txtReader.readLine();
			}
		}
		catch(IOException e)
		{
			System.err.println("error: load users" );
		}
	}

	//setup server, if no client for a long time, server closed
	public static void SetupServer(int port)
	{
		try
		{
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(10000000);
			System.out.println("Waiting for clients.");
		}
		catch(IOException e)
		{
			System.err.println("error: setup server");
		}
	}
}
