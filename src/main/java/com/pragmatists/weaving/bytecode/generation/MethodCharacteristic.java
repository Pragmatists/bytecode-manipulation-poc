package com.pragmatists.weaving.bytecode.generation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;

import static com.pragmatists.weaving.bytecode.generation.Identifiers.CONSTRUCTOR_METHOD_NAME;
import static com.pragmatists.weaving.type.Types.methodDescriptor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Getter
@EqualsAndHashCode
@ToString
public class MethodCharacteristic {
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
    private final Type returnType_;
    private final String signature = null; // unsupported yet
    private final String[] exceptions = new String[0]; // unsupported yet
    private final String descriptor;

    public MethodCharacteristic withName(String newMethodName) {
        return this.toBuilder().name(newMethodName).build();
    }

    @Builder(toBuilder = true)
    private MethodCharacteristic(int accessFlag, String name, List<Class> paramTypes, Class returnType, String descriptor) {
        this.accessFlag = accessFlag;
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;

        // TODO this is not ideal, given the default builder:
        //  it should be EITHER call `builder().returnType(...).paramTypes(...)` OR `builder().descriptor(...)`
        if (descriptor == null) {
            this.descriptor = methodDescriptor(returnType, paramTypes.toArray(new Class[paramTypes.size()]));
            this.returnType_ = classToType(returnType);
        } else {
            this.descriptor = descriptor;
            this.returnType_ = Type.getReturnType(descriptor);
        }
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

