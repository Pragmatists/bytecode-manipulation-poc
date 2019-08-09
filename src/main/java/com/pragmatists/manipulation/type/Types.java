package com.pragmatists.manipulation.type;

import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class Types {
    private static final String DESCRIPTOR_FORMAT = "(%s)%s";

    public static int correspondingReturnBytecode(Type returnType) {
        int sort = returnType.getSort();
        switch (sort) {
            case Type.VOID:
                return RETURN;

            case Type.BOOLEAN:
                return IRETURN; // keep the duplicate branches in switch to see test coverage for all cases
            case Type.CHAR:
                return IRETURN;
            case Type.BYTE:
                return IRETURN;
            case Type.SHORT:
                return IRETURN;
            case Type.INT:
                return IRETURN;

            case Type.FLOAT:
                return FRETURN;

            case Type.LONG:
                return LRETURN;

            case Type.DOUBLE:
                return DRETURN;

            case Type.ARRAY:
                return ARETURN; // see above
            case Type.OBJECT:
                return ARETURN;

            default:
                throw new IllegalArgumentException(String.format(
                        "Return type %s is not valid as a method return type.", returnType));
        }
    }

    public static String internalName(Class c) {
        return binaryToInternal(c.getName());
    }

    public static String binaryToInternal(String name) {
        return name.replace('.', '/');
    }

    public static String methodDescriptor(Class returnType, Class... paramTypes) {
        String paramTypeDescriptors = Arrays.stream(paramTypes)
                .map(Type::getDescriptor)
                .collect(Collectors.joining());

        String returnTypeDescriptor = returnType == null ? Type.VOID_TYPE.getDescriptor() : Type.getDescriptor(returnType);

        return String.format(DESCRIPTOR_FORMAT, paramTypeDescriptors, returnTypeDescriptor);
    }
}
