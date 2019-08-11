package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 * PrependingMethodVisitor injects instructions at the method's beginning, before any other instructions present at the
 * moment of visitation.
 */
public class PrependingMethodVisitor extends ModifyingMethodVisitor {
    private final MethodVisitor originalMethodVisitor;
    private final Instructions instructions;

    public PrependingMethodVisitor(MethodVisitor mv, Instructions instructions) {
        super(ASM7, mv);
        this.originalMethodVisitor = mv;
        this.instructions = instructions;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        instructions.appendMethodInstructions(originalMethodVisitor);
    }
}
