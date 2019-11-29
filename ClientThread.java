import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

class ClientThread extends Thread
{
	public BufferedReader inFromServer;		//get message from server
	public Socket clientSocket;				//client socket
	public volatile boolean exit = false; 	//flag that determines whether to close the thread

	//class constructor
	public ClientThread(Socket clientSocket) 
	{
        this.clientSocket = clientSocket;
    }

	public void run()
	{
		String serverMessage;
        
        //keep reading message from server
        try 
        {
        	inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!exit)
            {
            	serverMessage = inFromServer.readLine();
            	if(serverMessage == null)
            		break;
                System.out.println(serverMessage);
                if(serverMessage.equals("no response, logout"))
                	System.exit(1);
            }
            inFromServer.close();
            clientSocket.close();
        } 
        catch (Exception e) 
        {
            System.err.println("error: logout error");
        }
	}
}