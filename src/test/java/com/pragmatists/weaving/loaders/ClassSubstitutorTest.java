package com.pragmatists.weaving.loaders;

import testtypes.Target;
import testtypes.Clazz;
import com.pragmatists.weaving.bytecode.BytecodeGenerator;
import com.pragmatists.weaving.bytecode.CrudeBytecodeGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassSubstitutorTest {
    @Test
    void shouldLoadTargetClass() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        BytecodeGenerator bytecodeGenerator = new CrudeBytecodeGenerator();
        Map<String, byte[]> classesToBytecode = new HashMap<>();
        String className = Target.class.getName();
        classesToBytecode.put(className, bytecodeGenerator.generateClass(className));

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToBytecode);
        Class substitutedClass = classSubstitutor.loadClass(className);

        Constructor constructor = substitutedClass.getConstructors()[0];
        Clazz target = (Clazz) constructor.newInstance();
        assertEquals(classSubstitutor, substitutedClass.getClassLoader());
        assertEquals("target", target.getClassId());
        assertEquals("target2", target.getClassId2());
    }

    @Test
        // TODO move to other place
    void shouldLoad() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        BytecodeGenerator bytecodeGenerator = new CrudeBytecodeGenerator();
        Map<String, byte[]> classesToBytecode = new HashMap<>();
        String className = Target.class.getName();
        classesToBytecode.put(className, bytecodeGenerator.generateClass(className));

        ClassSubstitutor classSubstitutor = new ClassSubstitutor(classesToBytecode);
        Class substitutedClass = classSubstitutor.loadClass(className);

        Object o = substitutedClass.getConstructors()[0].newInstance();
        Method print = substitutedClass.getMethod("print");
        print.invoke(o);

        // todo make some assertion
    }
}