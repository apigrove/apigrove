/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership. Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */

$(document).ready(function() {

    var types = new Array("property", "tdrRule", "headerTrans", "targethost", "parameter","header");
    for(var i = 0; i<types.length; i++){
        var items = document.getElementsByClassName(types[i]);
        var itemCount = items.length;
        if(types[i] == "property") var propertyCount = itemCount;
        else if(types[i] == "tdrRule") var tdrCount = itemCount;
        else if(types[i] == "headerTrans") var headerTransCount = itemCount;
        else if(types[i] == "targethost") var targetHostCount = itemCount;
        else  if(types[i] == "parameter") var parameterCount = itemCount;
        else  if(types[i] == "header") var headerCount = itemCount;
    }

    $('.newItem').click(function(event){
        var type = $(this).attr('type');
        newItem(type);
        return false;
    });

    $('button.btn').click(function(event){
        event.preventDefault();
    });

    $('.moveSelected').click(function(event){
        event.preventDefault();
        var pid = $(this).closest("div").attr("id");
        var name = $(this).attr("name");
        var type = $(this).attr("type");
        var id = $(this).attr("id");
        var upperType = toTitleCase(type);
        if (pid == "select"+upperType) {
            var hidden = "<input type='hidden' class='"+id+"' id='"+name+"' name='selected_"+type+"[]' value='"+(type == "api" ? name : id)+"'>";
            $(this).slideUp("fast", function() {
                $(this).appendTo('#selected'+upperType).slideDown('fast');
                $('#selected'+upperType+'Placeholder').remove();
            });
            $('#clear'+upperType).append(hidden);
        }else{
            var hidden = document.getElementById(name);
            $(hidden).remove();
            $(this).slideUp("fast", function() {
                $(this).appendTo('#select'+upperType).slideDown('fast');
                if($('#selected'+upperType).children().length == 0){
                    $('#selected'+upperType).append('<p id="selected'+upperType+'Placeholder" style="font-size:14px; color:#5e5e5e">Selected '+upperType+'s</p>');
                }
            });
        }
    });

    $('a.btn[inputId]').click(function(event){
        event.preventDefault();
        // Checkbox button value attr must be undefined (uses "0" or "1")
        // Radio button must have a defined value (to assign to hidden input)
        var isCheckOption = ($(this).attr("value")  === undefined);
        var inputId = $(this).attr("inputId");
        if (isCheckOption) {
            // Checkbox button
            if ($(this).attr("checked") != null) {
                $("input#"+inputId).attr("value", 0);
                $(this).removeAttr("checked");
            } else {
                $("input#"+inputId).attr("value", 1);
                $(this).attr("checked", "checked");
            }

        } else {
            // Radio buttton
            $("input#"+inputId).attr("value", $(this).attr("value"));
        }

        if($(this).hasClass("auth")){
            handleAuthTypeClick($(this), 'fast');
        }

        if($(this).hasClass("api")){
            handleProvAuthTypeClick($("input#"+inputId), 'fast');
        }

    });

    /**
     * Onclick handler for the "howMany" button group
     * If the "More" button is clicked we want to expose the howMany field
     * We also want to hide the credential inputs so that they can be auto-completed
     */
    $('a.howMany').click(function(){
        var currentAuthType = $("#type").val();
        var ele = $(".auth[value='"+currentAuthType+"']");
        if(this.id === "howMany-one"){
            $("#howMany").val("1");
            $("#howMany").hide('fast');
            handleAuthTypeClick(ele, 'fast');
            $('#authtype-ipwhitelist').show('fast');
            $('#authId-ctlgroup').show('fast');

        }
        else if(this.id === "howMany-more"){
            $("#howMany").show('fast');
            $("#howMany").val('');
            $('#un-pw').hide('fast');
            $('#authKey').hide('fast');
            $('#authId-ctlgroup').hide('fast');

            // IpWhiteList doesn't make sense to batch, so we
            // hide it and need to select another auth (authKey) if
            // white list was already selected
            $('#authtype-ipwhitelist').hide('fast');
            if(currentAuthType === "ipWhiteList"){
                $(".auth[value='authKey']").click();
            }
        }
    });

    $('.removeItem').live("click", function(){
        var type = $(this).attr('type');
        var number = $(this).attr('number');
        var element = type + "Group" + number;
        var removeMe = document.getElementById(element);
        removeMe.parentNode.removeChild(removeMe);
        return false;
    });

    $('#showAdvanced').click(function(){
        $('#advancedSection').toggle('fast');
        $('.advbtn').toggle();
        return false;
    });

    $('.headerAction').live("change", function(){
        var id = $(this).attr('item');
        var val = $(this).val();
        if(val == 'Add'){
            $(id).show('fast');
        }else{
            $(id).hide();
        }
    });

    function makeAutocompleteProperties(field){
    	if($(field).smartAutoComplete() === undefined){
    		var container = $("<ul class='dropdown-menu smart_autocomplete_container' style='display:none'></ul>");
    		container.appendTo("body");
	        $(field).smartAutoComplete({
	        	disabled: false,
	            source: jQuery.parseJSON($("#relatedPropertiesTree").text()),
	            resultsContainer: container,
	            resultFormatter: function(r){ return ("<li class=\"smart_autocomplete_item\"><a href=\"#\">" + r + "</a></li>"); },
	            alignResultsContainer: true,
	
	            filter: function(searchTerm, source){
	                var context = this;
	                var options = $(context).data('smart-autocomplete');
	
	                // reduce a bunch of leading / with just one leading /
	                while(searchTerm.indexOf("//") === 0){
	                    searchTerm = searchTerm.substring(1, searchTerm.length);
	                }
	
	                var parentTerms = null;
	                var childTerm = null;
	
	                if(searchTerm.search("/") === -1){
	                    // the search contains no forward slashes, so leave the parent array empty
	                    parentTerms = [];
	                    childTerm = searchTerm;
	                } else {
	                    // separate the parent and child by the last "/"
	                    // and the split the parent into an array
	                    parentTerms = searchTerm.substring(0, searchTerm.lastIndexOf("/")).split("/");
	                    childTerm = searchTerm.substring(searchTerm.lastIndexOf("/")+1, searchTerm.length);
	                }
	
	                var pathArrToString = function(arr){
	                    var ret = "";
	                    while(arr[0] === ""){
	                        arr = arr.splice(1, arr.length);
	                    }
	                    for(var key in arr){
	                        ret = ret + "/" + arr[key];
	                    }
	                    return ret;
	                }
	
	                var parentCheck = function(pathArr){
	                    if(parentTerms.length > pathArr.length){
	                        return false;
	                    }
	                    for(var i = 1; i <= parentTerms.length; i++){
	                        if(parentTerms[parentTerms.length - i] !== pathArr[pathArr.length - i]){
	                            return false;
	                        }
	                    }
	                    return true;
	                }
	
	                var leafMatcher = function (items, pathArr, results){
	                    for(var keyforitem in items){
	                        var item = items[keyforitem];
	                        if(parentCheck(pathArr) && item.search(childTerm) !== -1){
	                            results.push(pathArrToString(pathArr.concat(item)));
	                        }
	                    }
	                }
	
	                var recursiveTreeMatcher = function (jsonObject, pathArr, results){
	                    var keys = Object.keys(jsonObject);
	                    for(var keyforkeys in keys){
	                        var key = keys[keyforkeys];
	
	                        if( parentCheck(pathArr) && key.search(childTerm) !== -1){
	                            results.push(pathArrToString(pathArr.concat(key)));
	                        }
	                        var child = jsonObject[key];
	                        if($.type(child) === "object"){
	                            results = recursiveTreeMatcher(child, pathArr.concat(key), results);
	                        } else if ($.type(child) === "array"){
	                            leafMatcher(child, pathArr.concat(key), results);
	                        }
	                    }
	                    return results;
	                }
	
	                return recursiveTreeMatcher(source, [""], []);
	            }
	        });
	        $(field).bind({
	
	            itemSelect: function(ev, selected_item){
	                var options = $(this).smartAutoComplete();
	
	                //get the text from selected item
	                var selected_value = $(selected_item).text();
	
	                while(selected_value.indexOf("/") === 0){
	                    selected_value = selected_value.substring(1, selected_value.length);
	                }
	                
	                var splitValues = selected_value.split("/");
	
	                if(splitValues.length < 3){
	                    $(this).val($(selected_item).text() + "/");
	
	                    options.setItemSelected(false);
	                    options.currentSelection = 0;
	
	                    options.originalCharCount = $(this).val().length;
	
	                    $(this).trigger('keyIn', [$(this).val()]);
	                } else {
	
	                    $(this).val(splitValues[splitValues.length-1]);
	
	                    //set item selected property
	                    options.setItemSelected(true);
	
	                    //set number of current chars in field
	                    options.originalCharCount = $(this).val().length;
	
	                    $(this).trigger('lostFocus');
	                }
	                ev.preventDefault();
	            }
	        });
    	} else {
	    $(field).smartAutoComplete()["disabled"]=false;
    	}
    }
    
    function removeAutocompleteProperties(field){
	if($(field).smartAutoComplete() !== undefined){
	    var options = $(field).smartAutoComplete();
	    options["disabled"]=true;
	}
    }

    $('.tdrType').live("change", function(){
        var id = $(this).attr('item');
        var val = $(this).val();
        if(val == 'Dynamic'){
            $("#tdrRuleValue"+id).attr('placeholder','Http Header Name');
            removeAutocompleteProperties($("#tdrRuleValue"+id));
            $("#tdrRuleExtractFrom"+id).show('fast');
        }
        else if(val == 'Static'){
            $("#tdrRuleValue"+id).attr('placeholder','Value');
            removeAutocompleteProperties($("#tdrRuleValue"+id));
            $("#tdrRuleExtractFrom"+id).hide('fast');
        }
        else if(val == 'Property'){
            $("#tdrRuleValue"+id).attr('placeholder','Property Name');
            makeAutocompleteProperties($("#tdrRuleValue"+id));
            $("#tdrRuleExtractFrom"+id).hide('fast');
        }
    });

    $('.headerTransformType').live("change", function(){
        var id=$(this).attr('item');
        var val = $(this).val();
        if(val == 'Static' ){
            $("#headerValue"+id).attr('placeholder','Value');
            removeAutocompleteProperties($("#headerValue"+id));
        } else if(val == 'Property' ){
            $("#headerValue"+id).attr('placeholder','Property');
            makeAutocompleteProperties($("#headerValue"+id));
        }
    });



    $('.enabled').click(function(event){
        var me = $(this).attr('name');
        switch(me){
            case 'enabled-true':
                $('input.'+me).val(1);
                $('input.enabled-false').val(0);
                break;
            case 'enabled-false':
                $('input.'+me).val(1);
                $('input.enabled-true').val(0);
        }
    });

    $('.auth-enabled').click(function(event){
        $('#status').val($(this).val());
    });

    $(document).ready(function(event){
        handleAuthTypeClick($("#type"), "fast");
        handleProvAuthTypeClick($("#provauth-authKey"), "fast");
        handleProvAuthTypeClick($("#https"), "fast");
        handleProvAuthTypeClick($("#tdrsenabled"), "fast");
        setupDeleteButtons();
        setupTooltipsAndPopovers();
    });

    /**
     * The onclick handler for the authType button group.
     * We want to show different kinds of credential fields depending
     * on the type of auth that is selected.
     *
     * It is complicated by the current setting of the "howMany" button group
     * If the "more" button is selected then we don't want to show the credential inputs because they
     * will be auto-generated.
     *
     * @param ele
     * @param speed
     */
    function handleAuthTypeClick(ele, speed){
        var type = ele.attr('value');

        switch(type){
            case 'basic':
                $('#IPs').hide(speed);
                $('#authKey').hide(speed);
                if($("#howMany").val()==="1")
                    $('#un-pw').show(speed);
                break;
            case "authKey":
                $('#IPs').hide(speed);
                $('#un-pw').hide(speed);
                if($("#howMany").val()==="1")
                    $('#authKey').show(speed);
                break;
            case 'wsse':
                $('#IPs').hide(speed);
                $('#authKey').hide(speed);
                if($("#howMany").val()==="1")
                    $('#un-pw').show(speed);
                break;
            case 'ipWhiteList':
                $('#authKey').hide(speed);
                $('#un-pw').hide(speed);
                $('#IPs').show(speed);
                break;
        }
    }

    /**
     * To control the "method" buttons on the "callapi" page
     */
    $(".callapi-method").click(function(event){
        $('#method').val($(this).attr('value'));
        if($(this).attr('value') !== "GET"){
            $('#request-body').show('fast');
        }
        else{
            $('#request-body').hide('fast');
        }
    });


    function handleProvAuthTypeClick(ele, speed){
        var id = $(ele).attr("id");
        var enabled = $(ele).val();

        switch(id){
            case 'provauth-authKey':
                if(enabled == 1){
                    $('#authkey-key-span').show(speed);
                } else {
                    $('#authkey-key-span').hide(speed);
                }
                break;

            case 'https':
                if(enabled == 1){
                    $('#https-mode-span').show(speed);
                } else {
                    $('#https-mode-span').hide(speed);
                }
                break;
            case 'tdrsenabled':
                if(enabled == 1){
                    $('#tdr-rule-span').show(speed);
                } else {
                    $('#tdr-rule-span').hide(speed);
                }
                break;
        }

        updateSubmitAndAuthButton();
    }

    function updateSubmitAndAuthButton(){
        if($("#provauth-authKey").val() === "1" ||
            $("#provauth-basic").val() === "1" ||
            $("#provauth-wsse").val() === "1" ||
            $("#provauth-ipWhiteList").val() === "1")
        {

            $("#submitAndAuthButton").show('fast');
        }
        else{
            $("#submitAndAuthButton").hide('fast');
        }
    }

    var type = $('#type').attr('value');
    var button = $('[name="'+type+'"]');
    handleAuthTypeClick(button, null);


    function newItem(type){
        if(type == 'targethost'){
            var itemBody = targetBody();
        }else if(type == 'property'){
            var itemBody = propertyBody();
        }else if(type == 'parameter'){
            var itemBody = parameterBody();
            $('#parameterKey0').focus();
        }else if(type == 'tdrRule'){
            var itemBody = tdrBody();
        }else if(type == 'headerTrans'){
            var itemBody = headerTransBody();
        }else if(type === 'header'){
            var itemBody = headerBody();
            $('#headerKey0').focus();
        }
        var parentID = "#" + type + "Group";
        $(parentID).append(itemBody);
    }

    function targetBody(){
        var body = "<div class=\"control-group targethost\" count=\""+targetHostCount+"\" id=\"targethostGroup"+targetHostCount+"\">"
            +"<label class=\"control-label\" for=\"targethost"+targetHostCount+"\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-xlarge\" name=\"targethost"+targetHostCount+"\" id=\"targethost"+targetHostCount+"\"> "
            +"<a class=\"btn removeItem\" type=\"targethost\" number=\""+targetHostCount+"\" title=\"Remove target host\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div>";
        targetHostCount++;
        return body;
    }

    function propertyBody(){
        var body = "<div class=\"control-group property\" count=\""+propertyCount+"\" id=\"propertyGroup"+propertyCount+"\">"
            +"<label class=\"control-label\" for=\"properties"+propertyCount+"\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small\" name=\"property["+propertyCount+"][name]\" placeholder=\"Key\" id=\"properties"+propertyCount+"\"> "
            +"<input type=\"text\" class=\"input-small\" name=\"property["+propertyCount+"][value]\" placeholder=\"Value\"> "
            +"<a class=\"btn removeItem\" type=\"property\" number=\""+propertyCount+"\" title=\"Remove property\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div";
        propertyCount++;
        return body;
    }

    function parameterBody(){
        var key = $('#parameterKey0').val();
        var value = $('#parameterValue0').val();
        $('#parameterKey0').val("");
        $('#parameterValue0').val("");
        var body = "<div class=\"control-group parameter\" count=\""+parameterCount+"\" id=\"parameterGroup"+parameterCount+"\">"
            +"<label class=\"control-label\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small parameter-key\" name=\"parameterKey"+parameterCount+"\" value=\""+key+"\" placeholder=\"Key\" id=\"parameterKey"+parameterCount+"\"> "
            +"<input type=\"text\" class=\"input-small parameter-value\" name=\"propertyValue"+parameterCount+"\" value=\""+value+"\" placeholder=\"Value\" id=\"parameterValue"+parameterCount+"\"> "
            +"<a class=\"btn removeItem updateParams\" type=\"parameter\" number=\""+parameterCount+"\" title=\"Remove parameter\" id=\"paramRemove"+parameterCount+"\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div";
        parameterCount++;
        return body;
    }

    function headerBody(){
        var key = $('#headerKey0').val();
        var value = $('#headerValue0').val();
        $('#headerKey0').val("");
        $('#headerValue0').val("");
        var body = "<div class=\"control-group header\" count=\""+headerCount+"\" id=\"headerGroup"+headerCount+"\">"
            +"<label class=\"control-label\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small header-key\" name=\"headerKey"+headerCount+"\" value=\""+key+"\" placeholder=\"Key\" id=\"headerKey"+headerCount+"\"> "
            +"<input type=\"text\" class=\"input-small header-value\" name=\"headerValue"+headerCount+"\" value=\""+value+"\" placeholder=\"Value\" id=\"headerValue"+headerCount+"\"> "
            +"<a class=\"btn removeItem updateHeaders\" type=\"header\" number=\""+headerCount+"\" title=\"Remove Header\" id=\"headerRemove"+headerCount+"\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div";
        headerCount++;
        return body;
    }

    function tdrBody(){
        var body = "<div class=\"control-group tdrRule\" count=\""+tdrCount+"\" id=\"tdrRuleGroup"+tdrCount+"\">"
            +"<label class=\"control-label\" for=\"tdrRules"+tdrCount+"\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small\" name=\"tdr["+tdrCount+"][name]\" placeholder=\"Name\" id=\"tdrRules"+tdrCount+"\"> "
            +"<select name=\"tdr["+tdrCount+"][type]\" class=\"input-small tdrType\" item=\""+tdrCount+"\">"
            +"<option>Type</option>"
            +"<option>Static</option>"
            +"<option>Dynamic</option>"
            +"<option>Property</option>"
            +"</select> "
            +"<select class=\"input-small\" name=\"tdr["+tdrCount+"][extractFrom]\" style=\"display: none; width: 120px;\" id=\"tdrRuleExtractFrom"+tdrCount+"\">"
            +"<option>Extract From</option>"
            +"<option>Either</option>"
            +"<option>Request</option>"
            +"<option>Response</option>"
            +"</select> "
            +"<input type=\"text\" class=\"input-small\" name=\"tdr["+tdrCount+"][value]\" placeholder=\"Value\"  id=\"tdrRuleValue"+tdrCount+"\" style=\"width: 120px;\"/> "
            +"<a class=\"btn removeItem\" type=\"tdrRule\" number=\""+tdrCount+"\" title=\"Remove TDR rule\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div>";
        tdrCount++;
        return body;
    }

    function headerTransBody(){
        var body = "<div class=\"control-group headerTrans\" count=\""+headerTransCount+"\" id=\"headerTransGroup"+headerTransCount+"\">"
            +"<label class=\"control-label\" for=\"headerTrans\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small\" name=\"header["+headerTransCount+"][name]\" placeholder=\"Name\" id=\"headerTrans\"> "
            +"<select name=\"header["+headerTransCount+"][timing]\" class=\"input-small\"> "
            +"<option>Timing</option>"
            +"<option>Request</option>"
            +"<option>Response</option>"
            +"</select> "
            +"<select name=\"header["+headerTransCount+"][action]\" item=\"#headerAdv"+headerTransCount+"\" class=\"input-small headerAction\">"
            +"<option>Action</option>"
            +"<option>Add</option>"
            +"<option>Remove</option>"
            +"</select> "
            +"<span id=\"headerAdv"+headerTransCount+"\" style=\"display:none\">"
            +"<select item=\""+headerTransCount+"\" name=\"header["+headerTransCount+"][type]\" class=\"input-small\">"
            +"<option>Type</option>"
            +"<option>Property</option>"
            +"<option>Static</option>"
            +"</select> "
            +"<input id=\"headerValue"+headerTransCount+"\" type=\"text\" class=\"input-small\" name=\"header["+headerTransCount+"][value]\" placeholder=\"Value\"></span> "
            +"<a class=\"btn removeItem\" type=\"headerTrans\" number=\""+headerTransCount+"\" title=\"Remove header transformation\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div>";
        headerTransCount++
        return body;
    }

    $('.buildCall').click(function(){
        var url = $(this).attr('endpoint') + "/" + $(this).attr('id') + "/test-call";
        if(url.indexOf("http://") == -1) var url = "http://" + url;
        document.getElementById('showUrl').innerHTML = url;
        document.getElementById('hiddenInput').innerHTML = "<input type='hidden' id=\"hiddenField\" url=\""+url+"\" value='"+url+"' name='url'/>";
        $('#urlForm').show('fast');
    });

    function filterResults(type){
        var text = document.getElementById('filter'+type).value.toLowerCase();
        var elements = document.getElementsByClassName('filter'+type);
        if($('button.filter'+type+':contains'))
        for (var i = 0; i < elements.length; ++i) {
            var item = elements[i];
            var filterBy = $(item).attr('filter').toLowerCase();
            if(text.indexOf(" ") != -1){
                var filterArray = text.split(" ");
                var a = 0;
                for(var b=0;b<filterArray.length; b++){
                    if(filterArray[b] == "") break;
                    if(a>0) break;
                    if(filterBy.indexOf(filterArray[b]) == -1) a++;
                }
                if(a == 0){
                    $(item).show();
                }else{
                    $(item).hide();
                }
            }else{
                if(filterBy.indexOf(text) == -1){
                    $(item).hide();
                }else{
                    $(item).show();
                }
            }
        }
    }

    $('.filterMe').keyup( function(){
        var type = $(this).attr("filterType");
        filterResults(type);
    });

    $('.updateParams').click( function(){
        updateParams();
    });

    $('.updateParams').live("click", function(){
        updateParams();
    });

    $('.parameter-key, .parameter-value').live("keyup", updateParams);

    $('.makeCall').click(function(){
        var url = $('#hiddenField').attr('url');
        makeRequest(url);
    });

    function makeRequest(url) {

        var params = {};
        var keys = document.getElementsByClassName('parameter-key');
        var keysCount = (keys.length);
        var values = document.getElementsByClassName('parameter-value');
        var valuesCount = (values.length);
        var i;
        for(i=0; i<keysCount; i++){

            var id = keys[i].id;
            var key = $('#'+id).val();
            var id = values[i].id;
            var value = $('#'+id).val();
            params["params["+key+"]"] = value;
        }

        var headers = "";
        keys = document.getElementsByClassName('header-key');
        keysCount = (keys.length);
        values = document.getElementsByClassName('header-value');
        valuesCount = (values.length);

        for(i=0; i<keysCount; i++){

            var id = keys[i].id;
            var key = $('#'+id).val();
            var id = values[i].id;
            var value = $('#'+id).val();
            params["headers[]"] = key+": "+value;
        }

        params['method'] = $("#method").val();
        params['url'] = url;
        params['body'] = $("#request-body-field").val();

        var urlString = "/makeCall";
        $.post(urlString, params, HandleResponse, 'json');

    }

    var apiCallCount = 1;
    function HandleResponse(response) {
        var container = $('#responses');

        var group = $(document.createElement("div"));
        group.addClass("accordion-group");
        group.attr('id', "response"+apiCallCount);
        container.prepend(group);

        var heading = $(document.createElement("div"));
        heading.addClass("accordion-heading");
        group.append(heading);

        var headingLink = $(document.createElement("a"));
        headingLink.addClass("accordion-toggle");
        headingLink.attr('data-toggle','collapse');
        headingLink.attr('data-parent',"#responses");
        headingLink.attr('href',"#responseBody"+apiCallCount);
        headingLink.html("<strong>Response "+apiCallCount+"</strong>");
        heading.append(headingLink);

        var body = $(document.createElement("div"));
        body.addClass("accordion-body collapse in");
        body.attr('id',"responseBody"+apiCallCount)
        group.append(body);

        var bodyInner = $(document.createElement("div"));
        bodyInner.addClass("accordion-inner");
        body.append(bodyInner);

        var headerH3 = $(document.createElement("h4"));
        headerH3.text("Header")
        bodyInner.append(headerH3);

        var headerPre = $(document.createElement("pre"));
        headerPre.text(response.header)
        bodyInner.append(headerPre);

        var bodyH3 = $(document.createElement("h4"));
        bodyH3.text("Body")
        bodyInner.append(bodyH3);

        var bodyPre = $(document.createElement("pre"));
        bodyPre.attr('id','body'+apiCallCount);
        bodyPre.text(response.body)
        bodyInner.append(bodyPre);

        container.show();
        $('#response-placeholder').hide();
        apiCallCount++;
    }

    function updateParams(){
        var keys = document.getElementsByClassName('parameter-key');
        var keysCount = (keys.length);
        var values = document.getElementsByClassName('parameter-value');
        var valuesCount = (values.length);
        var url = $('#hiddenField').attr('url');
        var i;
        for(i=0; i<keysCount; i++){
            if(i == 0){
                url += "?";
            }else{
                url+= "&"
            }
            var id = keys[i].id;
            var key = $('#'+id).val();
            var id = values[i].id;
            var value = $('#'+id).val();
            url += key + "=" + value;
        }
        document.getElementById('showUrl').innerHTML = url;
        $('#hiddenField').val(escape(url));
    }

    // Do we want the user to confirm & cancel Delete clicks with "OK" & "Cancel" buttons in a popup,
    // or a new "Confirm Delete" button overlaid on top of original Delete button?
    var CONFIRM_DELETE_IN_POPUP = true;
    var activeDeleteButton = null;

    /**
     * If a Delete button is showing a confirm Popover, reset that state by
     * hiding the Popover and, if we're using the "Confirm Delete" overlay button,
     * hiding the Confirm button and showing the original Delete button.
     */
    function resetDeletePopovers() {
        if (activeDeleteButton !== null) {
            if (CONFIRM_DELETE_IN_POPUP) {
                activeDeleteButton.popover("hide");
            } else {
                activeDeleteButton.confirmButton.popover("hide");
                activeDeleteButton.confirmButton.hide();
                activeDeleteButton.show();
            }
            activeDeleteButton = null;
        }
    }

    /**
     * Constructs the HTML body for a Delete-confirm popover (with "OK" and "Cancel" buttons).
     *
     * @param deleteButton  The original Delete button
     * @return {String} The HTML string to use as the Popover content
     */
    function deletePopoverBody(deleteButton) {
        var deleteRef = deleteButton.attr("href");
        var deleteId = deleteButton.attr("id")+"-confirm";
        var okText = deleteButton.attr("confirm-ok") !== undefined ? deleteButton.attr("confirm-ok") : "OK";
        var cancelText = deleteButton.attr("confirm-cancel") !== undefined ? deleteButton.attr("confirm-cancel") : "Cancel";

        // Currently uses &nbsp; to space buttons apart - could use a better method!
        var body = "<div class=\"btn-toolbar\">"
            +"<div class=\"btn-group\">"
            +"<a class=\"btn btn-danger\" id=\""+deleteId+"\" style=\"min-width:50px;\" href=\""+deleteRef+"\">"+okText+"</a>"
            +"</div>"
            +"&nbsp;&nbsp;&nbsp;&nbsp;"
            +"<div class=\"btn-group\">"
            +"<a class=\"btn\" style=\"min-width:50px;\">"+cancelText+"</a>"
            +"</div>"
            +"</div>";
        return body;
    }

    function setupDeleteButtons() {

        var deleteButtonCount = 0;

        $("a.delete").each(function(index, element){
            deleteButtonCount++;
            var me = $(element);
            var confirm = null;
            var confirmTitle = me.attr("confirm-title") !== undefined ? me.attr("confirm-title") : "Are you sure?";
            var confirmContent = me.attr("confirm-content") !== undefined ? me.attr("confirm-content") : "Click again to confirm";


            if (CONFIRM_DELETE_IN_POPUP) {
                me.popover({trigger:"manual", title:confirmTitle, html:true, content:deletePopoverBody(me)});
            } else {
                confirm = me.clone();
                me.parent().append(confirm);
                confirm.attr("id", me.attr("id")+"-confirm");
                confirm.text("Confirm " + me.text());
                confirm.hide();
                me.confirmButton = confirm;
            }

            me.click(function(event){
                event.preventDefault();
                event.stopPropagation();
                if (activeDeleteButton != me) {
                    resetDeletePopovers();
                    if (CONFIRM_DELETE_IN_POPUP) {
                        me.popover("show");
                    } else {
                        me.hide();
                        confirm.show();
                        // Create popover here after confirm.show(), otherwise placement is janky
                        confirm.popover({trigger:"manual", title:confirmTitle, content:confirmContent});
                        confirm.popover("show");
                    }
                    activeDeleteButton = me;
                }
            });
        });

        // Only add global click handler(s) if this page actually has delete buttons
        if (deleteButtonCount > 0) {
            // TODO: doesn't trigger on mobile menu (nav) select
            $(":not('a.delete')").click(function() {
                resetDeletePopovers();
            });
        }
    }

    function setupTooltipsAndPopovers() {

        // If this constant is true, text input fields show tooltips on focus
        // If false, tips are shown on hover (default behavior)
        var TEXT_INPUT_TIP_ON_FOCUS = false;
        var TOOLTIP_DELAY_SHOW = 2000;
        var TOOLTIP_DELAY_HIDE = 0;

        $("[rel=tooltip]").each(function(index, element) {
            var me = $(element);
            if (TEXT_INPUT_TIP_ON_FOCUS && (me.prop('tagName') === "INPUT") && (me.prop('type') === 'text')) {
                me.tooltip({trigger:"focus", delay:{show: TOOLTIP_DELAY_SHOW, hide: TOOLTIP_DELAY_HIDE}});
            } else {
                me.tooltip({trigger:"hover", delay:{show: TOOLTIP_DELAY_SHOW, hide: TOOLTIP_DELAY_HIDE}});
            }
        });

        // Attach the popover to the defining element, unless it defines a "for" attribute,
        // in which case attach the popover to the "for" target
        $("[rel=popover]").each(function(index, element){
            var me = $(element);
            var forControl = ((me.attr('for') !== undefined) ? $('#'+me.attr('for')) : null);

            if (forControl !== null) {
                forControl.popover({trigger:'manual', title:me.attr('data-title'), content:me.attr('data-content'), placement:me.attr('data-placement')});
            } else {
                me.popover();
            }

            me.click(function(event) {
                if (forControl !== null) {
                    forControl.popover('toggle');
                }
            });
        });
    }

    $.fn.ellipsis = function()
    {
        return this.each(function()
        {
            var el = $(this);

            if(el.css("overflow") == "hidden")
            {
                var text = el.html();
                var multiline = el.hasClass('multiline');
                var t = $(this.cloneNode(true))
                    .hide()
                    .css('position', 'absolute')
                    .css('overflow', 'visible')
                    .width(multiline ? el.width() : 'auto')
                    .height(multiline ? 'auto' : el.height())
                    ;

                el.after(t);

                function height() { return t.height() > el.height(); };
                function width() { return t.width() > el.width(); };

                var func = multiline ? height : width;

                while (text.length > 0 && func())
                {
                    text = text.substr(0, text.length - 1);
                    t.html(text + "...");
                }

                el.html(t.html());
                t.remove();
            }
        });
    };
    $(".ellipsis").ellipsis();
    function toTitleCase(str)
    {
        return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
    }

});