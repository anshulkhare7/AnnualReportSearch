$( document ).ready(function() {          

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

    $('#pagination').hide()

    $('#search-btn').click(function(){           
        getPage(1)
    });

    getPage = function (pageNumber){        
        $('#modal-container').empty()        
        $('#pagination').hide()
        $('#result-container').html('<img src="https://wpamelia.com/wp-content/uploads/2018/11/ezgif-2-6d0b072c3d3f.gif"></a>')

        query  = $('#search-input').val()
        if(query==''){
            return false;
        }
        SEARCH_URL = '/search';
        
        var jqxhr = $.get( SEARCH_URL, {'q':query, 'p':pageNumber});

        jqxhr.done(function(data, status) {
            if(data['status']=='OK'){
                searchResult = data['searchResults']
                if(searchResult.length > 0){
                    refreshPagination(data['resultCount'], pageNumber)
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
                        $('#'+newRowId+' .card-body').append('<a href="#" data-toggle="modal" data-target="#'+newModalId+'" class="btn btn-primary">View Page (text)</a>')                    
                        newRow.show()

                        newModal.find('.modal-title').html(item['companyName']+' ('+item['year']+') | Page No: '+item['pageNumber'])
                        newModal.find('.modal-body').html(item['content'])                    
                    })
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