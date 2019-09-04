package com.anshul.arsearch;

import java.util.ArrayList;
import java.util.List;

public class SearchResult{

    private String companyName;
    private String year;
    private long pageNumber;
    private List<String> searchFragment;
    private String content;

    public SearchResult(String companyName, String year, long pageNumber, List<String> searchFragment, String content) {
        super();
        this.companyName = companyName;
        this.year = year;
        this.pageNumber = pageNumber; 
        this.searchFragment = searchFragment;
        this.content = content;
    }

    public SearchResult(String companyName, String year, long pageNumber) {
        super();
        this.companyName = companyName;
        this.year = year;
        this.pageNumber = pageNumber; 
        this.searchFragment = new ArrayList<String>();
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getYear() {
        return year;
    }   

    public long getPageNumber() {
        return pageNumber;
    }

    public List<String> getSearchFragment() {
        return searchFragment;
    }
    
    public String getContent(){
        return content;
    }

    @Override
    public String toString() {
        return "SearchResult [companyName=" + companyName + ", content=" + content + ", pageNumber=" + pageNumber
                + ", year=" + year + "]";
    }    
}