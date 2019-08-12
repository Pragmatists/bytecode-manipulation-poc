package demo;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.extraction.InstructionsExtractor;
import com.pragmatists.manipulation.bytecode.modification.AppendingMethodVisitor;
import com.pragmatists.manipulation.bytecode.modification.InstructionsModifier;
import com.pragmatists.manipulation.loaders.ClassSubstitutor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.pragmatists.manipulation.type.Types.methodDescriptor;
import static org.objectweb.asm.Opcodes.POP;

public class BytecodeManipulationDemo {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        String mainClassName = "demo.RunThis";
        String stringProviderClassName = "demo.StringProvider";

        byte[] mainBytecode = getBytecodeFromResources("RunThis.class");
        byte[] stringProviderBytecode = getBytecodeFromResources("StringProvider.class");
        byte[] timePrinterBytecode = getBytecodeFromResources("TimePrinter.class");

        InstructionsExtractor nowExtractor = new InstructionsExtractor("now", methodDescriptor(null));
        Instructions instructionsToPrintCurrentDateTime = nowExtractor.extract(timePrinterBytecode)
                .orElseThrow(BytecodeManipulationDemo::failedToFindNowMethod);

        Instructions instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            mv.visitInsn(POP);
            mv.visitLdcInsn("This will be the String returned by StringProvider#get() instead");
            instructionsToPrintCurrentDateTime.appendMethodInstructions(mv);
        });

        byte[] modifiedStringProviderBytecode = InstructionsModifier.modifyMethodInClassfile(
                "get", methodDescriptor(String.class), stringProviderBytecode, instructions, AppendingMethodVisitor::new);

        Map<String, byte[]> classesToTargetBytecode = Map.of(
                mainClassName, mainBytecode,
                stringProviderClassName, modifiedStringProviderBytecode
        );

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToTargetBytecode);
        Class<?> mainClass = classSubstitutor.loadClass(mainClassName);

        Method main = mainClass.getMethod("main", String[].class);
        main.invoke(null, new Object[]{new String[0]});
    }

    private static byte[] getBytecodeFromResources(String classFilePath) {
        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            return systemClassLoader.getResourceAsStream(classFilePath).readAllBytes();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(String.format("Couldn't load class file from resources: %s", classFilePath), e);
        }
    }

    private static IllegalStateException failedToFindNowMethod() {
        return new IllegalStateException("The method `TimePrinter#now()` should be in place...");
    }
}
