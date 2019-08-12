package com.pragmatists.manipulation.bytecode.generation;

import com.pragmatists.manipulation.bytecode.AppendInstruction;
import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import com.pragmatists.manipulation.type.Types;
import lombok.Builder;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.PrintStream;

import static com.pragmatists.manipulation.type.Types.internalName;
import static com.pragmatists.manipulation.type.Types.methodDescriptor;
import static java.util.Collections.emptyList;
import static org.objectweb.asm.Opcodes.*;

public class MethodGenerator {
    private static final String CONSTRUCTOR_METHOD_NAME = "<init>";
    private static final MethodCharacteristic DEFAULT_CONSTRUCTOR_CHARACTERISTIC =
            new MethodCharacteristic(ACC_PUBLIC, CONSTRUCTOR_METHOD_NAME, emptyList(), null);
    public static final MethodGenerator DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS =
            new MethodGenerator(DEFAULT_CONSTRUCTOR_CHARACTERISTIC, MethodGenerator::defaultConstructorInstructions);

    private static final int AUTO_CALCULATE = 0;
    private static final String SYSTEM_OUT_FIELD_NAME = "out";
    private static final String PRINTLN_METHOD_NAME = "println";

    private final MethodCharacteristic mc;
    private final AppendInstruction appendInstruction;

    @Builder
    private MethodGenerator(MethodCharacteristic methodCharacteristic, AppendInstruction methodBodyWriter) {
        this.mc = methodCharacteristic;
        this.appendInstruction = methodBodyWriter;
    }

    public static MethodGenerator from(Instructions instructions) {
        return MethodGenerator.builder()
                .methodCharacteristic(instructions.getMethodCharacteristic())
                .methodBodyWriter(instructions::appendMethodInstructions)
                .build();
    }

    public void accept(ClassVisitor classVisitor) {
        MethodVisitor methodVisitor = classVisitor
                .visitMethod(mc.getAccessFlag(), mc.getName(), mc.getDescriptor(), mc.getSignature(), mc.getExceptions());

        methodVisitor.visitCode();

        appendInstruction.accept(methodVisitor);

        methodVisitor.visitInsn(Types.correspondingReturnBytecode(mc.getReturnType()));
        methodVisitor.visitMaxs(AUTO_CALCULATE, AUTO_CALCULATE);
        methodVisitor.visitEnd();
    }

    public static void defaultConstructorInstructions(MethodVisitor constructorVisitor, Class superClass) {
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, internalName(superClass), CONSTRUCTOR_METHOD_NAME, methodDescriptor(null), false);
    }

    public static void soutPrintlnInstructions(MethodVisitor methodVisitor, String message) {
        Class<PrintStream> printStreamClass = PrintStream.class;
        methodVisitor.visitFieldInsn(GETSTATIC, internalName(System.class), SYSTEM_OUT_FIELD_NAME, Type.getDescriptor(printStreamClass));
        methodVisitor.visitLdcInsn(message);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, internalName(printStreamClass), PRINTLN_METHOD_NAME, methodDescriptor(null, String.class), false);
    }

    private static void defaultConstructorInstructions(MethodVisitor constructorVisitor) {
        defaultConstructorInstructions(constructorVisitor, Object.class);
    }
}
