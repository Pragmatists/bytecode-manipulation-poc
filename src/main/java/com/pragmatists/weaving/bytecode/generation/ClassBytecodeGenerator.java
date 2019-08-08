package com.pragmatists.weaving.bytecode.generation;

import lombok.Builder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.List;

@Builder
public class ClassBytecodeGenerator {
    private final ClassCharacteristic characteristic;
    private final List<MethodGenerator> methodGenerators;

    public ClassBytecodeGenerator(ClassCharacteristic characteristic, List<MethodGenerator> methodGenerators) {
        this.characteristic = characteristic;
        this.methodGenerators = methodGenerators;
    }

    public byte[] generate(ClassWriter classWriter) {
        PrintWriter printWriter = new PrintWriter(System.out);
        // TODO make tracing configurable
        TraceClassVisitor tracingVisitor = new TraceClassVisitor(classWriter, printWriter);
        CheckClassAdapter classVisitor = new CheckClassAdapter(tracingVisitor, false);

        classVisitor.visit(characteristic.getJavaVersion(), characteristic.getAccessFlag(), characteristic.getInternalName(), characteristic.getSignature(), characteristic.getSuperInternalName(), characteristic.getInterfaces());
        methodGenerators.forEach(methodGenerator -> methodGenerator.accept(classVisitor));
        classVisitor.visitEnd();

        return classWriter.toByteArray();
    }
}