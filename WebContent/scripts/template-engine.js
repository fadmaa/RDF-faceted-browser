function TemplateEngine(tmplt,scriptTmplt){
	this.__scriptTemplate = scriptTmplt;
	this.__template = scriptTmplt?"":tmplt;
} 

TemplateEngine.prototype.viewResource = function(resource,container,endpoint,config){
	if(this.__scriptTemplate){
		this.__viewResourceUsingScript(resource,container,endpoint,config);
	}else{
		this.__viewResourceUsingTemplate(resource,container);
	}
};

TemplateEngine.prototype.__viewResourceUsingScript = function(resource,container,endpoint,config){
	var myFunc = window[this.__scriptTemplate];
	myFunc(resource,container,endpoint,config);
};

TemplateEngine.prototype.__viewResourceUsingTemplate = function(resource,container){
	var dom = $(this.__template);
	$.each(dom.find('[sparql_content]'),function(key,val){
		var props = $(val).attr('sparql_content');
		var propsArr = props.split(' ');
		for(var i=0;i<propsArr.length;i++){
			var k = propsArr[i];
			if(resource[k]){
				if(k==="@"){
					$(val).text(resource[k]);
				}else{
					//	FIXME taking only the first value... what about lists
					$(val).text(resource[k][0]);
				}
				break;
			}
		}
	});
	
	$.each(dom.find('[sparql_attribute]'),function(key,val){
		var s = $(val).attr('sparql_attribute');
		var attribute_name = s.substring(0,s.indexOf(':'));
		var props = s.substring(s.indexOf(':')+1);
		var propsArr = props.split(' ');
		for(var i=0;i<propsArr.length;i++){
			var k = propsArr[i];
			if(resource[k]){
				if(k==="@"){
					$(val).attr(attribute_name,resource[k]);
				}else{
					//	FIXME taking only the first value... what about lists
					$(val).attr(attribute_name,resource[k][0]);
				}
				break;
			}
		}
	});
	
	container.append(dom);	
};