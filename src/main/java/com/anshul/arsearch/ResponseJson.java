package com.anshul.arsearch;

import java.util.List;

public class ResponseJson{

    private String status = "ERROR";
    private List<SearchResult> searchResults;
    private long resultCount = 0;

    public ResponseJson(){        
    }

    public ResponseJson(String status, List<SearchResult> searchResults, long resultCount) {
        this.status = status;
        this.searchResults = searchResults;
        this.resultCount = resultCount;
    }

    public String getStatus() {
        return status;
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public long getResultCount(){
        return resultCount;
    }
    
}