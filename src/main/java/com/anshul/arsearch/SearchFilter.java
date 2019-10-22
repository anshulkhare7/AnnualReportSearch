package com.anshul.arsearch;

public class SearchFilter {

    private String name;
    private long count;

    
    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public SearchFilter(String name, long count) {
        this.name = name;
        this.count = count;
    }

    @Override
    public String toString() {
        return "SearchFilter [count=" + count + ", name=" + name + "]";
    }

}