	.arch armv7-a
	.text
	.global main
main:
	push {fp, lr}
	str fp, [sp, #-4]! \\ need this? 
	mov fp, sp
	sub sp, sp, #52
	mov r2, #10
	str r2, [fp, #-4]
	mov r2, #200
	str r2, [fp, #-8]
	ldr r2, [fp, #-4]
	str r2, [fp, #-12]
	ldr r1, [fp, #-8]
	str r1, [fp, #-16]
	add r2, r1, r2
	str r0, [fp, #-20]
	ARMInstruction evalSet(): Unsupported Set Signature!
	ldr r2, [fp, #-4]
	str r2, [fp, #-36]
	ldr r1, [fp, #-8]
	str r1, [fp, #-40]
	add r2, r1, r2
	str r0, [fp, #-44]
	ARMInstruction evalSet(): Unsupported Set Signature!
	ARMInstruction evalSet(): Unsupported Set Signature!
	ldr fp, [sp], #4
	bx lr
	mov sp, fp
	pop {fp, pc}
