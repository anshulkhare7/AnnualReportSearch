package com.anshul.arsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class SearchController{
    
    private final static String INDEX_NAME = "annual_reports";
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    @GetMapping(value="/search")
    public ResponseJson getMethodName(@RequestParam(name = "q") String queryString, @RequestParam(name = "p") Integer pageNumber) {        
                
        logger.debug("Initiating search for: " + queryString);
        
        ResponseJson responseJson = new ResponseJson();
        List<SearchResult> searchResults =  new ArrayList<SearchResult>();        

        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        try {                 
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = new MatchPhraseQueryBuilder("content", queryString);                 
                searchSourceBuilder.query(matchPhraseQueryBuilder);                
                searchSourceBuilder.size(10);
                searchSourceBuilder.from(pageNumber-1);

                HighlightBuilder highlightBuilder = new HighlightBuilder();
                HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content");
                highlightContent.highlighterType("plain");                
                highlightBuilder.field(highlightContent);

                searchSourceBuilder.highlighter(highlightBuilder);

                searchRequest.source(searchSourceBuilder);
                logger.debug("Request: "+searchRequest.toString());

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                
                SearchHits searchHits = searchResponse.getHits();    
                long hitsCount = searchHits.getTotalHits().value;
                logger.debug("Total results for the query phrase ''"+queryString+"' : "+hitsCount);

                for (SearchHit searchHit : searchHits) {                                        
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    String companyName = (String) sourceAsMap.get("company");
                    String year = (String) sourceAsMap.get("year");
                    Integer page = (Integer) sourceAsMap.get("page_no");
                    String content = (String) sourceAsMap.get("content");

                    Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                    HighlightField highlight = highlightFields.get("content");
                    Text[] fragments = highlight.fragments();
                    List<String> fragmentString = new ArrayList<String>();
                    for(Text txtFragment : fragments){
                        fragmentString.add(txtFragment.string());
                    }                    
                    searchResults.add(new SearchResult(companyName, year, page, fragmentString, content));
                }
                responseJson = new ResponseJson("OK", searchResults, hitsCount);
                client.close();
        } catch (Exception e) {            
            e.printStackTrace();
            logger.debug(e.getMessage());
		}
                
        return responseJson;
    }    
}