
all: Client.class ClientThread.class Server.class ServerThread.class

Client.class: Client.java
	javac Client.java

ClientThread.class: ClientThread.java
	javac ClientThread.java

Server.class: Server.java
	javac Server.java

ServerThread.class: ServerThread.java
	javac ServerThread.java
	
.PHONY: clean
clean: 
	rm -f *.class
