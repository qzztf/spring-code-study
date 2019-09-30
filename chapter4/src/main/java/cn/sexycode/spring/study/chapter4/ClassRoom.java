package cn.sexycode.spring.study.chapter4;

public class ClassRoom {
    public ClassRoom() {
        System.out.println("init ClassRoom");
    }

    private String name;

    private int size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
