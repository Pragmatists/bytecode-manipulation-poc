package com.pragmatists.weaving.utils;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class Types {
    public static int correspondingReturnBytecode(Type returnType) {
        int sort = returnType.getSort();
        switch (sort) {
            case Type.VOID:
                return RETURN;

            case Type.BOOLEAN:
                return IRETURN;
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
                return ARETURN;
            case Type.OBJECT:
                return ARETURN;

            default:
                throw new IllegalArgumentException(String.format(
                        "Return type %s is not valid as a method return type.", returnType));
        }
    }

    public static int correspondingReturnBytecode(Class c) {
        if (c == null) {
            return RETURN;
        }

        return correspondingReturnBytecode(Type.getType(c));
    }
}
