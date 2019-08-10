package com.pragmatists.manipulation.bytecode.characteristics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.objectweb.asm.Type;

import java.util.List;

import static com.pragmatists.manipulation.type.Types.methodDescriptor;

@Getter
@EqualsAndHashCode
@ToString
public class MethodCharacteristic {
    private final int accessFlag;
    private final String name;
    private final List<Class> paramTypes;
    private final Class returnType;
    private final Type returnType_;
    private final String signature = null; // not supported yet
    private final String[] exceptions = new String[0]; // not supported
    private final String descriptor;

    public MethodCharacteristic(int accessFlag, String name, List<Class> paramTypes, Class returnType) {
        this.accessFlag = accessFlag;
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.descriptor = methodDescriptor(returnType, paramTypes.toArray(new Class[0]));
        this.returnType_ = classToType(returnType);
    }

    public MethodCharacteristic(int accessFlag, String name, String descriptor) {
        this.accessFlag = accessFlag;
        this.name = name;
        this.paramTypes = null;
        this.returnType = null;
        this.descriptor = descriptor;
        this.returnType_ = Type.getReturnType(descriptor);
    }

    public Type getReturnType() {
        return returnType_;
    }

    private Type classToType(Class c) {
        if (c == null) {
            return Type.VOID_TYPE;
        }

        return Type.getType(c);
    }
}

