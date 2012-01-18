function TemplateEngine(tmplt){
	this.__template = tmplt;
} 

TemplateEngine.prototype.viewResource = function(resource,container){
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