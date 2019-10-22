package com.anshul.arsearch;

import java.util.List;

public class SearchData {
    private List<SearchResult> searchResults;
    private List<SearchFilter> searchFilters;
    private long resultCount;

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public List<SearchFilter> getSearchFilters() {
        return searchFilters;
    }

    public SearchData(List<SearchResult> searchResults, List<SearchFilter> searchFilters) {
        this.searchResults = searchResults;
        this.searchFilters = searchFilters;
    }

    public long getResultCount() {
        return resultCount;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }

    @Override
    public String toString() {
        StringBuilder searchDataBuilder = new StringBuilder();
        searchResults.forEach(action -> searchDataBuilder.append(action.toString()));
        searchFilters.forEach(action -> searchDataBuilder.append(action.toString()));
        return "SearchData [ Total Count: "+resultCount+"] Data ["+searchDataBuilder.toString()+"]";
    }
}
