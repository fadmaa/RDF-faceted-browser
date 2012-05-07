/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

function RdfPropertyListFacet(div, config, options, selection) {
    this._div = div;
    this._config = config;
    if (!("invert" in this._config)) {
        this._config.invert = false;
    }
    
    this._options = options || {};
    if (!("sort" in this._options)) {
        this._options.sort = "name";
    }
    
    this._selection = selection || [];
    this._blankChoice = (config.selectBlank) ? { s : true, c : 0 } : null;
    this._errorChoice = (config.selectError) ? { s : true, c : 0 } : null;
    
    this._data = null;
    
    this._initializeUI();
    this._update();
}

RdfPropertyListFacet.reconstruct = function(div, uiState) {
    return new RdfPropertyListFacet(div, uiState.c, uiState.o, uiState.s);
};

RdfPropertyListFacet.prototype.dispose = function() {
};

RdfPropertyListFacet.prototype.reset = function() {
    this._selection = [];
    this._blankChoice = null;
    this._errorChoice = null;
};

RdfPropertyListFacet.prototype.getUIState = function() {
    var json = {
        c: this.getJSON(),
        o: this._options
    };
    
    json.s = json.c.selection;
    delete json.c.selection;
    
    return json;
};

RdfPropertyListFacet.prototype.getJSON = function() {
    var o = {
        type: "rdf-property-list",
        name: this._config.name,
        property: this._config.property,
        expression: this._config.expression,
        omitBlank: "omitBlank" in this._config ? this._config.omitBlank : false,
        omitError: "omitError" in this._config ? this._config.omitError : false,
        selection: [],
        selectBlank: this._blankChoice !== null && this._blankChoice.s,
        selectError: this._errorChoice !== null && this._errorChoice.s,
        invert: this._config.invert
    };
    for (var i = 0; i < this._selection.length; i++) {
        var choice = {
            v: cloneDeep(this._selection[i].v)
        };
        o.selection.push(choice);
    }
    return o;
};

RdfPropertyListFacet.prototype.hasSelection = function() {
    return this._selection.length > 0 || 
        (this._blankChoice !== null && this._blankChoice.s) || 
        (this._errorChoice !== null && this._errorChoice.s);
};

RdfPropertyListFacet.prototype.setLoadingState = function(){
    this._elmts.bodyInnerDiv.empty().append(
            $('<div>').text("Loading...").addClass("facet-body-message"));
};

RdfPropertyListFacet.prototype.updateState = function(data) {
    this._data = data;
    
    if ("choices" in data) {
        var selection = [];
        var choices = data.choices;
        for (var i = 0; i < choices.length; i++) {
            var choice = choices[i];
            if (choice.s) {
                selection.push(choice);
            }
        }
        this._selection = selection;
        this._reSortChoices();
    
        this._blankChoice = data.blankChoice || null;
        this._errorChoice = data.errorChoice || null;
    }
    
    this._update();
};

RdfPropertyListFacet.prototype._reSortChoices = function() {
    this._data.choices.sort(this._options.sort == "name" ?
        function(a, b) {
            return a.v.l.localeCompare(b.v.l);
        } :
        function(a, b) {
            var c = b.c - a.c;
            return c !== 0 ? c : a.v.l.localeCompare(b.v.l);
        }
    );
};

