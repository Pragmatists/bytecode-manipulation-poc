package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.config.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;

/**
 * MethodBytecodeModifier uses special MethodVisitors (@link ModifyingMethodVisitor) to modify instructions of one of
 * the visited class's methods. Several ModifyingMethodVisitors can be combined to apply different changes, but the
 * order of visiting is relevant.
 */
public class InstructionsModifier extends ClassVisitor {
    private final String methodName;
    private final String descriptor;
    private final Instructions instructions;
    private final ClassVisitor classVisitor;
    private final ModifyingMethodVisitorProvider methodVisitorProvider;

    /**
     * Use this constructor only if the methodName argument value uniquely identifies a method in the class (i.e. when
     * descriptor unnecessary).
     */
    public InstructionsModifier(String methodName,
                                Instructions instructions,
                                ClassVisitor classVisitor,
                                ModifyingMethodVisitorProvider methodVisitorProvider) {
        this(methodName, null, instructions, classVisitor, methodVisitorProvider);
    }

    public InstructionsModifier(String methodName,
                                String descriptor,
                                Instructions instructions,
                                ClassVisitor classVisitor,
                                ModifyingMethodVisitorProvider methodVisitorProvider) {
        super(Config.ASM_VERSION, classVisitor);

        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.methodName = methodName;
        this.instructions = instructions;
        this.classVisitor = classVisitor;
        this.methodVisitorProvider = methodVisitorProvider;
        this.descriptor = descriptor;
    }

    public static byte[] modifyMethodInClassfile(String methodName,
                                                 String descriptor,
                                                 byte[] bytecode,
                                                 Instructions instructions,
                                                 ModifyingMethodVisitorProvider methodVisitorProvider) {
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        InstructionsModifier modifier =
                new InstructionsModifier(methodName, descriptor, instructions, classWriter, methodVisitorProvider);

        classReader.accept(modifier, 0);
        return classWriter.toByteArray();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = classVisitor.visitMethod(access, name, descriptor, signature, exceptions);
        if (methodIsForModification(name, descriptor, methodVisitor)) {
            return methodVisitorProvider.apply(methodVisitor, instructions);
        } else {
            return methodVisitor;
        }
    }

    private boolean methodIsForModification(String name, String descriptor, MethodVisitor methodVisitor) {
        return methodVisitor != null
                && methodName.equals(name)
                && (this.descriptor == null || this.descriptor.equals(descriptor));
    }

}
