package test.types;

public class Target implements Clazz {
    @Override
    public String getClassId() {
        return "non-target";
    }

    @Override
    public String getClassId2() {
        return "non-target2";
    }
}
