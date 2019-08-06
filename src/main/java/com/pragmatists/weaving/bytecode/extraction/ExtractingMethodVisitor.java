package com.pragmatists.weaving.bytecode.extraction;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.RETURN;

public class ExtractingMethodVisitor extends MethodVisitor {
    private List<Consumer<MethodVisitor>> operations = new ArrayList<>();
    private boolean pastReturn = false;
    
    public ExtractingMethodVisitor(int api) {
        super(api);
    }

    @Override
    public void visitInsn(int opcode) {
        pastReturn = opcode == RETURN || opcode == ARETURN;
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitInsn(opcode));

    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitIntInsn(opcode, operand));
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitVarInsn(opcode, var));
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitTypeInsn(opcode, type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitMethodInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        operations.add(mv -> mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitJumpInsn(opcode, label));
    }

    @Override
    public void visitLabel(Label label) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitLabel(label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitLdcInsn(value));
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitIincInsn(var, increment));
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitTableSwitchInsn(min, max, dflt, labels));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitLookupSwitchInsn(dflt, keys, labels));
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitTryCatchBlock(start, end, handler, type));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitLocalVariable(name, descriptor, signature, start, end, index));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitLineNumber(line, start));
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (pastReturn) {
            return;
        }

        operations.add(mv -> mv.visitMaxs(maxStack, maxLocals));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    public List<Consumer<MethodVisitor>> getOperations() {
        return operations;
    }
}
