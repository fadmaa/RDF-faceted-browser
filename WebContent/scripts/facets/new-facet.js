function NewFacetUI(onDone){
	this._show(onDone);
}

NewFacetUI.prototype._show = function(onDone){
	var self = this;
	var frame = DialogSystem.createDialog();
    frame.width("540px");
    
    var header = $('<div></div>').addClass("dialog-header").text("Add a new facet").appendTo(frame);
    var body = $('<div class="grid-layout layout-full"></div>').addClass("dialog-body").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    var html = $(
    	  '<table class="new-facet-table">' +
    	    '<tr>' +
    	      '<td>Property Path:</td>' +
    	      '<td>' +
    	        '<textarea cols="40" bind="_facet_pp"></textarea>' +
    	        '<br/><span class="note">Example: &lt;http://xmlns.com/foaf/0.1/member&gt; / &lt;http://www.w3.org/2000/01/rdf-schema#&gt;</span>' +
    	      '</td>' +
    	    '</tr>' +
    	    '<tr>' +
  	          '<td>Facet name:</td>' +
  	          '<td>' +
  	            '<input type="text" bind="_facet_name" />' +
  	          '</td>' +
  	        '</tr>' +
  	        '<tr>' +
              '<td>Facet variable name:</td>' +
              '<td>' +
                '<input type="text" bind="_facet_varname" />' +
              '</td>' +
            '</tr>' +
    	  '</table>'
    ).appendTo(body);
    
    self._elmts = DOM.bind(html);
    
    self._level = DialogSystem.showDialog(frame);
    self._footer(footer,onDone);
};

NewFacetUI.prototype._footer = function(footer,onDone){
	var self = this;
	$('<button></button>').addClass('button').html("&nbsp;&nbsp;Ok&nbsp;&nbsp;").click(function() {
		DialogSystem.dismissUntil(self._level - 1);
		if(onDone){
			var pp = self._elmts._facet_pp.val();
			var name = self._elmts._facet_name.val();
			var varname = self._elmts._facet_varname.val();
			var newfacet = {
				name:name,
				varname:varname,
				filter:{pattern:pp}
			};
			onDone(newfacet);
		}
    }).appendTo(footer);
	$('<button></button>').addClass('button').text("Cancel").click(function() {
        DialogSystem.dismissUntil(self._level - 1);
    }).appendTo(footer);
};