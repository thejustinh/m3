echo "REMOVING OLD CLASS FILES"
rm *.class
echo "COMPILING ALL JAVA FILES"
javac *.java
echo $'RUNNING GenerateAssembly PROGRAM\n-----'
java GenerateAssembly fib/fib.c.234r.expand fib.s
echo $'-----\nFINISHED'
