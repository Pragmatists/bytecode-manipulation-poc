package com.pragmatists.weaving.bytecode;

import com.pragmatists.weaving.bytecode.generation.MethodCharacteristic;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@EqualsAndHashCode
public class Instructions {
    private MethodCharacteristic methodCharacteristic;
    private final List<Consumer<MethodVisitor>> instructionAdditions;

    public Instructions() {
        this.instructionAdditions = new ArrayList<>();
    }

    private Instructions(MethodCharacteristic methodCharacteristic, List<Consumer<MethodVisitor>> instructionAdditions) {
        this.methodCharacteristic = methodCharacteristic;
        this.instructionAdditions = instructionAdditions;
    }

    public Instructions forMethod(String name) {
        return new Instructions(this.methodCharacteristic.withName(name), this.instructionAdditions);
    }

    public void collectInstruction(Consumer<MethodVisitor> instructionAddition) {
        Objects.requireNonNull(instructionAddition);
        instructionAdditions.add(instructionAddition);
    }

    public void appendMethodInstructions(MethodVisitor methodVisitor) {
        instructionAdditions.forEach(instructionAddition -> instructionAddition.accept(methodVisitor));
    }

    public void setMethodCharacteristic(MethodCharacteristic methodCharacteristic) {
        this.methodCharacteristic = methodCharacteristic;
    }
}
