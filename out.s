	.arch armv7-a
	.text
	.global main
main:
	push {fp, lr}
	mov fp, sp
	sub sp, sp, #52
	mov r2, #10
	str r2, [fp, #-4]
	mov r2, #200
	str r2, [fp, #-8]
	ldr r2, [fp, #-4]
	str r2, [fp, #-12]
	ldr r2, [fp, #-8]
	str r2, [fp, #-16]
	ldr r1, [fp, #-16]
	ldr r2, [fp, #-12]
	add r0, r1, r2
	str r0, [fp, #-20]
	ldr r2, [fp, #-20]
	str r2, [fp, #-24]
	bl printint
	ldr r2, [fp, #-4]
	str r2, [fp, #-36]
	ldr r2, [fp, #-8]
	str r2, [fp, #-40]
	ldr r1, [fp, #-40]
	ldr r2, [fp, #-36]
	add r0, r1, r2
	str r0, [fp, #-44]
	ldr r2, [fp, #-44]
	str r2, [fp, #-48]
	ldr r2, [fp, #-48]
	mov r0, r2
	mov sp, fp
	pop {fp, pc}
