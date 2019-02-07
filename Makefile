CC = gcc

all : fib.s
    $(CC) -c -o fib.o fib.s
