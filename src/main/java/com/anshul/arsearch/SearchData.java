package com.anshul.arsearch;

import java.util.List;

public class SearchData {
    private List<SearchResult> searchResults;
    private List<Filter> searchFilters;
    private long resultCount;

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public List<Filter> getSearchFilters() {
        return searchFilters;
    }

    public SearchData(List<SearchResult> searchResults) {
        this.searchResults = searchResults;        
    }

    public long getResultCount() {
        return resultCount;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }
    
    public void setSearchFilters(List<Filter> searchFilters) {
        this.searchFilters = searchFilters;
    }

    @Override
    public String toString() {
        StringBuilder searchDataBuilder = new StringBuilder();
        if(null!=searchResults)
            searchResults.forEach(action -> searchDataBuilder.append(action.toString()));
        if(null!=searchFilters)            
            searchFilters.forEach(action -> searchDataBuilder.append(action.toString()));
        return "SearchData [ Total Count: "+resultCount+"] Data ["+searchDataBuilder.toString()+"]";
    }
}
