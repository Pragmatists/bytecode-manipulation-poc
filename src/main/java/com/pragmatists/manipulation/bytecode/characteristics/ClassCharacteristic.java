package com.pragmatists.manipulation.bytecode.characteristics;

import lombok.Builder;
import lombok.Value;

import static com.pragmatists.manipulation.type.Types.binaryToInternal;

@Value
@Builder
public class ClassCharacteristic {
    private final int javaVersion;
    private final int accessFlag;
    private final String name;
    private final String signature = null; // not supported yet
    private final String superName;
    private final String[] interfaces;

    public String getInternalName() {
        return binaryToInternal(name);
    }

    public String getSuperInternalName() {
        return binaryToInternal(superName);
    }
}