RdfPropertyListFacet.prototype._initializeUI = function() {
    var self = this;
    
    var facet_id = this._div.attr("id");
    
    this._div.empty().show().html(
        '<div class="facet-title">' +
            '<div class="grid-layout layout-tightest layout-full"><table><tr>' +
                '<td width="1%"></td>' +
                '<td>' +
                	'<a href="javascript:{}" class="facet-choice-link" bind="changeButton">change</a>' +
                    '<a href="javascript:{}" class="facet-choice-link" bind="resetButton">reset</a>' +
                    '<span bind="titleSpan"></span>' +
                '</td>' +
            '</tr></table></div>' +
        '</div>' +
        '<div class="facet-expression" bind="expressionDiv" title="Click to edit expression"></div>' +
        '<div class="facet-controls" bind="controlsDiv" style="display:none;">' +
          '<a bind="choiceCountContainer" class="action" href="javascript:{}"></a> <span class="facet-controls-sortControls" bind="sortGroup">Sort by: ' +
            '<a href="javascript:{}" bind="sortByNameLink">name</a>' +
            '<a href="javascript:{}" bind="sortByCountLink">count</a>' +
          '</span>' +
        '</div></div>' +
        '<div class="facet-body" bind="bodyDiv">' +
            '<div class="facet-body-inner" bind="bodyInnerDiv"></div>' +
        '</div>'
    );
    this._elmts = DOM.bind(this._div);
    
    //this._elmts.titleSpan.text(this._config.name);
    if(this._config.propertyUri){
      //make facet name a link
      var facetNameAnchor = $('<a></a>').attr('href','dereference?uri=' + escape(this._config.propertyUri)).attr('title',this._config.propertyUri).text(this._config.name);
      facetNameAnchor.colorbox();
      this._elmts.titleSpan.append(facetNameAnchor);
    }else{
      this._elmts.titleSpan.text(this._config.name);	
    }
    this._elmts.expressionDiv.text(this._config.expression).hide().click(function() { self._editExpression(); });
    this._elmts.resetButton.click(function() { self._reset(); });
    this._elmts.changeButton.click(function() { self._change(); });

    this._elmts.choiceCountContainer.click(function() { self._copyChoices(); });
    this._elmts.sortByCountLink.click(function() {
        if (self._options.sort != "count") {
            self._options.sort = "count";
            self._reSortChoices();
            self._update(true);
        }
    });
    this._elmts.sortByNameLink.click(function() {
        if (self._options.sort != "name") {
            self._options.sort = "name";
            self._reSortChoices();
            self._update(true);
        }
    });
        
    if (this._config.expression != "value" && this._config.expression != "grel:value") {
        this._elmts.clusterLink.hide();
    }
    
    if (!("scroll" in this._options) || this._options.scroll) {
        this._elmts.bodyDiv.addClass("facet-body-scrollable");
        this._elmts.bodyDiv.resizable({
            minHeight: 30,
            handles: 's',
            stop: function(event, ui) {
                event.target.style.width = "auto"; // don't force the width
            }
        });
    }
};

RdfPropertyListFacet.prototype._copyChoices = function() {
    var self = this;
    var frame = DialogSystem.createDialog();
    frame.width("600px");
    
    var header = $('<div></div>').addClass("dialog-header").text("Facet Choices as Tab Separated Values").appendTo(frame);
    var body = $('<div></div>').addClass("dialog-body").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    body.html('<textarea wrap="off" bind="textarea" style="display: block; width: 100%; height: 400px;" />');
    var elmts = DOM.bind(body);
    
    $('<button class="button"></button>').text("Close").click(function() {
        DialogSystem.dismissUntil(level - 1);
    }).appendTo(footer);
    
    var lines = [];
    for (var i = 0; i < this._data.choices.length; i++) {
        var choice = this._data.choices[i];
        lines.push(choice.v.l + "\t" + choice.c);
    }
    if (this._blankChoice) {
        lines.push("(blank)\t" + this._blankChoice.c);
    }
    if (this._errorChoice) {
        lines.push("(error)\t" + this._errorChoice.c);
    }
    
    var level = DialogSystem.showDialog(frame);
    
    var textarea = elmts.textarea[0];
    textarea.value = lines.join("\n");
    textarea.focus();
    textarea.select();
};

