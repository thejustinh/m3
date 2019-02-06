	.arch armv7-a
	.text
	.global main
main:
	push {fp, lr}
	str fp, [sp, #-4]! \\ need this? 
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
.L29:
	ldr r2, [fp, #-4]
	str r2, null
	ldr r1, [fp, #-8]
	str r1, null
	ARMInstruction evalSet(): Unsupported Set Signature!
	ldr r2, [fp, #-8]
	str r2, null
	ldr r1, [fp, #-28]
	str r1, null
	ldr r0, [fp, #-16]
	str r0, null
	add [fp, #-44], [fp, #-40], #1
.L27:
	ldr r, [fp, #-44]
	str r, null
	ARMInstruction evalSet(): Unsupported Set Signature!
	ldr r, [fp, #-28]
	str r, null
	ARMInstruction evalSet(): Unsupported Set Signature!
	ARMInstruction evalSet(): Unsupported Set Signature!
ARMInsn rtl2arm(): OPERATION NOT SUPPORTED
