package com.pragmatists.weaving.bytecode.extraction;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

public class MethodBytecodeExtractor extends ClassVisitor {
    private final String methodName;
    private ExtractingMethodVisitor extractingMethodVisitor;

    public MethodBytecodeExtractor(String methodName) {
        super(ASM7);
        this.methodName = methodName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!methodName.equals(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        extractingMethodVisitor = new ExtractingMethodVisitor(ASM7);
        return extractingMethodVisitor;
    }

    public ExtractingMethodVisitor getExtractingMethodVisitor() {
        return extractingMethodVisitor;
    }
}