RdfPropertyListFacet.prototype._update = function(resetScroll) {
    var self = this;
    
    if (!this._data) {
        //this._elmts.statusDiv.hide();
        this._elmts.controlsDiv.hide();
        this._elmts.bodyInnerDiv.empty().append(
            $('<div>').text("Loading...").addClass("facet-body-message"));
            
        return;
    } else if ("error" in this._data) {
        //this._elmts.statusDiv.hide();
        this._elmts.controlsDiv.hide();
        this._elmts.bodyInnerDiv.empty().append(
            $('<div>').text(this._data.error).addClass("facet-body-message"));
        
        if (this._data.error == "Too many choices") {
            this._renderBodyControls();
        }
        return;
    }
    
    var scrollTop = 0;
    if (!resetScroll) {
        try {
            scrollTop = this._elmts.bodyInnerDiv[0].scrollTop;
        } catch (e) {
        }
    }
    
    this._elmts.bodyInnerDiv.empty();
    //this._elmts.statusDiv.show();
    this._elmts.controlsDiv.show();
    
    var choices = this._data.choices;
    var selectionCount = this._selection.length +
          (this._blankChoice !== null && this._blankChoice.s ? 1 : 0) +
          (this._errorChoice !== null && this._errorChoice.s ? 1 : 0);
          
    this._elmts.choiceCountContainer.text(choices.length + " choices");
    
    if (this._options.sort == "name") {
        this._elmts.sortByNameLink.removeClass("action").addClass("selected");
        this._elmts.sortByCountLink.removeClass("selected").addClass("action");
    } else {
        this._elmts.sortByNameLink.removeClass("selected").addClass("action");
        this._elmts.sortByCountLink.removeClass("action").addClass("selected");
    }
    
    var html = [];
    var temp = $('<div>');
    var encodeHtml = function(s) {
        return temp.text(s).html();
    };
    
    var renderChoice = function(index, choice, customLabel) {
        var label = customLabel || choice.v.l;
        var count = choice.c;
        
        html.push('<div class="facet-choice' + (choice.s ? ' facet-choice-selected' : '') + '" choiceIndex="' + index + '">');
        
            // include/exclude link
            html.push(
                '<a href="javascript:{}" class="facet-choice-link facet-choice-toggle" ' +
                    'style="visibility: ' + (choice.s ? 'visible' : 'hidden') + '">' + 
                    (choice.s ? 'exclude': 'include') +  
                '</a>'
            );
            
            html.push('<a href="javascript:{}" class="facet-choice-label">' + encodeHtml(label) + '</a>');
            //popup 
            if(choice.v && choice.v.v && choice.v.v.indexOf('<')===0 && choice.v.v.match(">$")==">"){
            	html.push('<a class="colorbox-link" href="dereference?uri=' + escape(choice.v.v.substring(1,choice.v.v.length-1)) + '" ><img src="images/icon_popup_search.gif" /></a>' );
    		}
            html.push('<span class="facet-choice-count">' + count + '</span>');
            
        html.push('</div>');
    };
    for (var i = 0; i < choices.length; i++) {
        renderChoice(i, choices[i]);
    }
    if (this._blankChoice !== null) {
        renderChoice(-1, this._blankChoice, "(blank)");
    }
    if (this._errorChoice !== null) {
        renderChoice(-2, this._errorChoice, "(error)");
    }
   
    //setting the popups    
    var htmlObj = $(html.join('')); htmlObj.find('a.colorbox-link').colorbox();
    
    this._elmts.bodyInnerDiv.empty().append(htmlObj);//html(html.join(''));
   
    this._renderBodyControls();
    this._elmts.bodyInnerDiv[0].scrollTop = scrollTop;
    
    var getChoice = function(elmt) {
        var index = parseInt(elmt.attr("choiceIndex"),10);
        if (index == -1) {
            return self._blankChoice;
        } else if (index == -2) {
            return self._errorChoice;
        } else {
            return choices[index];
        }
    };
    var findChoice = function(elmt) {
        return getChoice(elmt.closest('.facet-choice'));
    };
    var select = function(choice) {
        self._select(choice, false);
    };
    var selectOnly = function(choice) {
        self._select(choice, true);
    };
    var deselect = function(choice) {
        self._deselect(choice);
    };
    
    var wireEvents = function() {
        var bodyInnerDiv = self._elmts.bodyInnerDiv;
        bodyInnerDiv.find('.facet-choice-label').click(function() {
            var choice = findChoice($(this));
            if (choice.s) {
                if (selectionCount > 1) {
                    selectOnly(choice);
                } else {
                    deselect(choice);
                }
            } else if (selectionCount > 0) {
                selectOnly(choice);
            } else {
                select(choice);
            }
        });
        bodyInnerDiv.find('.facet-choice-edit').click(function() {
            var choice = findChoice($(this));
            self._editChoice(choice, $(this).closest('.facet-choice'));
        });
        
        bodyInnerDiv.find('.facet-choice').mouseenter(function() {
            $(this).find('.facet-choice-edit').css("visibility", "visible");
            
            var choice = getChoice($(this));
            if (!choice.s) {
                $(this).find('.facet-choice-toggle').css("visibility", "visible");
            }
        }).mouseleave(function() {
            $(this).find('.facet-choice-edit').css("visibility", "hidden");

            var choice = getChoice($(this));
            if (!choice.s) {
                $(this).find('.facet-choice-toggle').css("visibility", "hidden");
            }
        });
        
        bodyInnerDiv.find('.facet-choice-toggle').click(function() {
            var choice = findChoice($(this));
            if (choice.s) {
                deselect(choice);
            } else {
                select(choice);
            }
        });
    };
    window.setTimeout(wireEvents, 100);
};

