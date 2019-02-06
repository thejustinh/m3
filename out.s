	.arch armv7-a
	.text
	.global main
main:
	push {fp, lr}
	mov fp, sp
	sub sp, sp, #60
	mov r2, #0
	str r2, [fp, #-4]
	mov r2, #1
	str r2, [fp, #-8]
	mov r2, #0
	str r2, [fp, #-12]
	mov r2, #0
	str r2, [fp, #-16]
	b .L27
.L29:
	ldr r2, [fp, #-4]
	str r2, [fp, #-20]
	ldr r2, [fp, #-8]
	str r2, [fp, #-24]
	ldr r1, [fp, #-24]
	ldr r2, [fp, #-20]
	add r0, r1, r2
	str r0, [fp, #-28]
	ldr r2, [fp, #-8]
	str r2, [fp, #-32]
	ldr r2, [fp, #-28]
	str r2, [fp, #-36]
	ldr r2, [fp, #-16]
	str r2, [fp, #-40]
	add r2, r2, #1
.L27:
	ldr r2, [fp, #-44]
	str r2, [fp, #-48]
	cmp r2, #9
	ble .L29
	ldr r2, [fp, #-28]
	str r2, [fp, #-52]
	ldr r2, [fp, #-52]
	str r2, [fp, #-56]
	ldr r2, [fp, #-56]
	mov r0, r2
	mov sp, fp
	pop {fp, pc}
