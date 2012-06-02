function RdfBrowsingEngine(resourcesDiv, facetsDiv, summaryDiv, pageSizeControlsDiv, pagingControlsDiv, callback) {
    this._resourcesDiv = resourcesDiv;
    this._facetsDiv = facetsDiv;
    this._summaryDiv = summaryDiv;
    this._pageSizeControls = pageSizeControlsDiv;
    this._pagingControls = pagingControlsDiv;
    
    this._facets = [];
    this._limit = 10; this._offset = 0;
    this._filtered = 0;
    
    var dismissBusy = DialogSystem.showBusy();
	this.configuration_URL = "configuration.json";
	var self = this;
    this.__loadConfig(function(){
    	self.templateEngine = new TemplateEngine(self.config.template,self.config.script_template);
    	dismissBusy();
    },callback);
}

RdfBrowsingEngine.prototype.getJSON = function() {
	var self = this;
    var a = {
    	sparqlEndpointUrl: self._sparqlEndpointUrl,
    	graph: self.config.graph,
    	mainResourcesSelector: unescape(self._mainResourcesSelector),
    	template:self.templateEngine.__template,
        facets: []
    };
    for (var i = 0; i < this._facets.length; i++) {
        var facet = this._facets[i];
        a.facets.push(facet.facet.getJSON());
    }
    return a;
};

RdfBrowsingEngine.prototype.viewResources = function(resources){
	var getValue = function(obj, key){
		if(obj[key]){
				return obj[key][0];
		}else{
			return "";
		}
	};
	var self = this;
	this._resourcesDiv.empty();
	for(var i=0;i<resources.length;i++){
		var r = resources[i];
		self.templateEngine.viewResource(r,this._resourcesDiv,this._sparqlEndpointUrl,self.config);
	}
	
	self._pageSizeControls.empty().append($('<span></span>').html('Show: '));
	var sizes = [ 5, 10, 25, 50 ];
    var renderPageSize = function(index) {
        var pageSize = sizes[index];
        var a = $('<a href="javascript:{}"></a>')
            .addClass("viewPanel-pagingControls-page")
            .appendTo(self._pageSizeControls);
        if (pageSize == self._limit) {
            a.text(pageSize).addClass("selected");
        } else {
            a.text(pageSize).addClass("action").click(function(evt) {
                self._limit = pageSize;
                self.getResources();
            });
        }
    };
    for (var i = 0; i < sizes.length; i++) {
        renderPageSize(i);
    }

    
    self._pagingControls.empty();
    var from = (self._offset + 1);
   	var to = self._filtered?Math.min(self._filtered, self._offset + self._limit):'??';
    
    var firstPage = $('<a href="javascript:{}">&laquo; first</a>').appendTo(self._pagingControls);
    var previousPage = $('<a href="javascript:{}">&lsaquo; previous</a>').appendTo(self._pagingControls);
    if (self._offset > 0) {
        firstPage.addClass("action").click(function(evt) { self._onClickFirstPage(this, evt); });
        previousPage.addClass("action").click(function(evt) { self._onClickPreviousPage(this, evt); });
    } else {
        firstPage.addClass("inaction");
        previousPage.addClass("inaction");
    }
    
    $('<span>').addClass("viewpanel-pagingcount").html(" " + from + " - " + to + " ").appendTo(self._pagingControls);
    
    var nextPage = $('<a href="javascript:{}">next &rsaquo;</a>').appendTo(self._pagingControls);
    var lastPage = $('<a href="javascript:{}">last &raquo;</a>').appendTo(self._pagingControls);
    if (self._filtered===0 || self._offset + self._limit < self._filtered) {
        nextPage.addClass("action").click(function(evt) { self._onClickNextPage(this, evt); });
        lastPage.addClass("action").click(function(evt) { self._onClickLastPage(this, evt); });
    } else {
        nextPage.addClass("inaction");
        lastPage.addClass("inaction");
    }
    
	
};

RdfBrowsingEngine.prototype.viewHeader = function(){
	var self = this;
	$('<span></span>').text(self._filtered + ' matching item').prependTo(this._summaryDiv.empty());
};