RdfPropertyListFacet.prototype._renderBodyControls = function() {
    var self = this;
    var bodyControls = $('<div>')
        .addClass("facet-body-controls")
        .appendTo(this._elmts.bodyInnerDiv);
        
};

RdfPropertyListFacet.prototype._getMetaExpression = function() {
    var r = Scripting.parse(this._config.expression);
    
    return r.language + ':facetCount(' + [
        r.expression,
        JSON.stringify(this._config.expression),
        JSON.stringify(this._config.property)
    ].join(', ') + ')';
}

RdfPropertyListFacet.prototype._doEdit = function() {
    new ClusteringDialog(this._config.property, this._config.expression);
};

RdfPropertyListFacet.prototype._editChoice = function(choice, choiceDiv) {
    var self = this;
    
    var menu = MenuSystem.createMenu().addClass("data-table-cell-editor").width("400px");
    menu.html(
              '<textarea class="data-table-cell-editor-editor" bind="textarea" />' +
              '<div id="data-table-cell-editor-actions">' +
                '<div class="data-table-cell-editor-action">' +
                  '<button class="button" bind="okButton">Apply</button>' +
                  '<div class="data-table-cell-editor-key">Enter</div>' +
                '</div>' +
                '<div class="data-table-cell-editor-action">' +
                  '<button class="button" bind="cancelButton">Cancel</button>' +
                    '<div class="data-table-cell-editor-key">Esc</div>' +
                '</div>' +
              '</div>'
    );
    var elmts = DOM.bind(menu);
    
    MenuSystem.showMenu(menu, function(){});
    MenuSystem.positionMenuLeftRight(menu, choiceDiv);
    
    var originalContent;
    if (choice === this._blankChoice) {
        originalContent = "(blank)";
    } else if (choice === this._errorChoice) {
        originalContent = "(error)";
    } else {
        originalContent = choice.v.v;
    }
    
    var commit = function() {
        var text = elmts.textarea[0].value;
        
        MenuSystem.dismissAll();
        
        var edit = { to : text };
        if (choice === self._blankChoice) {
            edit.fromBlank = true;
        } else if (choice === self._errorChoice) {
            edit.fromError = true;
        } else {
            edit.from = [ originalContent ];
        }
        
        Refine.postCoreProcess(
            "mass-edit",
            {},
            {
                property: self._config.property,
                expression: "value",
                edits: JSON.stringify([ edit ])
            },
            {
                // limit edits to rows constrained only by the other facets
                engineConfig: ui.browsingEngine.getJSON(false, self),
                cellsChanged: true
            },
            {
                onDone: function(o) {
                    var selection = [];
                    var gotSelection = false;
                    for (var i = 0; i < self._selection.length; i++) {
                        var choice = self._selection[i];
                        if (choice.v.v == originalContent) {
                            if (gotSelection) {
                                continue;
                            }
                            choice.v.v = text;
                            gotSelection = true; // eliminate duplicated selections due to changing one selected choice to another
                        }
                        selection.push(choice);
                    }
                    self._selection = selection;
                }
            }
        );            
    };
    
    elmts.okButton.click(commit);
    elmts.textarea
        .text(originalContent)
        .keydown(function(evt) {
            if (!evt.shiftKey) {
                if (evt.keyCode == 13) {
                    commit();
                } else if (evt.keyCode == 27) {
                    MenuSystem.dismissAll();
                }
            }
        })
        .select()
        .focus();
        
    elmts.cancelButton.click(function() {
        MenuSystem.dismissAll();
    });
};

