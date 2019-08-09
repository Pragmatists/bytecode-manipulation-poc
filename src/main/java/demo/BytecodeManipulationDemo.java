package demo;

import com.pragmatists.manipulation.bytecode.Instructions;
import com.pragmatists.manipulation.bytecode.extraction.MethodInstructionsExtractor;
import com.pragmatists.manipulation.bytecode.modification.AppendingMethodVisitor;
import com.pragmatists.manipulation.bytecode.modification.MethodBytecodeModifier;
import com.pragmatists.manipulation.loaders.ClassSubstitutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.pragmatists.manipulation.ClassFileUtils.getBytecode;
import static com.pragmatists.manipulation.type.Types.methodDescriptor;
import static org.objectweb.asm.Opcodes.POP;

public class BytecodeManipulationDemo {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String mainFQClassName = "demo.RunThis";
        String stringProviderFQClassName = "demo.StringProvider";

        byte[] mainBytecode = getBytecode("RunThis.class");
        byte[] stringProviderBytecode = getBytecode("StringProvider.class");
        byte[] timeProviderBytecode = getBytecode("TimePrinter.class");

        MethodInstructionsExtractor nowExtractor = new MethodInstructionsExtractor("now");
        Instructions extractedCode = nowExtractor.extract(timeProviderBytecode)
                .orElse(new Instructions());

        Instructions instructions = new Instructions();
        instructions.collectInstruction(mv -> {
            mv.visitInsn(POP);
            extractedCode.appendMethodInstructions(mv);
            mv.visitLdcInsn("WOW!");
        });

        final byte[] modifiedStringProviderBytecode = MethodBytecodeModifier.modifyMethodInClassfile(
                "get", methodDescriptor(String.class), stringProviderBytecode, instructions, AppendingMethodVisitor::new);

        Map<String, byte[]> classesToTargetBytecode = Map.of(
                mainFQClassName, mainBytecode,
                stringProviderFQClassName, modifiedStringProviderBytecode
        );

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToTargetBytecode);
        Class<?> c = classSubstitutor.loadClass(mainFQClassName);

        Method main = c.getMethod("main", String[].class);
        main.invoke(null, new Object[]{new String[0]});
    }
}
