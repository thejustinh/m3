echo "COMPILING ALL JAVA FILES"
javac *.java
echo "RUNNING GenerateAssembly PROGRAM"
java GenerateAssembly fib/fib.c.234r.expand
