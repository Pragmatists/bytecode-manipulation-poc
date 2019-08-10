package com.pragmatists.manipulation.loaders;

import javax.lang.model.SourceVersion;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static lombok.Lombok.sneakyThrow;

/**
 * ClassSubstitutor is used to load classes from provided bytecode without checking if the class has been already loaded
 * by any other ClassLoader. The loading is based on mapping of classes's fully qualified names to byte arrays. If a
 * given class name is not mapped to bytecode, a ClassSubstitutor will fallback to its parent, which is by default the
 * class loader which loaded the ClassSubstitutor class.
 */
public class ClassSubstitutor extends ClassLoader {
    private final Map<String, byte[]> classNamesToBytecode;
    private final Map<String, Class> loadedClasses = new ConcurrentHashMap<>();
    private final ClassLoader fallBackClassloader;

    public ClassSubstitutor(Map<String, byte[]> classNamesToBytecode) {
        super(ClassSubstitutor.class.getClassLoader());
        this.classNamesToBytecode = Map.copyOf(classNamesToBytecode);
        validate(classNamesToBytecode.keySet());
        this.fallBackClassloader = getParent();
    }

    public ClassSubstitutor(Map<String, byte[]> classNamesToBytecode, ClassLoader fallbackClassloader) {
        super(fallbackClassloader);
        this.classNamesToBytecode = Map.copyOf(classNamesToBytecode);
        validate(classNamesToBytecode.keySet());
        this.fallBackClassloader = getParent();
    }

    // Overriding loadClass instead of findClass to bypass the delegation up the classloader hierarchy.
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (classNamesToBytecode.containsKey(name)) {
            return loadedClasses.computeIfAbsent(name, this::substituteClass);
        }

        return fallBackClassloader.loadClass(name);
    }

    private Class<?> substituteClass(String name) {
        byte[] targetByteCode = classNamesToBytecode.get(name);
        try {
            return defineClass(name, targetByteCode, 0, targetByteCode.length);
        } catch (Throwable t) { // We don't want a ClassFormatError to bubble up.
            // The calling method still has ClassNotFoundException listed in the `throws` clause.
            throw sneakyThrow(new ClassNotFoundException(
                    String.format("Could not load bytecode of class %s listed for substitution", name), t));
        }
    }

    private void validate(Set<String> classNames) {
        classNames.forEach(name -> {
            if (!SourceVersion.isName(name)) { // this doesn't FULLY do the trick, but provides some validation.
                throw new IllegalArgumentException(String.format("'%s' is an invalid class name.", name));
            }
        });
    }
}
