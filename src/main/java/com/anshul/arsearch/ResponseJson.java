package com.anshul.arsearch;

public class ResponseJson {

    private String status = "ERROR";
    private SearchData searchData;

    public ResponseJson() {
    }

    public SearchData getSearchData() {
        return searchData;
    }

     public String getStatus() {
        return status;
    }

    public ResponseJson(String status, SearchData searchData) {
        this.status = status;
        this.searchData = searchData;
    }
    
}