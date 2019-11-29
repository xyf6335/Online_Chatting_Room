
### Program Description

	This program implements a simple chatroom based on client server model over TCP connections.
	Using the terminal, a server can be setup. And user can log in the server with the correct username and password.
	Once logged in, users can send messages to each other.

	
### Development Environment
	- Java 	Version 12
	- OS X 	Version 10.15.1
	- IntelliJ
	- Terminal


### Files Description

	- Server.java
		This part of the code is to setup the server of the chatroom. 
		It can read the user_pass.txt file to set up a valid user database. Then it opens a server socket to listen for
		clients. Once the connection is established, a client socket is opened at the server side. A new thread begins
		to run while the server's main function keeps on listening.

	- ServerThread.java
		In the thread, the program will judge whether a user in valid or not. And it will deal with all the commands
		used by users. When the user logs out, the thread is closed.
	
	- Client.java
		This part of the code is the client side. It try to connect to the server. Once accepted, a thread is created.
		The client's main function read from the user and send it to the server.

	- ClientThread.java
		The thread keeps get server's messages and print out. When the user logs out, the thread will close and the
		main function exits.
	
	- user_pass.txt
		Valid usernames and corresponding passwords.

	- Makefile
		Compile all the .java files.

	- README.txt
		A simple Program description and instruction.


### Instructions to Compile and Run

	1. Change the current directory 
		$ cd ~/xx/xx/

	2. compile the .java files
		$ make
		(use "$ make clean" can remove all the .class files)

	3. invoke server program
		$ java Server <port>
		e.g. $java Server 5555

	4. invoke client program
		$ java Client <address> <port>
		e.g. $ java Client localhost 5555


### Functions of the Chatroom
	
	1. login
		Enter the username and password. If they match the database, user will log in. 
		If the password is incorrectly input for three times, user with current IP address will be put in the blacklist
		for a while, which is called block time.
		-- The block time can be changed in ServerThread.java, line 18.

	2. whoelse
		Displays name of other connected users.

	3. wholast <number>
		Displays name of those users connected within the last <number> minutes. Let 0 < number < 60.
		-- the duration of online time will show for every active user
		e.g. 	wholast 10		
				Show the users connected within the last 10 minutes.

	4. broadcast message <message>
		Broadcasts <message> to all connected users.
		e.g.	broadcast message hello world		
				Broadcast "hello world"	to all connected users.

	5. broadcast user <user> <user> ... <user> message <message>
		Broadcasts <message> to the list of users
		e.g. 	broadcast Northeastern message "hello world" 		
				Broadcast "hello world"	to users in Northeastern.

	6. message <user> <message>
		Private <message> to a <user>
		e.g. 	message Alex hello world		
				Send "hello world" to Alex.

	7. inactive
		If the user is inactive for a while, it will logout automatically.
		--The time can be changed in ServerThread.java line 19.

	8. logout
		Log out this user. Send information to other online clients and remove it from online list.

	9. offline message
		When user uses function5 (broadcast user <user> <user> ... <user> message <message>) or
		function6 (message <user> <message>), situation may happen that the <user> is not online.
		In this situation, the message will be stored. And it will be sent to the <user> once his is online.




