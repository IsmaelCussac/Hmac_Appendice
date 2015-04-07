all: compiler

compiler: 
	javac HMac.java
	javac Appendice.java

clean:
	rm fauxOut.pdf
	rm NotesOut.pdf
	rm HMac.class
	rm Appendice.class

