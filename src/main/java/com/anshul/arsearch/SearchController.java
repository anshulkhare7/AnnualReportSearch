package com.anshul.arsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.anshul.arsearch.beans.Tag;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@PropertySource(ignoreResourceNotFound = true, value = "classpath:application.properties")
@RestController
public class SearchController{
    
    private final static String INDEX_NAME = "annual_reports";
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    @Value("${elasticsearch.host}")
    private String elasticHostName;

    @Value("${elasticsearch.port}")
    private int elasticPort;                    
    
    @PostMapping(value="/companyfilters")
    public ResponseJson getCompanyFilters(@RequestBody SearchBody searchBody) {
        String queryString = searchBody.getQueryString();
        List<Tag> filters = searchBody.getFilters();
        int pageNumber = searchBody.getPageNumber();
        logger.info("Initiating search query: [" + queryString + "] | filterString: []");
        ResponseJson responseJson = new ResponseJson();
        List<SearchResult> searchResults =  new ArrayList<SearchResult>();        
    
        SearchRequest searchRequest = new SearchRequest();
        
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        RestHighLevelClient client = getClient();
        try {                 
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = new MatchPhraseQueryBuilder("content", queryString);
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();                                
                boolQueryBuilder.must(matchPhraseQueryBuilder);

                filters.forEach(action->boolQueryBuilder.must(new MatchPhraseQueryBuilder(action.getFilterName(), action.getFilterValue())));

                searchSourceBuilder.query(boolQueryBuilder);
                searchSourceBuilder.size(0);                             

                AggregationBuilder companyAggregationBuilder = new TermsAggregationBuilder("group_by_company", null).field("company.keyword").size(10);
                searchSourceBuilder.aggregation(companyAggregationBuilder);

                searchRequest.source(searchSourceBuilder);
                logger.info("Request: "+searchRequest.toString());

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);                
                
                /* Build Response */
                logger.debug("\n\n" + searchResponse.toString());                                

                SearchHits searchHits = searchResponse.getHits();    
                long hitsCount = searchHits.getTotalHits().value;
                logger.info("[companyfilter]Total results for the query phrase '"+queryString+"' : "+hitsCount);                

                List<Filter> companyFilters = new ArrayList<Filter>();

                ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get("group_by_company");                
                parsedStringTerms.getBuckets().stream().forEachOrdered( bucket -> {
                    Terms.Bucket bckt = (Terms.Bucket) bucket;
                    companyFilters.add(new Filter(bckt.getKey().toString(), bckt.getDocCount(), "Company"));
                });                
                
                SearchData searchData = new SearchData(searchResults);
                searchData.setSearchFilters(companyFilters);
                searchData.setResultCount(hitsCount);
                logger.debug(searchData.toString());
                
                responseJson = new ResponseJson("OK", searchData);                

                client.close();
        } catch (Exception e) {            
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
               
        return responseJson;
    }

    @PostMapping(value="/yearfilters")
    public ResponseJson getYearFilters(@RequestBody SearchBody searchBody) {
        String queryString = searchBody.getQueryString();
        List<Tag> filters = searchBody.getFilters();
        int pageNumber = searchBody.getPageNumber();
        logger.info("Initiating search query: [" + queryString + "] | filterString: []");
        ResponseJson responseJson = new ResponseJson();
        List<SearchResult> searchResults =  new ArrayList<SearchResult>();        
    
        SearchRequest searchRequest = new SearchRequest();
        
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        RestHighLevelClient client = getClient();
        try {                 
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = new MatchPhraseQueryBuilder("content", queryString);
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();                                
                boolQueryBuilder.must(matchPhraseQueryBuilder);

                filters.forEach(action->boolQueryBuilder.must(new MatchPhraseQueryBuilder(action.getFilterName(), action.getFilterValue())));             

                searchSourceBuilder.query(boolQueryBuilder);
                searchSourceBuilder.size(0);                             

                AggregationBuilder companyAggregationBuilder = new TermsAggregationBuilder("group_by_year", null).field("year.keyword").size(20);
                searchSourceBuilder.aggregation(companyAggregationBuilder);

                searchRequest.source(searchSourceBuilder);
                logger.info("Request: "+searchRequest.toString());

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);                
                
                /* Build Response */
                logger.debug("\n\n" + searchResponse.toString());                                

                SearchHits searchHits = searchResponse.getHits();    
                long hitsCount = searchHits.getTotalHits().value;
                logger.info("[yearfilter]Total results for the query phrase '"+queryString+"' : "+hitsCount);                

                List<Filter> yearFilters = new ArrayList<Filter>();

                ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get("group_by_year");                
                parsedStringTerms.getBuckets().stream().forEachOrdered( bucket -> {
                    Terms.Bucket bckt = (Terms.Bucket) bucket;
                    yearFilters.add(new Filter(bckt.getKey().toString(), bckt.getDocCount(), "Year"));
                });                
                
                SearchData searchData = new SearchData(searchResults);
                searchData.setSearchFilters(yearFilters);
                searchData.setResultCount(hitsCount);

                logger.debug(searchData.toString());
                
                responseJson = new ResponseJson("OK", searchData);                

                client.close();
        } catch (Exception e) {            
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
               
        return responseJson;
    }

