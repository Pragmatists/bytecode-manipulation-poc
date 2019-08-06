package com.pragmatists.weaving.bytecode;

import com.pragmatists.weaving.classes.ClassNameConverter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ConstructorGenerator {
    private final ClassNameConverter nameConverter = new ClassNameConverter(); // TODO di

    public void defaultConstructor(MethodVisitor constructorVisitor) {
        defaultConstructor(constructorVisitor, Object.class);
    }

    public void defaultConstructor(MethodVisitor constructorVisitor, Class superClass) {
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, nameConverter.binaryToInternal(superClass.getName()), "<init>", "()V", false);
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();
    }
}
