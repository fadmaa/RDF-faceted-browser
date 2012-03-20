function TemplateEngine(tmplt,scriptTmplt){
	this.__scriptTemplate = scriptTmplt;
	this.__template = scriptTmplt?"":tmplt;
} 

TemplateEngine.prototype.viewResource = function(resource,container,endpoint){
	if(this.__scriptTemplate){
		this.__viewResourceUsingScript(resource,container,endpoint);
	}else{
		this.__viewResourceUsingTemplate(resource,container);
	}
};

TemplateEngine.prototype.__viewResourceUsingScript = function(resource,container,endpoint){
	var myFunc = window[this.__scriptTemplate];
	myFunc(resource,container,endpoint);
};

TemplateEngine.prototype.__viewResourceUsingTemplate = function(resource,container){
	var dom = $(this.__template);
	$.each(dom.find('[sparql_content]'),function(key,val){
		var k = $(val).attr('sparql_content');
		if(resource[k]){
			//FIXME taking only the first value... what about lists
			$(val).text(resource[k][0]);
		}else{
			$(val).text('undefined');
		}
	});
	container.append(dom);	
};