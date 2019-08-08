package com.pragmatists.weaving.bytecode.generation;

import com.pragmatists.weaving.bytecode.Instructions;
import com.pragmatists.weaving.utils.Types;
import lombok.Builder;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.PrintStream;
import java.util.function.Consumer;

import static com.pragmatists.weaving.bytecode.generation.Identifiers.CONSTRUCTOR_METHOD_NAME;
import static com.pragmatists.weaving.bytecode.generation.MethodCharacteristic.DEFAULT_CONSTRUCTOR_CHARACTERISTIC;
import static com.pragmatists.weaving.utils.Types.internalName;
import static com.pragmatists.weaving.utils.Types.methodDescriptor;
import static org.objectweb.asm.Opcodes.*;

@Builder
public class MethodGenerator {
    public static final MethodGenerator DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS =
            new MethodGenerator(DEFAULT_CONSTRUCTOR_CHARACTERISTIC, MethodGenerator::defaultConstructorBytecode);

    private static final int AUTO_CALCULATE = 0;
    private static final String SYSTEM_OUT_FIELD_NAME = "out";
    private static final String PRINTLN_METHOD_NAME = "println";

    private final MethodCharacteristic characteristic;
    private final Consumer<MethodVisitor> methodBodyWriter;

    public static MethodGenerator of(Instructions instructions) {
        return MethodGenerator.builder()
                .characteristic(instructions.getMethodCharacteristic())
                .methodBodyWriter(instructions::appendMethodInstructions)
                .build();
    }

    public void accept(ClassVisitor classVisitor) {
        MethodVisitor methodVisitor = classVisitor
                .visitMethod(characteristic.getAccessFlag(), characteristic.getName(), characteristic.getDescriptor(), characteristic.getSignature(), characteristic.getExceptions());
        methodVisitor.visitCode();
        methodBodyWriter.accept(methodVisitor);
        methodVisitor.visitInsn(Types.correspondingReturnBytecode(characteristic.getReturnType()));
        methodVisitor.visitMaxs(AUTO_CALCULATE, AUTO_CALCULATE);
        methodVisitor.visitEnd();
    }

    public static void defaultConstructorBytecode(MethodVisitor constructorVisitor, Class superClass) {
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, internalName(superClass), CONSTRUCTOR_METHOD_NAME.getValue(), methodDescriptor(null), false);
    }

    private static void defaultConstructorBytecode(MethodVisitor constructorVisitor) {
        defaultConstructorBytecode(constructorVisitor, Object.class);
    }

    public static void soutBytecode(MethodVisitor methodVisitor, String message) {
        final Class<PrintStream> printStreamClass = PrintStream.class;
        methodVisitor.visitFieldInsn(GETSTATIC, internalName(System.class), SYSTEM_OUT_FIELD_NAME, Type.getDescriptor(printStreamClass));
        methodVisitor.visitLdcInsn(message);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, internalName(printStreamClass), PRINTLN_METHOD_NAME, methodDescriptor(null, String.class), false);
        methodVisitor.visitInsn(RETURN);
    }
}
