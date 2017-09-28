JFLAGS = -g
JCC = javac

default: client.class server.class packet.class regulator.java 

packet.class: packet.java
	$(JCC) $(JFLAGS) packet.java

regulator.class: regulator.java
	$(JCC) $(JFLAGS) regulator.java

client.class: client.java
	$(JCC) $(JFLAGS) client.java

server.class: server.java
	$(JCC) $(JFLAGS) server.java

clean: 
	$(RM) *.class output.txt
