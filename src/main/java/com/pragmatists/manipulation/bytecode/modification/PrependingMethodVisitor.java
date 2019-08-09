package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

public class PrependingMethodVisitor extends MethodVisitor {
    private final MethodVisitor originalMethodVisitor;
    private final Instructions instructions;

    public PrependingMethodVisitor(MethodVisitor mv, Instructions instructions) {
        super(ASM7, mv);
        this.originalMethodVisitor = mv;
        this.instructions = instructions;
    }

    @Override
    public void visitCode() {
        originalMethodVisitor.visitCode();
        instructions.appendMethodInstructions(originalMethodVisitor);
    }
}
