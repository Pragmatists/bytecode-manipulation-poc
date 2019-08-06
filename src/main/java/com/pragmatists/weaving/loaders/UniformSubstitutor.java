package com.pragmatists.weaving.loaders;

public class UniformSubstitutor extends ClassLoader {
    private final byte[] targetByteCode;

    public UniformSubstitutor(byte[] targetByteCode) {
        this.targetByteCode = targetByteCode;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return defineClass(name, targetByteCode, 0, targetByteCode.length);
        } catch (NoClassDefFoundError | SecurityException e) {
            return getClass().getClassLoader().loadClass(name);
        }
     }
}
