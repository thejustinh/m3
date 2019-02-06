gcc -c -o awp.o awp.s
gcc -c -o printer.o printer.c
gcc -o awp awp.o printer.o
./awp
