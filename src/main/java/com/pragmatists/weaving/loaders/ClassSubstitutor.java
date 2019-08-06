package com.pragmatists.weaving.loaders;

import com.pragmatists.weaving.loaders.exceptions.NoBytecodeProvidedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassSubstitutor extends ClassLoader {
    private final Map<String, byte[]> classesToTargetBytecode;
    private final Map<String, Class> loadedClasses = new ConcurrentHashMap<>();
    private final ClassLoader fallBackClassloader;

    public ClassSubstitutor(Map<String, byte[]> classesToTargetBytecode) {
        this.classesToTargetBytecode = classesToTargetBytecode;
        this.fallBackClassloader = getClass().getClassLoader();
    }

    public ClassSubstitutor(Map<String, byte[]> classesToTargetBytecode, ClassLoader fallBackClassloader) {
        this.classesToTargetBytecode = classesToTargetBytecode;
        this.fallBackClassloader = fallBackClassloader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // TODO synchronization
        if (classesToTargetBytecode.containsKey(name)) {
            return loadedClasses.computeIfAbsent(name, this::substituteClass);
        }

        return fallBackClassloader.loadClass(name);
    }

    private Class<?> substituteClass(String name) {
        byte[] targetByteCode = classesToTargetBytecode.get(name);
        if (targetByteCode != null) {
            return defineClass(name, targetByteCode, 0, targetByteCode.length);
        } else {
            throw new NoBytecodeProvidedException(name);
        }
    }
}
