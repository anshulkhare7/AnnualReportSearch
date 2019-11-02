package com.anshul.arsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping(value="/search")
    public ResponseJson getMethodName(@RequestParam(name = "q") String queryString, @RequestParam(name = "f") String filterString,
                                             @RequestParam(name = "p") Integer pageNumber) {        
                        
        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticHostName, elasticPort, "http"));        
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elasticadmin", "3|@$t!c777"));
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback(){        
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }); 
        RestHighLevelClient client = new RestHighLevelClient(builder);

        logger.info("Initiating queryString: [" + queryString + "] | filterString: ["+filterString+"]");
        
        ResponseJson responseJson = new ResponseJson();
        List<SearchResult> searchResults =  new ArrayList<SearchResult>();        
        List<SearchFilter> searchFilters = new ArrayList<SearchFilter>();
        SearchData searchData = new SearchData(searchResults, searchFilters);

        SearchRequest searchRequest = new SearchRequest();
        
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        try {                 
                MatchPhraseQueryBuilder matchPhraseQueryBuilder = new MatchPhraseQueryBuilder("content", queryString);
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();                                
                boolQueryBuilder.must(matchPhraseQueryBuilder);

                if(null != filterString && !filterString.isEmpty() && !filterString.equalsIgnoreCase("all")){
                    boolQueryBuilder.must(new MatchPhraseQueryBuilder("company", filterString));
                }

                searchSourceBuilder.query(boolQueryBuilder);
                searchSourceBuilder.size(10);
                searchSourceBuilder.from(pageNumber-1);

                HighlightBuilder highlightBuilder = new HighlightBuilder();
                HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content");
                highlightContent.highlighterType("plain");                
                highlightBuilder.field(highlightContent);

                searchSourceBuilder.highlighter(highlightBuilder);

                AggregationBuilder aggregationBuilder = new TermsAggregationBuilder("group_by_company", null).field("company.keyword");
                searchSourceBuilder.aggregation(aggregationBuilder);

                searchRequest.source(searchSourceBuilder);
                logger.info("Request: "+searchRequest.toString());

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                
                /* Build Response */
                logger.debug("\n\n" + searchResponse.toString());
                
                ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get("group_by_company");                
                parsedStringTerms.getBuckets().stream().forEachOrdered( bucket -> {
                    Terms.Bucket bckt = (Terms.Bucket) bucket;
                    searchFilters.add(new SearchFilter(bckt.getKey().toString(), bckt.getDocCount()));
                });                

                SearchHits searchHits = searchResponse.getHits();    
                long hitsCount = searchHits.getTotalHits().value;
                logger.info("Total results for the query phrase '"+queryString+"' : "+hitsCount);
                searchData.setResultCount(hitsCount);
                searchFilters.add(new SearchFilter("All", hitsCount));

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
                logger.debug(searchData.toString());
                
                responseJson = new ResponseJson("OK", searchData);
                client.close();
        } catch (Exception e) {            
            e.printStackTrace();
            logger.debug(e.getMessage());
		}
                
        return responseJson;
    }    
}