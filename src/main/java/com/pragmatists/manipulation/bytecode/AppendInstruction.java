package com.pragmatists.manipulation.bytecode;

import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

@FunctionalInterface
public interface AppendInstruction extends Consumer<MethodVisitor> {
}
