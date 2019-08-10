package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.Instructions;
import org.objectweb.asm.MethodVisitor;


/**
 * A MethodVisitor that will extract bytecode instructions and collect them as {@link Instructions}.
 * When ExtractingMethodVisitor visits an instruction that it wants to extract, it should call
 * {@link Instructions#collectInstruction}, e.g.:
 * <pre>{@code
 *      public void visitInsn(int opcode) {
 *             instructions.collectInstruction(mv -> mv.visitInsn(opcode));
 *      }
 * }</pre>
 * This way the instructions can be later visited by a different MethodVisitor, e.g. a one called by a
 * {@link org.objectweb.asm.ClassWriter ClassWriter}.
 */
public abstract class ExtractingMethodVisitor extends MethodVisitor {
    public ExtractingMethodVisitor(int api) {
        super(api);
    }

    public ExtractingMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    abstract Instructions getInstructions();
}
