package com.pragmatists.weaving.examples;

import com.pragmatists.weaving.bytecode.Instructions;
import com.pragmatists.weaving.bytecode.extraction.MethodInstructionsExtractor;
import com.pragmatists.weaving.bytecode.modification.PrependingMethodVisitor;
import com.pragmatists.weaving.type.Types;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static com.pragmatists.weaving.examples.Utils.getBytecode;
import static com.pragmatists.weaving.examples.Utils.loadModifiedClass;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.LSTORE;

public class Examples {
    private static final Calculator originalCalculatorInstance = new Calculator();
    private static final byte[] originalCalculatorBytecode = getBytecode(Calculator.class);
    private static final String addMethodName = "add";
    private static final String methodDescriptor = Types.methodDescriptor(String.class, long.class, long.class);

    @Test
    void simpleParameterHijacking() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        var instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            mv.visitLdcInsn(-1L);           // pushing -1L onto the stack
            mv.visitVarInsn(LSTORE, 1);     // storing into first argument's slots
            mv.visitLdcInsn(0L);            // pushing 0l onto the stack
            mv.visitVarInsn(LSTORE, 3);     // storing into second argument's slots (longs take two slots)
        });

        var modifiedCalculatorClass =
                loadModifiedClass(addMethodName, methodDescriptor, instructions, originalCalculatorBytecode, PrependingMethodVisitor::new);
        var modifiedCalculator = modifiedCalculatorClass.getConstructors()[0].newInstance();
        var add = modifiedCalculatorClass.getDeclaredMethod("add", long.class, long.class);

        String invocationResult = (String) add.invoke(modifiedCalculator, 3L, 4L);
        assertNotEquals(originalCalculatorInstance.add(3L, 4L), invocationResult);
        assertEquals("-1", invocationResult);
    }

    @Test
    void parameterHijackingWithBytecodeExtraction() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var timerClassBytecode = getBytecode(Timer.class);
        var timerMethodExtractor = new MethodInstructionsExtractor("getTime"); // skipping descriptor, method unique

        var getTimeInstructions = timerMethodExtractor.extract(timerClassBytecode)
                .orElseThrow(FailedExampleException::new);
        var instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            getTimeInstructions.appendMethodInstructions(mv);  // adding instructions taken from Timer#getTime()
            mv.visitVarInsn(LSTORE, 1);                        // popping stack into first argument's slot
        });

        var time = new Timer().getTime();
        var modifiedCalculatorClass =
                loadModifiedClass(addMethodName, methodDescriptor, instructions, originalCalculatorBytecode, PrependingMethodVisitor::new);
        var modifiedCalculator = modifiedCalculatorClass.getConstructors()[0].newInstance();
        var add = modifiedCalculatorClass.getDeclaredMethod("add", long.class, long.class);

        String invocationResult = (String) add.invoke(modifiedCalculator, 3L, 4L);
        assertNotEquals(originalCalculatorInstance.add(3L, 4L), invocationResult);
        assertTrue(time < Long.parseLong(invocationResult));
    }
}
