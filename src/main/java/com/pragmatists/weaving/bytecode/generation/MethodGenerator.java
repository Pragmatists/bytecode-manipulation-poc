package com.pragmatists.weaving.bytecode.generation;

import com.pragmatists.weaving.utils.ClassNames;
import com.pragmatists.weaving.utils.Types;
import lombok.Builder;
import lombok.Value;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.PrintStream;
import java.util.function.Consumer;

import static com.pragmatists.weaving.bytecode.generation.Identifiers.CONSTRUCTOR_METHOD_NAME;
import static com.pragmatists.weaving.utils.ClassNames.binaryToInternal;
import static org.objectweb.asm.Opcodes.*;

@Value
@Builder
public class MethodGenerator {
    public static final MethodGenerator DEFAULT_CONSTRUCTOR_OF_OBJECT_SUBCLASS = MethodGenerator.builder()
            .characteristic(MethodCharacteristic.DEFAULT_CONSTRUCTOR_CHARACTERISTIC)
            .methodBody(MethodGenerator::defaultConstructorBytecode)
            .build();

    private static final int AUTO_CALCULATE = 0;
    private static final String SYSTEM_OUT_FIELD_NAME = "out";
    private static final String PRINTLN_METHOD_NAME = "println";

    private final MethodCharacteristic characteristic;
    private final Consumer<MethodVisitor> methodBody;

    public void accept(ClassVisitor classVisitor) {
        MethodVisitor methodVisitor = classVisitor
                .visitMethod(characteristic.getAccessFlag(), characteristic.getName(), characteristic.getDescriptor(), characteristic.getSignature(), characteristic.getExceptions());
        methodVisitor.visitCode();
        methodBody.accept(methodVisitor);
        methodVisitor.visitInsn(Types.correspondingReturnBytecode(characteristic.getReturnType()));
        methodVisitor.visitMaxs(AUTO_CALCULATE, AUTO_CALCULATE);
        methodVisitor.visitEnd();
    }

    private static void defaultConstructorBytecode(MethodVisitor constructorVisitor) {
        defaultConstructorBytecode(constructorVisitor, Object.class);
    }

    public static void defaultConstructorBytecode(MethodVisitor constructorVisitor, Class superClass) {
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, binaryToInternal(superClass.getName()), CONSTRUCTOR_METHOD_NAME.getValue(), "()V", false);
    }

    public static void soutBytecode(MethodVisitor methodVisitor, String message) {
        final Class<PrintStream> printStreamClass = PrintStream.class;
        methodVisitor.visitFieldInsn(GETSTATIC, ClassNames.binaryToInternal(System.class.getName()), SYSTEM_OUT_FIELD_NAME, Type.getDescriptor(printStreamClass));
        methodVisitor.visitLdcInsn(message);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, ClassNames.binaryToInternal(printStreamClass.getName()), PRINTLN_METHOD_NAME, "(Ljava/lang/String;)V", false);
        methodVisitor.visitInsn(RETURN);
    }
}
