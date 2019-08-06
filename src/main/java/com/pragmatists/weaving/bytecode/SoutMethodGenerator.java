package com.pragmatists.weaving.bytecode;

import com.pragmatists.weaving.classes.ClassNameConverter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SoutMethodGenerator {
    private final ClassNameConverter nameConverter = new ClassNameConverter(); // todo di1

    void sout(MethodVisitor methodVisitor, String message) {
        methodVisitor.visitCode();

        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn(message);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        methodVisitor.visitInsn(RETURN);

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }
}
