function EditFacetExpression(exp,onDone){
	this._expression = exp;
	this._show(onDone);
}

EditFacetExpression.prototype._show = function(onDone){
	var self = this;
	var frame = DialogSystem.createDialog();
    frame.width("420px");
    
    var header = $('<div></div>').addClass("dialog-header").text("Edit facet expression").appendTo(frame);
    var body = $('<div class="grid-layout layout-full"></div>').addClass("dialog-body").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    var html = $(
    	  '<div class="rdf-reconcile-spaced">' + 
    	    'Similar to triple patterns in SPARQL query without the very first and last parts... ' + 
    	  '</div>' +
    	  '<span style="vertical-align: top;">?x </span><textarea cols="40" bind="_newexpression">' + 
    	    self._expression + 
    	  '</textarea>' +
    	  '<span> ?v</span>'
    ).appendTo(body);
    
    self._elmts = DOM.bind(html);
    
    self._level = DialogSystem.showDialog(frame);
    self._footer(footer,onDone);
};

EditFacetExpression.prototype._footer = function(footer,onDone){
	var self = this;
	$('<button></button>').addClass('button').html("&nbsp;&nbsp;Ok&nbsp;&nbsp;").click(function() {
		DialogSystem.dismissUntil(self._level - 1);
		if(onDone){
			var e = self._elmts._newexpression.val();
			onDone(e);
		}
    }).appendTo(footer);
	$('<button></button>').addClass('button').text("Cancel").click(function() {
        DialogSystem.dismissUntil(self._level - 1);
    }).appendTo(footer);
};