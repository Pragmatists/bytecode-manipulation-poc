package com.pragmatists.manipulation.bytecode;

import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class Instructions {
    private MethodCharacteristic methodCharacteristic;
    private final List<InstructionsAppender> instructionAdditions;

    public Instructions() {
        this.instructionAdditions = new ArrayList<>();
    }

    public void collectInstruction(InstructionsAppender instructionAddition) {
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
