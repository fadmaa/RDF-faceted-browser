function viewObservation(resource,container,endpoint,config){
	obsContainer = $('<div/>').addClass('observation').appendTo(container);
	obsVal = "error";
	for (p in resource){
		if (config.measures.indexOf(p) != -1){
			obsVal = resource[p];
			break;
		}
	}
	
	
	if(resource['http://www.w3.org/2000/01/rdf-schema#label']){
		$('<span/>').html(resource['http://www.w3.org/2000/01/rdf-schema#label'][0] + ': ').appendTo(obsContainer);
	}
	$('<span/>').addClass('observation-value').html(''+obsVal).appendTo(obsContainer);
	
//	$('<div/>').html(resource['@']).appendTo(obsContainer);
	$('<div>').append($('<span></span>').addClass('dimension-name').text('year : '))
			.append($('<span></span>').text(' ' + resource['http://data.lod2.eu/scoreboard/properties/year'])).appendTo(obsContainer);
	$('<div/>').html('country: ' + resource['http://data.lod2.eu/scoreboard/properties/country']).appendTo(obsContainer);
	
	var a_link = $('<a>').attr('href','get-observation-detail?uri=' + escape(resource['@']) + '&endpoint=' + getEndpoint()).text('details').colorbox();
	$('<div/>').append(a_link).appendTo(obsContainer);
	
}