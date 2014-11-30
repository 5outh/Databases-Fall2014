CONTENT = {};

BASE_URL = window.location.host;

ACTIVE_CONTENT = "";

$(document).ready(function(){
	console.log("jquery");
	
	$('body').on('click', '.getContent', function(e){
		e.preventDefault();
		
		var href = $(this).attr('href');
		
		getContent(href);
		
	});
	
	$('.data-table').DataTable();
});

function getContent(href){
	if(!CONTENT[href]){
		console.log("content does not exist");
		showLoader();
		$.ajax({
			'url' : href,
			'type': 'GET',
			'success' : function(response){
				var element = $(response).appendTo("#data-content"); 
				
				if($(element).prop("tagName") == "TABLE")
					$(element).DataTable();
				
				$(element).find("table").DataTable();
				
				CONTENT[href] = $(element);
				
				$(ACTIVE_CONTENT).hide();
				
				ACTIVE_CONTENT = $(element);
				
				hideLoader();
			},
			
			'error': function(e){
				alert("an error occured... check the console");
				console.log(e);
			}
		});
		
		CONTENT[href] = "aldansdn";
	}
	
	else {
		$(ACTIVE_CONTENT).hide()

		$(CONTENT[href]).show();
		
		ACTIVE_CONTENT = CONTENT[href];
		
	}
}

function showLoader(){
	$("body").append("<div id='loader'></div>");
}

function hideLoader(){
	$('#loader').remove();
}