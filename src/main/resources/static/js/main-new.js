$( document ).ready(function() {          
    console.log('Document ready!')

    
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

    populateFilters = function(searchFilters){
        $('#filter-body').empty()
        $.each(searchFilters, function(index, item){
            grp_name = 'group-'+(index+1)
            html = '<div class="btn-group mr-2" role="group" aria-label="'+grp_name+'">';
            html += '<button type="button" onclick="getFilteredResults(\''+item['name']+'\')" class="btn btn-primary btn-sm">';
            html += '<span>'+item['name']+'</span>&nbsp;&nbsp;';
            html += '<span class="badge badge-light">' + item['count'] + '</span></button></div>';                        
            $('#filter-body').append(html)
        })
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

    getFilteredResults = function(filterName){
        filterString = filterName
        getPage(1)
    }
    $('#search-btn').click(function(){ 
        filterString = '';             
        getPage(1)
    });

    // toggleFilters = function(){
    //     if(filtersVisible){            
    //         $('#filter-container').slideUp()
    //         $(this).html('Show Filters&nbsp;&nbsp;&#8681;')            
    //     }else{
    //         $('#filter-container').slideDown()
    //         $(this).html('Hide Filters&nbsp;&nbsp;&#8679;')                        
    //     }
    //     filtersVisible = !filtersVisible
    // }

    // $('#filters-btn').click(function(){
    //     toggleFilters()
    // });             

    getPage = function (pageNumber){        
        $('#modal-container').empty()        
        $('#pagination').hide()
        $('#result-container').html('<img src="/img/loading.gif"/>')
        
        query  = $('#search-input').val()
        if(query==''){
            return false;
        }
        SEARCH_URL = '/search';
        
        var jqxhr = $.get( SEARCH_URL, {'q':query, 'f': filterString, 'p':pageNumber});

        jqxhr.done(function(data, status) {
            if(data['status']=='OK'){
                searchData = data['searchData']
                console.log(searchData)
                
                searchResults = searchData['searchResults']
                searchFilters = searchData['searchFilters']
                resultCount = searchData['resultCount']
                
                if(searchResults.length > 0){                                        
                    populateSearchResults(searchResults)                    
                    refreshPagination(resultCount, pageNumber)
                    populateFilters(searchFilters)
                    $('aside').show()
                }else{
                    $('#pagination ul.pagination').empty()
                    $('#result-container').html('<div id="result-template" class="row"><div class="col-sm"><div class="card text-center"><h5 class="card-header">Found Nothing</h5></div></div></div>')
                }

            }                        
        });

        jqxhr.fail(function(data, status) {
            console.log(data['responseText']);            
        });

        /* jqxhr.always(function(data, status) {
            console.log("Printing this anyways "+data['status']);
        });*/
        
        return false;
    }
});