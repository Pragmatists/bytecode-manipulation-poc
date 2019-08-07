package com.pragmatists.weaving.bytecode.generation;

import lombok.Builder;
import lombok.Value;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.pragmatists.weaving.bytecode.generation.Identifiers.CONSTRUCTOR_METHOD_NAME;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Value
@Builder
public class MethodCharacteristic {
    private static final String DESCRIPTOR_FORMAT = "(%s)%s";

    public static final MethodCharacteristic DEFAULT_CONSTRUCTOR_CHARACTERISTIC = MethodCharacteristic.builder()
            .accessFlag(ACC_PUBLIC)
            .name(CONSTRUCTOR_METHOD_NAME.getValue())
            .paramTypes(Collections.emptyList())
            .returnType(null)
            .build();

    private final int accessFlag;
    private final String name;
    private final List<Class> paramTypes;
    private final Class returnType;
    private final String signature = null; // unsupported
    private final String[] exceptions = new String[]{}; // unsupported

    public MethodCharacteristic(int accessFlag, String name, List<Class> paramTypes, Class returnType) {
        this.accessFlag = accessFlag;
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    public String getDescriptor() {
        String paramTypeDescriptors = paramTypes.stream()
                .map(Type::getDescriptor)
                .collect(Collectors.joining());

        String returnTypeDescriptor = returnType == null ? Type.VOID_TYPE.getDescriptor() : Type.getDescriptor(returnType);

        return String.format(DESCRIPTOR_FORMAT, paramTypeDescriptors, returnTypeDescriptor);
    }
}

