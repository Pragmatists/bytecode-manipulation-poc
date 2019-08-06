package com.pragmatists.weaving.loaders;

import testtypes.Target;
import testtypes.Clazz;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class UniformSubstitutorTest {
    private static byte[] targetClassBytecode;

    @BeforeAll
    static void setUp() throws IOException {
        InputStream is = UniformSubstitutorTest.class.getClassLoader().getResourceAsStream("Target.class");
        targetClassBytecode = is.readAllBytes();
    }

    @Test
    void shouldLoadTargetClass() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        UniformSubstitutor uniformSubstitutor = new UniformSubstitutor(targetClassBytecode);

        Class substitutedClass = uniformSubstitutor.loadClass(Target.class.getName());

        Constructor constructor = substitutedClass.getDeclaredConstructors()[0];
        Clazz instance = (Clazz) constructor.newInstance();
        assertEquals("target", instance.getClassId());
    }

    @Test
    void shouldLoadAsInitialClass() throws ClassNotFoundException {
        UniformSubstitutor uniformSubstitutor = new UniformSubstitutor(targetClassBytecode);

        Class substitutedClass = uniformSubstitutor.loadClass(Target.class.getName());

        assertEquals(uniformSubstitutor, substitutedClass.getClassLoader());
        assertEquals(Target.class.getName(), substitutedClass.getName());
    }
}