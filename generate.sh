echo "REMOVING OLD CLASS FILES"
rm *.class
echo "COMPILING ALL JAVA FILES"
javac *.java
echo $'RUNNING GenerateAssembly PROGRAM\n-----'
#java GenerateAssembly fib/fib.c.234r.expand out.s
java GenerateAssembly addWithPrint/addwithprint.c.212r.expand awp.s
echo $'-----\nFINISHED'