RdfBrowsingEngine.prototype.addFacet = function(type, config, options) {
    var elmt = this._createFacetContainer();
    var facet;
    if(type==="list"){
		facet = new RdfPropertyListFacet(elmt, facets[i].config, options);
	}else if (type==="search"){
		facet = new RdfSearchFacet(elmt, facets[i].config, options);
	}else{
		//ignore
		return;
	}

    this._facets.push({ elmt: elmt, facet: facet });
    
    this.update();
};

RdfBrowsingEngine.prototype.addFacets = function(facets) {
	for(var i=0;i<facets.length;i++){
    	var elmt = this._createFacetContainer();
    	var facet = facets[i];
    	if(facet.type==="list"){
    		facet = new RdfPropertyListFacet(elmt, facets[i].config);
    	}else if (facet.type==="search"){
    		facet = new RdfSearchFacet(elmt, facets[i].config);
    	}else if (facet.type==="numeric"){
    		facet = new RdfRangeFacet(elmt,facets[i].config);
    	}else{
    		//ignore
    		continue;
    	}
    
    	this._facets.push({ elmt: elmt, facet: facet });
	}
	
	this.update();
};

RdfBrowsingEngine.prototype._createFacetContainer = function() {
    return $('<li></li>').addClass("facet-container").attr("id","facet-" + this._facets.length).hide().appendTo(this._facetsDiv.find('.facets-container'));
};

RdfBrowsingEngine.prototype.update = function(onlyFacets) {
	var self = this;
	for(var i=0;i<self._facets.length;i++){
		self._facets[i].facet.setLoadingState();
	}
	$.post(
	        "compute-facets",
	        { "rdf-engine": JSON.stringify(this.getJSON(true)) },
	        function(data) {
	        	self._sparqlEndpointUrl = data.sparqlEndpointUrl;
	        	var facetData = data.facets;
	            for (var i = 0; i < facetData.length; i++) {
	                self._facets[i].facet.updateState(facetData[i]);
	            }
	            
	            
	        },"json");
	if(onlyFacets!==true){
		this.getResources(0);
		$.post("count-resources",{"rdf-engine": JSON.stringify(this.getJSON(true))},function(data){
			self._filtered = data.filtered;
			self.viewHeader();
		},"json");
	}
	
};

RdfBrowsingEngine.prototype._onClickFirstPage = function(){
	this.getResources(0);
};
RdfBrowsingEngine.prototype._onClickPreviousPage = function(elmt, evt) {
	this.getResources(this._offset - this._limit);
};
RdfBrowsingEngine.prototype._onClickNextPage = function(elmt, evt) {
	this.getResources(this._offset + this._limit);
};
RdfBrowsingEngine.prototype._onClickLastPage = function(elmt, evt) {
	this.getResources(Math.floor(this._filtered / this._limit) * this._limit);
};

RdfBrowsingEngine.prototype.getResources = function(start,onDone) {
	var self = this;
	if(!start){start=0;}
	var dismissBusy = DialogSystem.showBusy();
	$.post("get-resources?limit=" + this._limit + "&offset=" + start,{"rdf-engine": JSON.stringify(this.getJSON(true))},function(data){
		if(data.code==='error'){
			alert(data.message);
			dismissBusy();
			return;
		}
		self._limit = data.limit;
    	self._offset = data.offset;
    	self.viewResources(data.resources, $('#view-panel'));
		dismissBusy();
	},"json");
};

RdfBrowsingEngine.prototype.__loadConfig = function(callback1,callback2){
	var self = this;
	$.get(self.configuration_URL,{},
			function(raw_data){
		        var data = JSON.parse(raw_data.replace(/\n/g, ' '));
				self.config = data ;
				self._sparqlEndpointUrl = self.config.endpoint_url;
				self._mainResourcesSelector = self.config.main_resource_selector;
				//load CSS
				if(self.config.css){
					RdfBrowsingEngine.__loadCSS(self.config.css);
				}
				//load scripts
				if(self.config.script){
					RdfBrowsingEngine.__loadScript(self.config.script);
				}
				if(callback2){
					callback2();
				}
				if(callback1){
					callback1();
				}
			},"text"
	);	
};

RdfBrowsingEngine.__loadCSS = function(cssFile) {
	$("head").append("<link>");
    css = $("head").children(":last");
    css.attr({
          id: "dynamic_css",
          rel:  "stylesheet",
          type: "text/css",
          href: cssFile
    });
};

RdfBrowsingEngine.__loadScript = function(scriptFile) {
	$.getScript(scriptFile);
};