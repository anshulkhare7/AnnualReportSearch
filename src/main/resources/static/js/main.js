$( document ).ready(function() {          
    console.log('Document ready!')

    var filters = []
    filterString = '';
    filtersVisible = false; 

    $('aside').hide()
    $('#pagination').hide()
    $('#filter-container').hide()

    refreshPagination = function(resultCount, currPage){
        $('#pagination ul.pagination').empty()
        numberOfPages = Math.ceil(resultCount / 10)

        if(currPage>1){
            pageHtml = '<li onclick="getPage(1)" class="page-item"><a class="page-link" href="#" tabindex="-1"><<</a></li>'
            pageHtml = pageHtml + '<li onclick="getPage('+(currPage-1)+')" class="page-item"><a class="page-link" href="#" tabindex="-1"><</a></li>'
        }else{
            pageHtml = '<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1"><<</a></li>'
            pageHtml = pageHtml + '<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1"><</a></li>'            
        }
        for(i=1; i <= numberOfPages; i++){
            if((currPage-i) > 2 || (i-currPage)>2 ){
                continue
            }
            if(i==currPage){                
                pageHtml = pageHtml + '<li class="page-item active"><a class="page-link" href="#">'+i+'</a></li>'
            }else{
                pageHtml = pageHtml + '<li onclick="getPage('+i+')" class="page-item"><a class="page-link" href="#">'+i+'</a></li>'
            }            
        }
        if(currPage==numberOfPages){
            pageHtml = pageHtml + '<li class="page-item disabled"><a class="page-link" href="#">></a></li>'
            pageHtml = pageHtml + '<li class="page-item disabled"><a class="page-link" href="#">>></a></li>'
        }else{
            pageHtml = pageHtml + '<li class="page-item" onclick="getPage('+(currPage+1)+')"><a class="page-link" href="#">></a></li>'            
            pageHtml = pageHtml + '<li class="page-item" onclick="getPage('+numberOfPages+')"><a class="page-link" href="#">>></a></li>'
        }

        $('#pagination ul.pagination').html(pageHtml)
        $('#pagination').show()
    }       

    populateCompanyFilters = function(pageNumber){
        $('#company-filter').empty()
        query  = $('#search-input').val()
        if(query==''){
            return false;
        }
        COMPANY_FILTER_URL = '/companyfilters';
        
        var searchPayload = new Object();
        searchPayload.queryString = query
        searchPayload.pageNumber = pageNumber
        searchPayload.filters = filters
                
        $.ajax({
            url:COMPANY_FILTER_URL,
            type:"POST",
            data:JSON.stringify(searchPayload),
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function(data, status){
                if(data['status']=='OK'){
                    searchData = data['searchData']                    
                    searchFilters = searchData['searchFilters']
                    $.each(searchFilters, function(index, item){
                        html = '<a href="#" onclick="filterByCompany(\''+item['name']+'\')" class="list-group-item">'+item['name']+' <span class="float-right badge badge-light round">'+ item['count']+'</span> </a>'
                        $('#company-filter').append(html)
                    })
                }
            }
        });  
    }

    populateYearFilters = function(pageNumber){
        $('#year-filter').empty()
        query  = $('#search-input').val()
        if(query==''){
            return false;
        }
        YEAR_FILTER_URL = '/yearfilters';

        var searchPayload = new Object();
        searchPayload.queryString = query
        searchPayload.pageNumber = pageNumber
        searchPayload.filters = filters
        
        $.ajax({
            url:YEAR_FILTER_URL,
            type:"POST",
            data:JSON.stringify(searchPayload),
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function(data, status){
                if(data['status']=='OK'){
                    searchData = data['searchData']                    
                    searchFilters = searchData['searchFilters']
                    $.each(searchFilters, function(index, item){
                        html = '<a href="#" onclick="filterByYear(\''+item['name']+'\')" class="list-group-item">'+item['name']+' <span class="float-right badge badge-light round">'+ item['count']+'</span> </a>'
                        $('#year-filter').append(html)
                    })
                }
            }
        });            
    }

    populateSearchResults = function(searchResult){
        $('#result-container').empty()
        $.each(searchResult, function(index, item){                    
            newRow = $('#result-template').clone()                    
            newModal = $('#exampleModalLong').clone()

            newRowId = 'result-'+index
            newModalId = 'modal-'+index

            newRow.attr('id', newRowId)
            newModal.attr('id', newModalId)

            newRow.appendTo("#result-container")
            newModal.appendTo("#modal-container")

            $('#'+newRowId+' h5.card-header').html(item['companyName']+' ('+item['year']+') ')                    
            $('#'+newRowId+' .card-footer').html('Page No: '+item['pageNumber'])
            $.each(item['searchFragment'], function(idx, frg){
                fragment = $('#result-template p.card-text').clone()
                fragment.html(frg)
                $('#'+newRowId+' .card-body').append(fragment).append('<hr/>')                    
            })                                        
            $('#'+newRowId+' .card-body').append('<a href="'+item['pdfUrl']+'" target="_blank" class="btn btn-primary">View PDF</a><span>&nbsp;</span>')
            $('#'+newRowId+' .card-body').append('<a href="#" data-toggle="modal" data-target="#'+newModalId+'" class="btn btn-primary">View Page (text)</a>')
            newRow.show()

            newModal.find('.modal-title').html(item['companyName']+' ('+item['year']+') | Page No: '+item['pageNumber'])
            newModal.find('.modal-body').html(item['content'])                    
        })
    }    

    filterByCompany = function(companyName){
        $.each(filters, function(index, item){
            if(item['filterName']=='company')
            filters.splice(index, 1)    
        })
        filters.push({"filterName":"company", "filterValue":companyName})  
        getPage(1)
        populateYearFilters(1)
        populateCompanyFilters(1)
        refreshSelectedFilters()
    }

    filterByYear = function(year){
        $.each(filters, function(index, item){
            if(item['filterName']=='year')
            filters.splice(index, 1)    
        })
        filters.push({"filterName":"year", "filterValue":year})
        getPage(1)
        populateYearFilters(1)
        populateCompanyFilters(1)
        refreshSelectedFilters()
    }    

    $('#search-btn').click(function(){ 
        filterString = '';     
        filters = []        
        getPage(1)
        populateCompanyFilters(1)
        populateYearFilters(1)
        refreshSelectedFilters()
    });        

    refreshSelectedFilters = function(){
        $('#selected-filters').empty()
        $.each(filters, function(index, item){
            html = '<a href="#" onclick="removeFilter(\''+item['filterName']+'\')" class="list-group-item">'+item['filterValue']+' <span class="float-right badge badge-light round"> &#10060;</span> </a>'
            $('#selected-filters').append(html)
        })
    }

    removeFilter = function(filtername){        
        $.each(filters, function(index, item){
            if (item['filterName']==filtername) {                
                filters.splice(index, 1)
            }
        })
        getPage(1)
        populateCompanyFilters(1)
        populateYearFilters(1)
        refreshSelectedFilters()
    }

    getPage = function (pageNumber){        
        $('#modal-container').empty()        
        $('#pagination').hide()
        $('#result-container').html('<img src="/img/loading.gif"/>')
        
        query  = $('#search-input').val()
        if(query==''){
            return false;
        }
        SEARCH_URL = '/search';
                
        var searchPayload = new Object();
        searchPayload.queryString = query
        searchPayload.pageNumber = pageNumber
        searchPayload.filters = filters        

        $.ajax({
            url:SEARCH_URL,
            type:"POST",
            data:JSON.stringify(searchPayload),
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function(data, status){
                if(data['status']=='OK'){
                    searchData = data['searchData']                                        
                    searchResults = searchData['searchResults']                
                    resultCount = searchData['resultCount']
                    
                    if(searchResults.length > 0){                                        
                        populateSearchResults(searchResults)                    
                        refreshPagination(resultCount, pageNumber)                    
                        $('aside').show()
                    }else{
                        $('#pagination ul.pagination').empty()
                        $('#result-container').html('<div id="result-template" class="row"><div class="col-sm"><div class="card text-center"><h5 class="card-header">Found Nothing</h5></div></div></div>')
                    }
    
                }  
            }
          })            
        return false;
    }
});

