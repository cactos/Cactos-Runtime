$(document).ready(function() {
	$("i.running").addClass("fa fa-check");
	$('i.shut').addClass("fa fa-times");
	$('i.paused').addClass("fa fa-pause");
	$('i.failure').addClass("fa fa-bolt");
	// setInterval(ajaxCall, 10000);
    
    //active items:
    $('.navbar li').click(function(e){
        $('.navbar li.active').removeClass('active');
        var $this = $(this);
        if (!$this.hasClass('active')){
            $this.addClass('active');
        }
    });
});

function filterList(){
	if ($('#filterRunning').is(':checked')){
		$('li.computeNode.running').css('display', 'inherit');
	} else {
		$('li.computeNode.running').css('display', 'none');
	}
	if ($('#filterShut').is(':checked')){
		$('li.computeNode.shut').css('display', 'inherit');
	} else {
		$('li.computeNode.shut').css('display', 'none'); 
	}
	if ($('#filterPaused').is(':checked')){
		$('li.computeNode.paused').css('display', 'inherit');
	} else {
		$('li.computeNode.paused').css('display', 'none'); 
	}
	if ($('#filterFailure').is(':checked')){
		$('li.computeNode.failure').css('display', 'inherit');
	} else {
		$('li.computeNode.failure').css('display', 'none'); 
	}
}

function searchtext() {
    var sVal = $('#searchBar').val().toLowerCase();
    if (sVal == ""){
        $('.searchList > .searchable').css('display', 'inherit');
    } else {
        $('.searchList > .searchable').each(function(){
            var text = $(this).text().toLowerCase();
            (text.indexOf(sVal) >= 0) ? $(this).css('display', 'inherit') : $(this).css('display', 'none');
        });
    }
}

function toggle() {
	if (!$('#toggle').is(':checked')) {
		$('#toggleDiv :input').attr('disabled', true);
	} else {
		$('#toggleDiv :input').removeAttr('disabled');
	}
}
removeParent = function(e) {
	$(e).parents('div.alterDomainElem').remove();
}
function expand(elem)
{
	elem = jQuery(elem);
	if(elem.css('max-height') == '0px')
	{
		elem.css('max-height', '600px');
		elem.parent().children('a.fa').removeClass('fa-chevron-down').addClass('fa-chevron-up');
	}
	else
	{
		elem.css('max-height', '0px');
		elem.parent().children('a.fa').removeClass('fa-chevron-up').addClass('fa-chevron-down');
	}
}
