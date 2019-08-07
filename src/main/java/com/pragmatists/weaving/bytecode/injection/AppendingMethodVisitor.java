package com.pragmatists.weaving.bytecode.injection;

import com.pragmatists.weaving.bytecode.Instructions;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class AppendingMethodVisitor extends MethodVisitor {
    private final MethodVisitor originalMethodVisitor;
    private final Instructions instructions;

    public AppendingMethodVisitor(MethodVisitor mv, Instructions instructions) {
        super(ASM7, mv);
        this.originalMethodVisitor = mv;
        this.instructions = instructions;
    }

    @Override
    public void visitInsn(int opcode) {
        if (isReturnOpcode(opcode)) {
            instructions.appendMethodInstructions(originalMethodVisitor);
        }

        originalMethodVisitor.visitInsn(opcode);
        originalMethodVisitor.visitEnd();
    }

    private boolean isReturnOpcode(int opcode) {
        return opcode >= IRETURN && opcode <= RETURN;
    }
}
