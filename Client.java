import java.io.*;
import java.net.*;

public class Client
{
	public static Socket clientSocket;			//socket on the client side
	public static BufferedReader inFromServer;	//get message from server
	public static BufferedReader inFromUser;	//get command from user
	public static PrintStream outToServer;		//send message to server


	public static void main(String args[])
	{
		String serverAddress = args[0];				//get server address
		int portNumber = Integer.parseInt(args[1]);	//get server port number
		String line;								//command from user

		//set up client socket and IO
		try
		{
			clientSocket = new Socket(serverAddress, portNumber);
			System.out.println("connected to " + clientSocket.getRemoteSocketAddress());
			outToServer = new PrintStream(clientSocket.getOutputStream());
			inFromUser = new BufferedReader(new InputStreamReader(System.in));
		}
		catch(IOException e)
		{
			System.err.println("error: setup client");
		}

		try
		{
			//create a thread to get message from server and print it out
			ClientThread thread = new ClientThread(clientSocket);
			thread.start();

			//when user use command "logout", close the client
			while(true)
			{
				line = inFromUser.readLine();
				outToServer.println(line);
				if(line.equals("logout"))		
					break;
			}
			thread.exit = true;
			inFromUser.close();
			System.exit(0);
		}
		catch(IOException e)
		{
			System.err.println("error: from user to server" + e.getMessage());
		}
	}
}


