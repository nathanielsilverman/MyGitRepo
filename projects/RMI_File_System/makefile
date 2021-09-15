JFLAGS = -g
JC = javac
J = java
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	DIR_Entry.java \
	Fat32File.java \
	fat32_reader.java \
	Fat32_driver.java \
	RMIServerInterface.java \
	RMIServer.java \
	ClientRequest.java \

default: classes

classes: $(CLASSES:.java=.class)

run:
	$(J) Fat32_driver fat32.img

clean:
	$(RM) *.class