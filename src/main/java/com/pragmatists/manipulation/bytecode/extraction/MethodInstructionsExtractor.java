package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.generation.MethodCharacteristic;
import com.pragmatists.manipulation.config.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * TODO
 */
public class MethodInstructionsExtractor extends ClassVisitor {
    private final String methodName;
    private final String methodDescriptor;
    private final Function<byte[], ClassReader> classReaderProvider;
    private final Function<MethodCharacteristic, ExtractingMethodVisitor> extractingMethodVisitorProvider;

    private ExtractingMethodVisitor extractingMethodVisitor;

    public MethodInstructionsExtractor(String methodName) {
        this(methodName, null);
    }

    public MethodInstructionsExtractor(String methodName, String methodDescriptor) {
        this(methodName, methodDescriptor, ClassReader::new, ExtractingMethodVisitor::of);
    }

    public MethodInstructionsExtractor(String methodName,
                                       Function<byte[], ClassReader> classReaderProvider,
                                       Function<MethodCharacteristic, ExtractingMethodVisitor> extractingMethodVisitorProvider) {
        this(methodName, null, classReaderProvider, extractingMethodVisitorProvider);
    }

    public MethodInstructionsExtractor(String methodName,
                                       String methodDescriptor,
                                       Function<byte[], ClassReader> classReaderProvider,
                                       Function<MethodCharacteristic, ExtractingMethodVisitor> extractingMethodVisitorProvider) {
        super(Config.ASM_VERSION);
        this.methodDescriptor = methodDescriptor;

        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.methodName = methodName;

        this.classReaderProvider = classReaderProvider;
        this.extractingMethodVisitorProvider = extractingMethodVisitorProvider;
    }

    /**
     * @return instructions collected during last visitation of the bytecode
     */
    public Optional<Instructions> extract(byte[] classBytecode) {
        ClassReader classReader = classReaderProvider.apply(classBytecode);
        classReader.accept(this, 0);

        if (extractingMethodVisitor != null) {
            return Optional.of(extractingMethodVisitor.getInstructions());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (methodMatches(name, descriptor)) {
            MethodCharacteristic characteristic = MethodCharacteristic.builder()
                    .accessFlag(access)
                    .name(name)
                    .descriptor(descriptor)
                    .build();

            extractingMethodVisitor = extractingMethodVisitorProvider.apply(characteristic);
            return extractingMethodVisitor;
        } else {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }

    private boolean methodMatches(String name, String descriptor) {
        return methodName.equals(name) && (methodDescriptor == null || methodDescriptor.equals(descriptor));
    }
}
