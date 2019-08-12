package test.types;

public class Modified {
    public void foo() {
        System.out.println("foo here");
    }

    public void foo(Boolean flag) {
        if (flag) {
            System.out.println("foo: true");
            return;
        }

        System.out.println("foo: false");
    }

}
