package com.anshul.arsearch;

public class Filter {

    private String filterType;
    private String name;
    private long count;

    
    public String getFilterType(){
        return filterType;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public Filter(String name, long count, String filterType) {
        this.name = name;
        this.count = count;
        this.filterType = filterType;
    }

    @Override
    public String toString() {
        return filterType+" Filter [name=" + name + ", count=" + count + "]";
    }

}