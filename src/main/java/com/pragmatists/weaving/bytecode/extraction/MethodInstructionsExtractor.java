package com.pragmatists.weaving.bytecode.extraction;

import com.pragmatists.weaving.bytecode.Instructions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.function.Function;

import static com.pragmatists.weaving.config.Config.ASM_VERSION;


public class MethodInstructionsExtractor extends ClassVisitor {
    private final String methodName;
    private final Function<byte[], ClassReader> classReaderProvider;
    private final Function<String, ExtractingMethodVisitor> extractingMethodVisitorProvider;

    private ExtractingMethodVisitor extractingMethodVisitor;

    public MethodInstructionsExtractor(String methodName) {
        this(methodName, ClassReader::new, ExtractingMethodVisitor::forDescriptor);
    }

    public MethodInstructionsExtractor(String methodName,
                                       Function<byte[], ClassReader> classReaderProvider,
                                       Function<String, ExtractingMethodVisitor> extractingMethodVisitorProvider) {
        super(ASM_VERSION);

        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.methodName = methodName;

        this.classReaderProvider = classReaderProvider;
        this.extractingMethodVisitorProvider = extractingMethodVisitorProvider;
    }

    /**
     * @return instructions collected during last visitation of the bytecode
     */
    public Instructions extract(byte[] classBytecode) {
        ClassReader classReader = classReaderProvider.apply(classBytecode);
        classReader.accept(this, 0);

        return extractingMethodVisitor.getInstructions();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!methodName.equals(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        extractingMethodVisitor = extractingMethodVisitorProvider.apply(descriptor);
        return extractingMethodVisitor;
    }
}
