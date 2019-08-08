package test.control;

public class ForTest {
    public String loop(int n) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < n; i++) {
            result.append("x");
        }

        return result.toString();
    }
}
