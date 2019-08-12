package com.pragmatists.manipulation.bytecode.modification;

import org.objectweb.asm.MethodVisitor;

/**
 * MethodVisitor that will be used by {@link InstructionsModifier} while visiting a
 * {@link org.objectweb.asm.ClassReader ClassReader} to modify methods instructions.
 */
public abstract class ModifyingMethodVisitor extends MethodVisitor {
    public ModifyingMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }
}
