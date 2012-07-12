var rdf_engine = {};

var RdfBrowser = {
	facets_URL : "../facets.json",
	initialize:function(params){
		var rightPanelDiv = $('#right-panel');
		var leftPanelDiv = $('#left-panel');
		var viewPanelDiv = $('#view-panel');
		var summaryDiv = $('#summary-bar');
		var pageSizeControlsDiv = $('.viewpanel-pagesize');
		var pageControlsDiv = $('.viewpanel-paging');
		resize(rightPanelDiv,leftPanelDiv,viewPanelDiv);
		var self = this;
		this._engine = new RdfBrowsingEngine(viewPanelDiv,leftPanelDiv,summaryDiv,pageSizeControlsDiv,pageControlsDiv, params,function(){
			$.get(self.facets_URL,{},
				function(facets_data){
					self._engine.addFacets(facets_data);
				}
				,"json");
		});
		
		$('#add_facet').click(function(){
			self._engine.addFacetUI();
		});
		rdf_engine = this._engine;
	}
};

$(function(){
	RdfBrowser.initialize(getUrlVars());
});

function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function resize(rightPanelDiv,leftPanelDiv,viewPanelDiv) {
	
	
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
    
    viewPanelDiv.height((height  - rightPanelVPaddings) + "px");
    
}

RdfBrowser.update = function(onlyFacets){
	rdf_engine.update(onlyFacets);
};

RdfBrowser.refocus = function(refocusFacet){
	rdf_engine.refocus(refocusFacet);
};

RdfBrowser.removeFacet = function(facet_index, update) {
	rdf_engine.removeFacet(facet_index,update);
};