RdfPropertyListFacet.prototype._select = function(choice, only) {
    if (only) {
        this._selection = [];
        if (this._blankChoice !== null) {
            this._blankChoice.s = false;
        }
        if (this._errorChoice !== null) {
            this._errorChoice.s = false;
        }
    }
    
    choice.s = true;
    if (choice !== this._errorChoice && choice !== this._blankChoice) {
        this._selection.push(choice);
    }
    
    this._updateRest();
};

RdfPropertyListFacet.prototype._deselect = function(choice) {
    if (choice === this._errorChoice || choice === this._blankChoice) {
        choice.s = false;
    } else {
        for (var i = this._selection.length - 1; i >= 0; i--) {
            if (this._selection[i] === choice) {
                this._selection.splice(i, 1);
                break;
            }
        }
    }
    this._updateRest();
};

RdfPropertyListFacet.prototype._reset = function() {
    this._selection = [];
    this._blankChoice = null;
    this._errorChoice = null;
    this._config.invert = false;
    
    this._updateRest();
};

RdfPropertyListFacet.prototype._change = function() {
	var self = this;
	new EditFacetExpression(self._config.property,function(e) {
		self._config.property = e;
//		self._update();
		RdfBrowser.update(true);
	});
};

RdfPropertyListFacet.prototype._invert = function() {
    this._config.invert = !this._config.invert;
    
    this._updateRest();
};

RdfPropertyListFacet.prototype._remove = function() {
    ui.browsingEngine.removeFacet(this);
    
    this._div = null;
    this._config = null;
    
    this._selection = null;
    this._blankChoice = null;
    this._errorChoice = null;
    this._data = null;
};

RdfPropertyListFacet.prototype._updateRest = function() {
	RdfBrowser.update();
};

RdfPropertyListFacet.prototype._editExpression = function() {
    var self = this;
    var title = (this._config.property) ? 
            ("Edit Facet's Expression based on Column " + this._config.property) : 
            "Edit Facet's Expression";
    
    var column = Refine.columnNameToColumn(this._config.property);
    var o = DataTableView.sampleVisibleRows(column);
    
    new ExpressionPreviewDialog(
        title,
        column ? column.cellIndex : -1, 
        o.rowIndices,
        o.values,
        this._config.expression, 
        function(expr) {
            if (expr != self._config.expression) {
                self._config.expression = expr;
                
                self._elmts.expressionDiv.text(self._config.expression);
                if (self._config.expression == "value" || self._config.expression == "grel:value") {
                    self._elmts.clusterLink.show();
                } else {
                    self._elmts.clusterLink.hide();
                }
                
                self.reset();
                self._updateRest();
            }
        }
    );
};

function cloneDeep(o) {
    if (o === undefined || o === null) {
      return o;
    } else if (o instanceof Function) {
      return o;
    } else if (o instanceof Array) {
      var a = [];
      for (var i = 0; i < o.length; i++) {
        a.push(cloneDeep(o[i]));
      }
      return a;
    } else if (o instanceof Object) {
      var a = {};
      for (var n in o) {
        if (o.hasOwnProperty(n)) {
          a[n] = cloneDeep(o[n]);
        }
      }
      return a;
    } else {
      return o;
    }
}