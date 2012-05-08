var rdf_engine = {};

var RdfBrowser = {
	facets_URL : "get-facets",
	initialize:function(){
		var rightPanelDiv = $('#right-panel');
		var rightPanelHeaderDiv = $('#right-panel-header');
		var leftPanelDiv = $('#left-panel');
		var viewPanelDiv = $('#view-panel');
		var summaryDiv = $('#summary-bar');
		var pageSizeControlsDiv = $('.viewpanel-pagesize');
		var pageControlsDiv = $('.viewpanel-paging');
		resize(rightPanelDiv,leftPanelDiv,viewPanelDiv,rightPanelHeaderDiv);
		var self = this;
		this._engine = new RdfBrowsingEngine(viewPanelDiv,leftPanelDiv,summaryDiv,pageSizeControlsDiv,pageControlsDiv, function(){
			$.ajax({
				url:self.facets_URL+ "?endpoint=" + getEndpoint(),
				success:function(facets_data){
					self._engine.addFacets(facets_data);
				}
				});
		});
		rdf_engine = this._engine;
	}
};

function getEndpoint(){
	var url = location.href;
	return url.substring(url.indexOf("?endpoint=") + 10)
}
var export_rdf = function(format){
	return function(){
	var form = document.createElement("form");
    $(form)
        .css("display", "none")
        .attr("method", "post")
        .attr("action", "export-rdf")
        .attr("target","rdf-export")
        ;
    $('<input />')
        .attr("name", "rdf-engine")
        .attr("value", JSON.stringify(rdf_engine.getJSON()))
        .appendTo(form);
    $('<input />')
        .attr("name", "format")
        .attr("value", format)
        .appendTo(form);

    document.body.appendChild(form);

    window.open("about:blank", "rdf-export");
    form.submit();

    document.body.removeChild(form);
	}
};

var export_json = function(){
	var form = document.createElement("form");
    $(form)
        .css("display", "none")
        .attr("method", "post")
        .attr("action", "export-json")
        .attr("target","json-export")
        ;
    $('<input />')
        .attr("name", "rdf-engine")
        .attr("value", JSON.stringify(rdf_engine.getJSON()))
        .appendTo(form);

    document.body.appendChild(form);

    window.open("about:blank", "json-export");
    form.submit();

    document.body.removeChild(form);
};

$(function(){
	RdfBrowser.initialize();
	
	$("#export").click(function(){
		MenuSystem.createAndShowStandardMenu(
		        [
		         {
		        	 "id":"export-ttl",
		        	 "label":"export in TURTLE",
		        	 "click":export_rdf("TURTLE")
		        	 
		         },
		         {
		        	 "id":"export-rdf-xml",
		        	 "label":"export in RDF/XML",
		        	 "click":export_rdf("RDF/XML")
		         },
		         {
		        	 "id":"export-json",
		        	 "label":"export in JSON-LD",
		        	 "click":export_json
		         }
		         
		        ],
		        this,
		        { horizontal: false }
		    );
		
	});
});

function resize(rightPanelDiv,leftPanelDiv,viewPanelDiv,rightPanelHeaderDiv) {
	
	
    var header = $("#header");
    
    var leftPanelWidth = 300;
    var width = $(window).width();
    var top = $("#header").outerHeight();
    var height = $(window).height() - top;
    
    var leftPanelPaddings = leftPanelDiv.outerHeight(true) - leftPanelDiv.height();
    leftPanelDiv
        .css("top", top + "px")
        .css("left", "0px")
        .css("height", (height - leftPanelPaddings) + "px")
        .css("width", leftPanelWidth + "px");
        
    var rightPanelVPaddings = rightPanelDiv.outerHeight(true) - rightPanelDiv.height();
    var rightPanelHPaddings = rightPanelDiv.outerWidth(true) - rightPanelDiv.width();
    rightPanelDiv
        .css("top", top + "px")
        .css("left", leftPanelWidth + "px")
        .css("height", (height - rightPanelVPaddings) + "px")
        .css("width", (width - leftPanelWidth - rightPanelHPaddings) + "px");
    
    rightPanelHeaderDiv.css("width", (width - leftPanelWidth - rightPanelHPaddings) + "px");
    
    viewPanelDiv.height((height  - rightPanelVPaddings) + "px");
    
}

RdfBrowser.update = function(onlyFacets){
	rdf_engine.update(onlyFacets);
};