package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;
import org.objectweb.asm.MethodVisitor;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ExtractingMethodVisitorProvider extends BiFunction<MethodVisitor, MethodCharacteristic, ExtractingMethodVisitor> {
}
