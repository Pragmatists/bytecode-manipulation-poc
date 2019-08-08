package com.pragmatists.weaving.loaders;

import javax.lang.model.SourceVersion;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassSubstitutor extends ClassLoader {
    private final Map<String, byte[]> classesToTargetBytecode;
    private final Map<String, Class> loadedClasses = new ConcurrentHashMap<>();
    private final ClassLoader fallBackClassloader;

    public ClassSubstitutor(Map<String, byte[]> classesToTargetBytecode) {
        super(ClassSubstitutor.class.getClassLoader());
        this.classesToTargetBytecode = Map.copyOf(classesToTargetBytecode);
        validate(classesToTargetBytecode.keySet());
        this.fallBackClassloader = getParent();
    }

    public ClassSubstitutor(Map<String, byte[]> classesToTargetBytecode, ClassLoader fallbackClassloader) {
        super(ClassSubstitutor.class.getClassLoader());
        this.classesToTargetBytecode = Map.copyOf(classesToTargetBytecode);
        validate(classesToTargetBytecode.keySet());
        this.fallBackClassloader = fallbackClassloader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // TODO synchronization or leave non-parallel-capable?
        if (classesToTargetBytecode.containsKey(name)) {
            return loadedClasses.computeIfAbsent(name, this::substituteClass);
        }

        return fallBackClassloader.loadClass(name);
    }

    private Class<?> substituteClass(String name) {
        byte[] targetByteCode = classesToTargetBytecode.get(name);
        return defineClass(name, targetByteCode, 0, targetByteCode.length);
    }

    private void validate(Set<String> classNames) {
        classNames.forEach(name -> {
            if (!SourceVersion.isName(name)) { // TODO this doesn't FULLY do the trick, but provides some validation...
                throw new IllegalArgumentException(String.format("'%s' is an invalid class name.", name));
            }
        });
    }
}
