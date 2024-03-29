package com.pragmatists.manipulation.bytecode.generation;

import com.pragmatists.manipulation.bytecode.characteristics.ClassCharacteristic;
import lombok.Builder;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.List;

/**
 * ClassBytecodeGenerator generates Java bytecode based on {@link ClassCharacteristic} and {@link MethodGenerator}s fields.
 */
public class ClassBytecodeGenerator {
    private final ClassCharacteristic characteristic;
    private final List<MethodGenerator> methodGenerators;

    @Builder
    public ClassBytecodeGenerator(ClassCharacteristic characteristic, List<MethodGenerator> methodGenerators) {
        this.characteristic = characteristic;
        this.methodGenerators = methodGenerators;
    }

    public byte[] generate(ClassWriter classWriter) {
        PrintWriter printWriter = new PrintWriter(System.out);
        ClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, printWriter);
        ClassVisitor checkClassAdapter = new CheckClassAdapter(tracingVisitor, false);

        checkClassAdapter.visit(characteristic.getJavaVersion(), characteristic.getAccessFlag(), characteristic.getInternalName(), characteristic.getSignature(), characteristic.getSuperInternalName(), characteristic.getInterfaces());
        methodGenerators.forEach(methodGenerator -> methodGenerator.accept(checkClassAdapter));
        checkClassAdapter.visitEnd();

        return classWriter.toByteArray();
    }
}
