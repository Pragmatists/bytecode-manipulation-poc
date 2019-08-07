package com.pragmatists.weaving.bytecode.generation;

import lombok.Builder;
import lombok.Value;

import static com.pragmatists.weaving.utils.ClassNames.binaryToInternal;

@Value
@Builder
public class ClassCharacteristic {
    private final int javaVersion;
    private final int accessFlag;
    private final String name;
    private final String signature = null; // not supported yet
    private final String superName;
    private final String[] interfaces;

    public String getName() {
        return binaryToInternal(name);
    }

    public String getSuperName() {
        return binaryToInternal(superName);
    }
}
