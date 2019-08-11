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
        String mainFQClassName = "demo.RunThis";
        String stringProviderFQClassName = "demo.StringProvider";

        byte[] mainBytecode = getBytecodeFromResources("RunThis.class");
        byte[] stringProviderBytecode = getBytecodeFromResources("StringProvider.class");
        byte[] timePrinterBytecode = getBytecodeFromResources("TimePrinter.class");

        InstructionsExtractor nowExtractor = new InstructionsExtractor("now");
        Instructions extractedCode = nowExtractor.extract(timePrinterBytecode)
                .orElse(new Instructions());

        Instructions instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            mv.visitInsn(POP);
            extractedCode.appendMethodInstructions(mv);
            mv.visitLdcInsn("Nobody expected this!");
        });

        byte[] modifiedStringProviderBytecode = InstructionsModifier.modifyMethodInClassfile(
                "get", methodDescriptor(String.class), stringProviderBytecode, instructions, AppendingMethodVisitor::new);

        Map<String, byte[]> classesToTargetBytecode = Map.of(
                mainFQClassName, mainBytecode,
                stringProviderFQClassName, modifiedStringProviderBytecode
        );

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToTargetBytecode);
        Class<?> c = classSubstitutor.loadClass(mainFQClassName);

        System.out.println();

        Method main = c.getMethod("main", String[].class);
        main.invoke(null, new Object[]{new String[0]});
    }

    private static byte[] getBytecodeFromResources(String classFilePath) {
        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            return systemClassLoader.getResourceAsStream(classFilePath).readAllBytes();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(String.format("Couldn't load class file from path %s", classFilePath), e);
        }
    }
}
