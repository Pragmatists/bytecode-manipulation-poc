package test.control;

public class IfTest {
    public String choose(boolean flag) {
        String result;

        if (flag) {
            result = "right";
        } else {
            result = "left";
        }

        return result;
    }
}
