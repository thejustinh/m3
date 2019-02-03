	.arch armv7-a
	.text
	.global main
main:
	push {fp, lr}
	mov fp, sp
	sub sp, sp #Calculated Later
	mov r4, #0
	str r4, [fp, #-0]
	mov r4, #1
	str r4, [fp, #-4]
	mov r4, #0
	str r4, [fp, #-8]
	mov r4, #0
	str r4, [fp, #-12]
	ldr r4, [fp, #-0]
	ldr r4, [fp, #-4]
	add [fp, #-24], [fp, #-16], [fp, #-20]
	ldr r4, [fp, #-4]
	ldr r4, [fp, #-24]
	ldr r4, [fp, #-12]
	add [fp, #-40], [fp, #-36], #1
	ldr r4, [fp, #-40]
	ARMInstruction evalSet(): Unsupported Set Signature!	ldr r4, [fp, #-24]
	ARMInstruction evalSet(): Unsupported Set Signature!	ARMInstruction evalSet(): Unsupported Set Signature!ARMInsn rtl2arm(): OPERATION NOT SUPPORTED