all: fib awp

fib: fib.s
	./fib_gen.sh > a
	gcc -c -o fib.o fib.s
	gcc -o runFib fib.o

awp: awp.s printer.c
	./generate.sh > a
	gcc -c -o awp.o awp.s
	gcc -c -o printer.o printer.c
	gcc -o awp awp.o printer.o
