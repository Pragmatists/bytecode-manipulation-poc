package com.pragmatists.weaving.bytecode;

import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Instructions {
    private final List<Consumer<MethodVisitor>> instructionAdditions = new ArrayList<>();

    public void collectInstruction(Consumer<MethodVisitor> instructionAddition) {
        Objects.requireNonNull(instructionAddition);
        instructionAdditions.add(instructionAddition);
    }

    public void appendMethodInstructions(MethodVisitor methodVisitor) {
        instructionAdditions.forEach(instructionAddition -> instructionAddition.accept(methodVisitor));
    }
}
