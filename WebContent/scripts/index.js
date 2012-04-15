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
				url:self.facets_URL,
				success:function(facets_data){
					self._engine.addFacets(facets_data);
				}
				});
		});
		rdf_engine = this._engine;
	}
};

$(function(){
	RdfBrowser.initialize();
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