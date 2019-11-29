import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ServerThread extends Thread
{

    final int BLOCK_TIME = 10;		//seconds,  blocktime when input wrong password for three times
    final int TIME_OUT = 30;		//minutes,  if client do nothing for a time, the thread will close

    //IO
    public BufferedReader inFromClient;
    public DataOutputStream outToClient;

    public Socket socket;

    String name;
    boolean status = true;			//the status of client
    boolean noResponse = false;		//flag that shows whether client has no response

    int pwdWrong = 0;				//times of wrong password input

    //class constructor
    public ServerThread(Socket server)
    {
        this.socket = server;
    }


    //thread starts
    public void run()
    {
        //setup IO
        try
        {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e)
        {
            System.err.println("error: server thread IO");
        }

        //user login
        name = logIn(name);
        //function implements
        command(name);
    }


    public String logIn(String name)
    {

        InetAddress addr;

        //username judgement
        while(status)
        {
            //if the user has been put in the blacklist for enough time, remove it
            for(String blackedName: Server.blacklistAddress.keySet())
            {
                if((System.currentTimeMillis() - Server.blacklistTime.get(blackedName)) >= (BLOCK_TIME * 1000) )
                {
                    Server.blacklistAddress.remove(blackedName);
                    Server.blacklistTime.remove(blackedName);

                }
            }
            try
            {
                outToClient.writeBytes("\nPlease enter username:\n" );

                socket.setSoTimeout(TIME_OUT * 60000);	//if long time no response, catch IOException
                //get the user's name and address
                name = inFromClient.readLine();
                addr = socket.getInetAddress();

                if (!Server.nameToPassword.containsKey(name))	//valid user or not
                {
                    outToClient.writeBytes("username invalid\n");
                }
                else if(Server.nameToThread.containsKey(name))		//if the user has been online, he cannot longin agian
                {
                    outToClient.writeBytes("user is online\n");
                }

                else if(Server.blacklistAddress.containsKey(name))		//in blacklist or not
                {
                    if (Server.blacklistAddress.get(name).equals(addr))
                    {
                        outToClient.writeBytes("\nblacklist\n");
                        break;
                    }
                }

                else
                    break;
            }
            catch(IOException e)
            {
                System.err.println("not read name");
                status = false;
                noResponse = true;
            }
        }


        //judge the password
        while(status && pwdWrong < 3)						//user can try 3 times
        {
            try
            {
                outToClient.writeBytes("\nPlease enter password:\n");
                String pwd;

                socket.setSoTimeout(TIME_OUT * 60000);		//if long time no response, catch IOException
                pwd = inFromClient.readLine();
                if (!pwd.equals(Server.nameToPassword.get(name)))
                {
                    outToClient.writeBytes("password invalid\n");
                    pwdWrong++;
                }
                else
                {
                    outToClient.writeBytes("\nLogin successful!\nWelcome " + name + "\n\n");
                    System.out.println(name + "  login");

                    //add the user to online list
                    Server.nameToThread.put(name, this);
                    Server.nameToLoginTime.put(name, System.currentTimeMillis());

                    //send login info to other online clients
                    for(String other: Server.nameToThread.keySet())
                    {
                        if (!other.equals(name)) {
                            Server.nameToThread.get(other).outToClient.writeBytes("Welcome: " + name + "\n");
                        }
                    }
                    break;
                }
            }
            catch(IOException e)
            {
                System.err.println("not read password");
                status = false;
                noResponse = true;
            }

            //if input wrong password for 3 times, put the user into the blacklist for 60 seconds
            if(pwdWrong == 3)
            {
                Server.blacklistAddress.put(name, socket.getInetAddress());
                Server.blacklistTime.put(name, System.currentTimeMillis());

                try
                {
                    outToClient.writeBytes("You failed 3 times! Please wait for " + BLOCK_TIME + " seconds!\n");
                    //outToClient.writeBytes(Server.username.contains(name) + "\n");
                    //socket.close();
                }
                catch(IOException e)
                {
                    System.err.println("password failed");
                }
                status = false;
            }

        }
        return name;
    }

    //deal with client's commands
    public void command(String name)
    {
        String clientCommand;
        String[] line;
        String message;

        //send offline message
        if(Server.offlineMessage.containsKey(name))
        {
            int messageCt = Server.offlineMessage.get(name).size();
            List<String[]> messages = Server.offlineMessage.get(name);
            for(int j = 0; j < messageCt; j++)
            {
                try
                {
                    outToClient.writeBytes("offline message:\n" + messages.get(j)[0]
                            + ": "+ messages.get(j)[1] + "\n");
                }
                catch(IOException e)
                {
                    System.err.println("error: offline message");
                }
            }
            Server.offlineMessage.remove(name);
        }

        while(status)
        {

            try
            {
                outToClient.writeBytes("\nPlease enter command:\n");
                socket.setSoTimeout(TIME_OUT * 60000);			//if long time no response, catch IOException
                clientCommand = inFromClient.readLine();
                if(isClientClose(socket) == true)
                    break;

                //function: whoelse
                if(clientCommand.equals("whoelse"))
                {
                    for(String other: Server.nameToThread.keySet())
                    {
                        if(!other.equals(name))
                            outToClient.writeBytes(other + "\n");
                    }
                }
                //function: wholast time
                else if(clientCommand.startsWith("wholast"))
                {
                    int time_minute;
                    line = clientCommand.split(" ");
                    time_minute = Integer.parseInt(line[1]);
                    if(time_minute > 60 || time_minute < 0)
                        outToClient.writeBytes("Number invalid!\n");
                    else
                    {
                        for (String other: Server.nameToLoginTime.keySet())
                        {
                            double time_last = (System.currentTimeMillis() - Server.nameToLoginTime.get(other)) / 60000;
                            if((time_last - (double)time_minute) < 0)
                            {
                                outToClient.writeBytes(other + "  time: " + time_last + "  minutes\n");
                            }
                        }
                    }
                }
                //function: broadcast to all online clients
                else if(clientCommand.startsWith("broadcast message "))
                {
                    message = clientCommand.substring(18);
                    for(String other: Server.nameToThread.keySet())
                    {
                        if(!other.equals(name))
                            Server.nameToThread.get(other).outToClient.writeBytes("\n" + name + ": "+ message + "\n");
                    }
                }
                //function: broadcast to certain clients
                else if(clientCommand.startsWith("broadcast user "))
                {
                    line = clientCommand.split(" ");
                    //System.out.println(line.length + "\n");
                    int userNum = -1;
                    //get the number of users
                    for(int i = 2; i < line.length; i++)
                    {
                        if(line[i].equals("message"))
                        {
                            userNum = i;
                            break;
                        }
                    }
                    //System.out.println(userNum + "\n");
                    if((userNum == -1) || (userNum == 2))
                    {
                        outToClient.writeBytes("command invalid");
                    }
                    else
                    {
                        message = clientCommand.substring(clientCommand.indexOf("message") + 8);
                        for(int i = 2; i < userNum; i++)
                        {
                            if (!Server.nameToThread.containsKey(line[i]))
                            {
                                //add offline message
                                outToClient.writeBytes("user<" + line[i] + "> offline\n");
                                outToClient.writeBytes("Message will be received after the user is online\n");
                                if (!Server.offlineMessage.containsKey(line[i]))
                                    Server.offlineMessage.put(line[i], new ArrayList<>());
                                Server.offlineMessage.get(line[i]).add(new String[] {name, message});
                            }
                            else
                            {
                                Server.nameToThread.get(line[i]).outToClient.writeBytes(name + ": " + message + "\n");
                            }
                        }
                    }
                }
                //function: message to certain client
                else if(clientCommand.startsWith("message "))
                {
                    line = clientCommand.split(" ");
                    message = clientCommand.substring(clientCommand.indexOf(line[2]));

                    if (!Server.nameToThread.containsKey(line[1]))
                    {
                        outToClient.writeBytes("user<" + line[1] + "> offline\n");
                        outToClient.writeBytes("Message will be received after the user is online\n");
                        if (!Server.offlineMessage.containsKey(line[1]))
                            Server.offlineMessage.put(line[1], new ArrayList<>());
                        Server.offlineMessage.get(line[1]).add(new String[] {name, message});
                    }
                    else
                    {
                        Server.nameToThread.get(line[1]).outToClient.writeBytes(name + ": " + message + "\n");
                    }

                }
                //function: logout
                else if(clientCommand.equals("logout"))
                    break;
                else
                    outToClient.writeBytes("Command invalid!\n");
            }
            catch(IOException e)
            {
                System.err.println("command wrong!");
                status = false;

                noResponse = true;
            }
        }

        try
        {
            //remove from online list
            Server.nameToThread.remove(name);
            Server.nameToLoginTime.remove(name);

            //send notification to other active users
            for(String other: Server.nameToThread.keySet())
            {
                Server.nameToThread.get(other).outToClient.writeBytes("\n" + name + " log out\n");
            }
            System.out.println(name + " logout");

            if(noResponse)
            {
                outToClient.writeBytes("no response, logout");
            }

            //close socket
            inFromClient.close();
            outToClient.close();
            socket.close();
        }
        catch(IOException e)
        {
            System.err.println("socket close error");
        }
    }

    //whether connected
    public Boolean isClientClose(Socket socket)
    {
        try
        {
            socket.sendUrgentData(0);
            return false;
        }
        catch(Exception se)
        {
            return true;
        }
    }

}