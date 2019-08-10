package com.pragmatists.manipulation.bytecode.extraction;

import com.pragmatists.manipulation.bytecode.characteristics.MethodCharacteristic;

import java.util.function.Function;

@FunctionalInterface
public interface ExtractingMethodVisitorProvider extends Function<MethodCharacteristic, ExtractingMethodVisitor> {
}
