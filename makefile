# Compile to to .class files
# These are not directly executable, but they are the compiled Java bytecode 
# that can be run on the Java Virtual Machine (JVM).
compile:
	javac -d bin src/main/java/concurrency/synchronization/*.java

counter:
	java -cp bin synchronization.Counter 2
