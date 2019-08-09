package test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class PrintCaptor extends PrintStream {
    private final List<String> results = new ArrayList<>();

    public PrintCaptor() {
        super(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
    }

    @Override
    public void print(String x) {
        results.add(x);
    }

    public List<String> getResults() {
        return results;
    }
}

