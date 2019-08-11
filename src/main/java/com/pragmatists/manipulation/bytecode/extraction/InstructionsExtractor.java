package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import com.pragmatists.manipulation.config.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * MethodInstructionExtractor uses {@link ExtractingMethodVisitor} instances for visiting methods. By contract these
 * ExtractingMethodVisitors collect desired instructions as {@link Instructions}.
 * <p>
 * CAVEAT: Currently the default ExtractingMethodVisitor implementation used by MethodInstructionExtractor is
 * {@link LinearExtractingMethodVisitor} which is limited almost to only methods with single return opcode.
 */
public class InstructionsExtractor extends ClassVisitor {
    private final String methodName;
    private final String methodDescriptor;
    private final Function<byte[], ClassReader> classReaderProvider;
    private final Function<InstructionsExtractor, ClassVisitor> readerVisitorProvider;
    private final ExtractingMethodVisitorProvider methodVisitorProvider;

    private ExtractingMethodVisitor extractingMethodVisitor;


    /**
     * The instance provided by this constructor is suitable when methodName argument value is a unique method name within
     * the class. If there are more methods with this name, the extracted instructions depend of the order of visitation
     * of the methods by the {@link ClassVisitor} and are thus undetermined.
     *
     * @param methodName the name (should be unique within the class) of a method whose instructions are to be extracted
     */
    public InstructionsExtractor(String methodName) {
        this(methodName, null);
    }

    /**
     * @param methodName       the name of a method whose instructions are to be extracted
     * @param methodDescriptor the descriptor of that method
     */
    public InstructionsExtractor(String methodName, String methodDescriptor) {
        this(methodName, methodDescriptor, null);
    }

    /**
     * @param methodName       the name of a method whose instructions are to be extracted
     * @param methodDescriptor the descriptor of that method
     * @param classVisitor     a ClassVisitor to visit this InstructionsExtractor, e.g. for logging
     */
    public InstructionsExtractor(String methodName,
                                 String methodDescriptor,
                                 ClassVisitor classVisitor) {
        this(methodName, methodDescriptor, classVisitor, v -> v, LinearExtractingMethodVisitor::of, ClassReader::new);
    }

    /**
     * @param methodName            the name of a method whose instructions are to be extracted
     * @param methodDescriptor      the descriptor of that method
     * @param classVisitor          a ClassVisitor to visit this InstructionsExtractor, e.g. for logging
     * @param readerVisitorProvider an optional visitor to visit the ClassReader and be visited by
     *                              InstructionExtractor, e.g. for logging
     * @param methodVisitorProvider function providing an {@link ExtractingMethodVisitor} given
     *                              {@link MethodCharacteristic} of the currently visited method
     * @param classReaderProvider   function providing a {@link ClassReader} instance that will accept this
     */
    public InstructionsExtractor(String methodName,
                                 String methodDescriptor,
                                 ClassVisitor classVisitor,
                                 Function<InstructionsExtractor, ClassVisitor> readerVisitorProvider,
                                 ExtractingMethodVisitorProvider methodVisitorProvider,
                                 Function<byte[], ClassReader> classReaderProvider) {
        super(Config.ASM_VERSION, classVisitor);
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.methodDescriptor = methodDescriptor;
        this.methodName = methodName;
        this.readerVisitorProvider = readerVisitorProvider;
        this.classReaderProvider = classReaderProvider;
        this.methodVisitorProvider = methodVisitorProvider;
    }

    /**
     * @param classBytecode bytecode of a class
     * @return instructions collected during last visitation of the bytecode
     */
    public Optional<Instructions> extract(byte[] classBytecode) {
        ClassReader classReader = classReaderProvider.apply(classBytecode);
        ClassVisitor visitor = readerVisitorProvider.apply(this);
        classReader.accept(visitor, 0);

        if (extractingMethodVisitor != null) {
            return Optional.of(extractingMethodVisitor.getInstructions());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!methodMatches(name, descriptor)) {
            return null;
        }

        MethodCharacteristic characteristic = new MethodCharacteristic(access, name, descriptor);
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        extractingMethodVisitor = methodVisitorProvider.apply(methodVisitor, characteristic);

        return extractingMethodVisitor;
    }

    private boolean methodMatches(String name, String descriptor) {
        return methodName.equals(name) && (methodDescriptor == null || methodDescriptor.equals(descriptor));
    }
}
