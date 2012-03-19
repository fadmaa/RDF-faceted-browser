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

function RdfSearchFacet(div, config, options) {
  this._div = div;
  this._config = config;
  this._options = options;

  this._query = config.query || null;
  this._endpointVendor = config.endpoint_vendor;
  this._property = config.property;
  this._timerID = null;

  this._initializeUI();
}

RdfSearchFacet.reconstruct = function(div, uiState) {
  return new RdfSearchFacet(div, uiState.c, uiState.o);
};

RdfSearchFacet.prototype.dispose = function() {
};

RdfSearchFacet.prototype.reset = function() {
  this._query = null;
  this._div.find(".input-container input").each(function() { this.value = ""; });
};

RdfSearchFacet.prototype.getUIState = function() {
  var json = {
      c: this.getJSON(),
      o: this._options
  };

  return json;
};

RdfSearchFacet.prototype.getJSON = function() {
  var o = {
      type: "rdf-search",
      name: this._config.name,
      query: this._query||'',
      endpoint_vendor: this._endpointVendor,
      property:this._property
  };
  return o;
};

RdfSearchFacet.prototype.hasSelection = function() {
  return this._query !== null;
};

RdfSearchFacet.prototype._initializeUI = function() {
  var self = this;
  this._div.empty().show().html(
      '<div class="facet-title">' + 
      '<div class="grid-layout layout-tightest layout-full"><table><tr>' +
      '<td width="1%"><a href="javascript:{}" title="Remove this facet" class="facet-title-remove" bind="removeButton">&nbsp;</a></td>' +
      '<td>' +
      '<span>' + this._config.name + '</span>' +
      '</td>' +
      '</tr></table></div>' +
      '</div>' +
      '<div class="facet-text-body"><div class="grid-layout layout-tightest layout-full"><table>' +
      '<tr><td><div class="input-container"><input bind="input" /></div></td>' +
      '<td><div class="input-container"><input type="button" value="search" bind="search"/></div></td>' + 
      '</tr>' +
      '</table></div></div>'
  );

  var elmts = DOM.bind(this._div);

  elmts.removeButton.click(function() { self._remove(); });

  if (this._query) {
    elmts.input[0].value = this._query;
  }
  elmts.search.bind('click',function(){
	  self._query = elmts.input.val();
	  RdfBrowser.update({ engineChanged: true });
  });
};

RdfSearchFacet.prototype.updateState = function(data) {
};

RdfSearchFacet.prototype.render = function() {
  this._setRangeIndicators();
};

RdfSearchFacet.prototype._reset = function() {
  this._query = null;
  this._updateRest();
};

RdfSearchFacet.prototype._remove = function() {
  ui.browsingEngine.removeFacet(this);

  this._div = null;
  this._config = null;
  this._options = null;
};

RdfSearchFacet.prototype._scheduleUpdate = function() {
  if (!this._timerID) {
    var self = this;
    this._timerID = window.setTimeout(function() {
      self._timerID = null;
      self._updateRest();
    }, 500);
  }
};

RdfSearchFacet.prototype._updateRest = function() {
	RdfBrowser.update({ engineChanged: true });
};

RdfSearchFacet.prototype.setLoadingState = function(){
	
};