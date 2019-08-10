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
    private final ExtractingMethodVisitorProvider extractingMethodVisitorProvider;

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
        this(methodName, methodDescriptor, ClassReader::new, LinearExtractingMethodVisitor::of);
    }

    /**
     * @param methodName                      the name of a method whose instructions are to be extracted
     * @param methodDescriptor                the descriptor of that method
     * @param classReaderProvider             function providing a {@link ClassReader} instance that will accept this
     *                                        visitor for extraction of instructions
     * @param extractingMethodVisitorProvider function providing an {@link ExtractingMethodVisitor} given
     *                                        {@link MethodCharacteristic} of the currently visited method
     */
    public InstructionsExtractor(String methodName,
                                 String methodDescriptor,
                                 Function<byte[], ClassReader> classReaderProvider,
                                 ExtractingMethodVisitorProvider extractingMethodVisitorProvider) {
        super(Config.ASM_VERSION);
        this.methodDescriptor = methodDescriptor;

        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.methodName = methodName;

        this.classReaderProvider = classReaderProvider;
        this.extractingMethodVisitorProvider = extractingMethodVisitorProvider;
    }

    /**
     * @param classBytecode bytecode of a class
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
            MethodCharacteristic characteristic = new MethodCharacteristic(access, name, descriptor);

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
