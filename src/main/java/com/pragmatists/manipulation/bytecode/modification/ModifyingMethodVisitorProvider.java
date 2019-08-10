package com.pragmatists.manipulation.bytecode.modification;

import com.pragmatists.manipulation.bytecode.Instructions;
import org.objectweb.asm.MethodVisitor;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ModifyingMethodVisitorProvider extends BiFunction<MethodVisitor, Instructions, ModifyingMethodVisitor> {
}