    @PostMapping(value="/search")
    public ResponseJson getSearchResults(@RequestBody SearchBody searchBody) {
        String queryString = searchBody.getQueryString();
        List<Tag> filters = searchBody.getFilters();
        int pageNumber = searchBody.getPageNumber();
        logger.info("Initiating search query: [" + queryString + "] | filterString: []");
        
        ResponseJson responseJson = new ResponseJson();
        List<SearchResult> searchResults =  new ArrayList<SearchResult>();        
    
        SearchRequest searchRequest = new SearchRequest();
        
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        RestHighLevelClient client = getClient();
        try {                 
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = new MatchPhraseQueryBuilder("content", queryString);
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();                                
                boolQueryBuilder.must(matchPhraseQueryBuilder);

                filters.forEach(action->boolQueryBuilder.must(new MatchPhraseQueryBuilder(action.getFilterName(), action.getFilterValue())));             

                searchSourceBuilder.query(boolQueryBuilder);
                searchSourceBuilder.size(10);
                searchSourceBuilder.from(pageNumber-1);

                HighlightBuilder highlightBuilder = new HighlightBuilder();
                HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content");
                highlightContent.highlighterType("plain");                
                highlightBuilder.field(highlightContent);

                searchSourceBuilder.highlighter(highlightBuilder);
                
                searchRequest.source(searchSourceBuilder);
                logger.info("Request: "+searchRequest.toString());

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);                
                
                /* Build Response */
                logger.debug("\n\n" + searchResponse.toString());                                

                SearchHits searchHits = searchResponse.getHits();    
                long hitsCount = searchHits.getTotalHits().value;
                logger.info("Total results for the query phrase '"+queryString+"' : "+hitsCount);                                

                for (SearchHit searchHit : searchHits) {                                        
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    String companyName = (String) sourceAsMap.get("company");
                    String year = (String) sourceAsMap.get("year");
                    Integer page = (Integer) sourceAsMap.get("page_no");
                    String content = (String) sourceAsMap.get("content");
                    String pdf_url = (String) sourceAsMap.get("pdf_link");

                    Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                    HighlightField highlight = highlightFields.get("content");
                    Text[] fragments = highlight.fragments();
                    List<String> fragmentString = new ArrayList<String>();
                    for(Text txtFragment : fragments){
                        fragmentString.add(txtFragment.string());
                    }                    
                    searchResults.add(new SearchResult(companyName, year, page, fragmentString, pdf_url, content));                    
                }

                
                SearchData searchData = new SearchData(searchResults);
                searchData.setResultCount(hitsCount);
                logger.debug(searchData.toString());
                
                responseJson = new ResponseJson("OK", searchData);
                client.close();
        } catch (Exception e) {            
            e.printStackTrace();
            logger.debug(e.getMessage());
		}
                
        return responseJson;
    }    
    
    private RestHighLevelClient getClient(){
        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticHostName, elasticPort, "http"));        
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elasticadmin", "3|@$t!c777"));
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback(){        
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }); 
        return new RestHighLevelClient(builder);   
    }
}