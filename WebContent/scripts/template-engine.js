TemplateEngine = {
	config:null,
	configuration_URL : "configuration.json",
	viewResource : function(resource,container){
		//load configuration.json
		//FIXME this should happen only once... relocate
		var self = this;
		if(!self.config){
			self.loadConfigAndRender(resource,container);
		}else{
			self.__renderResource(resource, container);
		}
	},
	
	loadConfigAndRender:function(resource,container){
		var self = this;
		$.ajax({
			url:self.configuration_URL,
			success:function(data){
				self.config = jQuery.parseJSON( data );
				self.__renderResource(resource,container);
			}
		});	
	},
	
	__renderResource: function(resource,container){
		var dom = $(this.config.template);
		$.each(dom.find('[sparql_content]'),function(key,val){
			var k = $(val).attr('sparql_content');
			if(resource[k]){
				//FIXME taking only hte first value... what about lists
				$(val).text(resource[k][0]);
			}else{
				$(val).text('undefined');
			}
		});
		container.append(dom);
	},
	
	__loadConfig: function(callback){
		var self = this;
		$.ajax({
			url:self.configuration_URL,
			success:function(data){
				self.config = jQuery.parseJSON( data );
				if(callback){
					callback();
				}
			}
		});	
	}
};