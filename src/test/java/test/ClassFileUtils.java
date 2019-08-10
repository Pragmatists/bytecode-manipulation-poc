package test;

import java.io.IOException;
import java.io.InputStream;

public class ClassFileUtils {
    private static final String CLASS_FILE_EXTENSION = ".class";

    public static byte[] getBytecode(Class c) {
        InputStream resourceAsStream = c.getResourceAsStream(c.getSimpleName() + CLASS_FILE_EXTENSION);
        return getBytecode(resourceAsStream);
    }

    public static byte[] getBytecodeFromResources(String classFileName) {
        return getBytecode(ClassLoader.getSystemClassLoader().getResourceAsStream(classFileName));
    }

    private static byte[] getBytecode(InputStream is) {
        try {
            return is.readAllBytes();
        } catch (IOException | NullPointerException e) {
            throw new CouldNotLoadBytecodeFromFileException(e);
        }
    }

    public static class CouldNotLoadBytecodeFromFileException extends RuntimeException {
        CouldNotLoadBytecodeFromFileException(Throwable cause) {
            super(cause);
        }
    }
}
