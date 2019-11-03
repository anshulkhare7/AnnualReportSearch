package com.anshul.arsearch;

import java.util.List;

import com.anshul.arsearch.beans.Tag;

public class SearchBody {

    private String queryString;
    private int pageNumber;
    private List<Tag> filters;        

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<Tag> getFilters() {
        return filters;
    }

    public void setFilters(List<Tag> filters) {
        this.filters = filters;
    }
}