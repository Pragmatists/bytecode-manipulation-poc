package test.control;

public class TryCatchTest {
    public String tryCatch(boolean shouldThrow) {
        String result;
        try {
            if (shouldThrow) {
                throw new RuntimeException();
            }

            result = "not thrown";
        } catch (RuntimeException rte) {
            result = rte.toString();
        }

        return result;
    }
}
