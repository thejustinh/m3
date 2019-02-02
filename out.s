	.arch armv7-a
	.text
	.global main
main:
	push {fp, lr}
	mov fp, sp
	sub sp, sp #Calculated Later
