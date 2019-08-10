package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import com.pragmatists.manipulation.config.Config;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import static com.pragmatists.manipulation.type.Types.correspondingReturnBytecode;

/**
 * This is a basic {@link ExtractingMethodVisitor} that will collect all instructions until it encounters a return opcode
 * corresponding to the return type of the method being visited. The return opcode itself will not be collected.
 * <p>
 * There is an important limitation to the usefulness of this extractor, as for methods with multiple return
 * instructions, the extracted code will be incomplete.
 */
class LinearExtractingMethodVisitor extends ExtractingMethodVisitor {
    private final Instructions instructions = new Instructions();
    private final int returnOpcode;

    private boolean pastReturn = false;

    private LinearExtractingMethodVisitor(MethodCharacteristic methodCharacteristic, int returnOpcode) {
        super(Config.ASM_VERSION);
        this.returnOpcode = returnOpcode;
        instructions.setMethodCharacteristic(methodCharacteristic);
    }

    static LinearExtractingMethodVisitor of(MethodCharacteristic methodCharacteristic) {
        String descriptor = methodCharacteristic.getDescriptor();
        Type returnType = Type.getReturnType(descriptor);
        int returnOpcode = correspondingReturnBytecode(returnType);
        return new LinearExtractingMethodVisitor(methodCharacteristic, returnOpcode);
    }

    @Override
    Instructions getInstructions() {
        return instructions;
    }

    @Override
    public void visitInsn(int opcode) {
        pastReturn = opcode == returnOpcode;
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitInsn(opcode));
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitIntInsn(opcode, operand));
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitVarInsn(opcode, var));
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitTypeInsn(opcode, type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitMethodInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        instructions.collectInstruction(mv -> mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitJumpInsn(opcode, label));
    }

    @Override
    public void visitLabel(Label label) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitLabel(label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitLdcInsn(value));
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitIincInsn(var, increment));
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitTableSwitchInsn(min, max, dflt, labels));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitLookupSwitchInsn(dflt, keys, labels));
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitTryCatchBlock(start, end, handler, type));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitLocalVariable(name, descriptor, signature, start, end, index));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitLineNumber(line, start));
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (pastReturn) {
            return;
        }

        instructions.collectInstruction(mv -> mv.visitMaxs(maxStack, maxLocals));
    }
}
