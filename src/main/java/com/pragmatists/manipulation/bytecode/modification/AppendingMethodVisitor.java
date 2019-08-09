package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.config.Config;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

public class AppendingMethodVisitor extends MethodVisitor {
    private final MethodVisitor originalMethodVisitor;
    private final Instructions instructions;

    public AppendingMethodVisitor(MethodVisitor mv, Instructions instructions) {
        super(Config.ASM_VERSION, mv);
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
