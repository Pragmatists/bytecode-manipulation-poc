package examples.classes;

import java.util.ArrayList;
import java.util.List;

public class ListReturner {
    public List<String> getList(String string) {
        List<String> strings = new ArrayList<>();
        strings.add(string);
        return strings;
    }
}
