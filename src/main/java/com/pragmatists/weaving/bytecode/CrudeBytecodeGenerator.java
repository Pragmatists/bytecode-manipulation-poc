package com.pragmatists.weaving.bytecode;

import com.pragmatists.weaving.classes.ClassNameConverter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.*;

public class CrudeBytecodeGenerator implements BytecodeGenerator {
    private final ClassNameConverter namesConverter = new ClassNameConverter(); // TOD di
    private final ConstructorGenerator constructorGenerator = new ConstructorGenerator(); // TOD di

    @Override
    public byte[] generateClass(String name) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, printWriter);
        CheckClassAdapter classVisitor = new CheckClassAdapter(tracingVisitor, false);

        classVisitor.visit(V11, ACC_PUBLIC, namesConverter.binaryToInternal(name), null, namesConverter.binaryToInternal(Object.class.getName()), new String[] { "testtypes/Clazz" });
        MethodVisitor constructorVisitor = classVisitor.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

        constructorVisitor.visitCode();
        constructorGenerator.defaultConstructor(constructorVisitor);

        MethodVisitor methodVisitor = classVisitor.visitMethod(ACC_PUBLIC, "getClassId", "()Ljava/lang/String;", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitLdcInsn("target");
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();

        MethodVisitor anotherMethodVisitor = classVisitor.visitMethod(ACC_PUBLIC, "getClassId2", "()Ljava/lang/String;", null, null);
        anotherMethodVisitor.visitCode();
        anotherMethodVisitor.visitLdcInsn("target2");
        anotherMethodVisitor.visitInsn(ARETURN);
        anotherMethodVisitor.visitMaxs(0, 0);
        anotherMethodVisitor.visitEnd();

        SoutMethodGenerator soutMethodGenerator = new SoutMethodGenerator();

        MethodVisitor mv = classVisitor.visitMethod(ACC_PUBLIC, "print", "()V", null, null);
        soutMethodGenerator.sout(mv, "Hello World!");

        classVisitor.visitEnd();

        return classWriter.toByteArray();
    }
}
