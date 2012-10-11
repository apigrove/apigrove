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

    var types = new Array("property", "tdrRule", "headerTrans", "targethost", "parameter");
    for(var i = 0; i<types.length; i++){
        var items = document.getElementsByClassName(types[i]);
        var itemCount = items.length;
        if(types[i] == "property") var propertyCount = itemCount;
        else if(types[i] == "tdrRule") var tdrCount = itemCount;
        else if(types[i] == "headerTrans") var headerTransCount = itemCount;
        else if(types[i] == "targethost") var targetHostCount = itemCount;
        else  if(types[i] == "parameter") var parameterCount = itemCount;
    }

    $('.newItem').click(function(event){
        var type = $(this).attr('type');
        newItem(type);
        return false;
    });

    $('button.btn').click(function(event){
        event.preventDefault();
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

    $('.tdrType').live("change", function(){
        var id = $(this).attr('item');
        var val = $(this).val();
        if(val == 'Dynamic'){
            $("#tdrRuleValue"+id).attr('placeholder','Http Header Name');
            $("#tdrRuleExtractFrom"+id).show('fast');
        }
        else if(val == 'Static'){
            $("#tdrRuleValue"+id).attr('placeholder','Value');
            $("#tdrRuleExtractFrom"+id).hide('fast');
        }
        else if(val == 'Property'){
            $("#tdrRuleValue"+id).attr('placeholder','Property Name');
            $("#tdrRuleExtractFrom"+id).hide('fast');
        }
    });

    $('.enabled').click(function(event){
        var me = $(this).attr('name');
//        var enabledStatus = $("button.auth.active:contains('Enabled')").length;
//        var disabledStatus = $("button.auth.active:contains('Disabled')").length;
        switch(me){
            case 'enabled-true':
//                if(enabledStatus == 0){
                    $('input.'+me).val(1);
                    $('input.enabled-false').val(0);
//                }else{
//                    $('input.'+me).val(0);
//                    $('input.enabled-false').val(1);
//                }
                break;
            case 'enabled-false':
//                if(disabledStatus == 0){
                    $('input.'+me).val(1);
                    $('input.enabled-true').val(0);
//                }else{
//                    $('input.'+me).val(0);
//                    $('input.enabled-true').val(1);
//                }
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

    function handleAuthTypeClick(ele, speed){
        var type = ele.attr('value');

        switch(type){
            case 'basic':
                $('#IPs').hide(speed);
                $('#authKey').hide(speed);
                $('#un-pw').show(speed);
                break;
            case "authKey":
                $('#IPs').hide(speed);
                $('#un-pw').hide(speed);
                $('#authKey').show(speed);
                break;
            case 'wsse':
                $('#IPs').hide(speed);
                $('#authKey').hide(speed);
                $('#un-pw').show(speed);
                break;
            case 'ipWhiteList':
                $('#authKey').hide(speed);
                $('#un-pw').hide(speed);
                $('#IPs').show(speed);
                break;
        }
        //ele.addClass("active");
    }


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
        //ele.addClass("active");
    }



    var type = $('#type').attr('value');
    var button = $('[name="'+type+'"]');
    handleAuthTypeClick(button, null);


    function newItem(type){
        var itemCount = Math.floor((Math.random()*100000)+1);
        if(type == 'targethost'){
            var itemBody = targetBody(itemCount);
        }else if(type == 'property'){
            var itemBody = propertyBody(itemCount);
        }else if(type == 'parameter'){
            var itemBody = parameterBody(itemCount);
        }else if(type == 'tdrRule'){
            var itemBody = tdrBody(itemCount);
        }else if(type == 'headerTrans'){
            var itemBody = headerTransBody(itemCount);
        }
        var parentID = "#" + type + "Group";
        $(parentID).append(itemBody);
    }

    function targetBody(count){
        var body = "<div class=\"control-group targethost\" count=\""+targetHostCount+"\" id=\"targethostGroup"+count+"\">"
            +"<label class=\"control-label\" for=\"targethost"+count+"\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-xlarge\" name=\"targethost"+count+"\" id=\"targethost"+count+"\"> "
            +"<a class=\"btn removeItem\" type=\"targethost\" number=\""+count+"\" title=\"Remove target host\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div>";
        targetHostCount++;
        return body;
    }

    function propertyBody(count){
        var body = "<div class=\"control-group property\" count=\""+propertyCount+"\" id=\"propertyGroup"+count+"\">"
            +"<label class=\"control-label\" for=\"properties"+count+"\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small\" name=\"property["+propertyCount+"][name]\" placeholder=\"Key\" id=\"properties"+count+"\"> "
            +"<input type=\"text\" class=\"input-small\" name=\"property["+propertyCount+"][value]\" placeholder=\"Value\"> "
            +"<a class=\"btn removeItem\" type=\"property\" number=\""+count+"\" title=\"Remove property\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div";
        propertyCount++;
        return body;
    }

    function parameterBody(count){
        var key = $('#parameterKey0').val();
        var value = $('#parameterValue0').val();
        $('#parameterKey0').val("");
        $('#parameterValue0').val("");
        var body = "<div class=\"control-group parameter\" count=\""+parameterCount+"\" id=\"parameterGroup"+count+"\">"
            +"<label class=\"control-label\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small parameter-key\" name=\"parameterKey"+parameterCount+"\" value=\""+key+"\" placeholder=\"Key\" id=\"parameterKey"+count+"\"> "
            +"<input type=\"text\" class=\"input-small parameter-value\" name=\"propertyValue"+parameterCount+"\" value=\""+value+"\" placeholder=\"Value\" id=\"parameterValue"+count+"\"> "
            +"<a class=\"btn updateParams\" type=\"parameter\" number=\""+count+"\" title=\"Update Parameter\">Update</a> "
            +"<a class=\"btn removeItem updateParams\" type=\"parameter\" number=\""+count+"\" title=\"Remove parameter\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div";
        parameterCount++;
        return body;
    }

    function tdrBody(count){
        var body = "<div class=\"control-group tdrRule\" count=\""+tdrCount+"\" id=\"tdrRuleGroup"+count+"\">"
            +"<label class=\"control-label\" for=\"tdrRules"+count+"\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small\" name=\"tdr["+tdrCount+"][name]\" placeholder=\"Name\" id=\"tdrRules"+count+"\"> "
            +"<select name=\"tdr["+tdrCount+"][type]\" class=\"input-small tdrType\" item=\""+count+"\">"
                +"<option>Type</option>"
                +"<option>Static</option>"
                +"<option>Dynamic</option>"
                +"<option>Property</option>"
            +"</select> "
            +"<select class=\"input-small\" name=\"tdr["+tdrCount+"][extractFrom]\" style=\"display: none; width: 120px;\" id=\"tdrRuleExtractFrom"+count+"\">"
                 +"<option>Extract From</option>"
                 +"<option>Request</option>"
                 +"<option>Response</option>"
            +"</select> "
            +"<input type=\"text\" class=\"input-small\" name=\"tdr["+tdrCount+"][value]\" placeholder=\"Value\"  id=\"tdrRuleValue"+count+"\" style=\"width: 120px;\"/> "
            +"<a class=\"btn removeItem\" type=\"tdrRule\" number=\""+count+"\" title=\"Remove TDR rule\"><i class=\"icon-minus\"></i></a>"
            +"</div>"
            +"</div>";
        tdrCount++;
        return body;
    }

    function headerTransBody(count){
        var body = "<div class=\"control-group headerTrans\" count=\""+headerTransCount+"\" id=\"headerTransGroup"+count+"\">"
            +"<label class=\"control-label\" for=\"headerTrans\">&nbsp;</label>"
            +"<div class=\"controls\">"
            +"<input type=\"text\" class=\"input-small\" name=\"header["+headerTransCount+"][name]\" placeholder=\"Name\" id=\"headerTrans\"> "
            +"<select name=\"header["+headerTransCount+"][timing]\" class=\"input-small\"> "
            +"<option>Timing</option>"
            +"<option>Request</option>"
            +"<option>Response</option>"
            +"</select> "
            +"<select name=\"header["+headerTransCount+"][action]\" item=\"#headerAdv"+count+"\" class=\"input-small headerAction\">"
            +"<option>Action</option>"
            +"<option>Add</option>"
            +"<option>Remove</option>"
            +"</select> "
            +"<span id=\"headerAdv"+count+"\" style=\"display:none\">"
            +"<select name=\"header["+headerTransCount+"][type]\" class=\"input-small\">"
            +"<option>Type</option>"
            +"<option>Property</option>"
            +"<option>Static</option>"
            +"</select> "
            +"<input type=\"text\" class=\"input-small\" name=\"header["+headerTransCount+"][value]\" placeholder=\"Value\"></span> "
            +"<a class=\"btn removeItem\" type=\"headerTrans\" number=\""+count+"\" title=\"Remove header transformation\"><i class=\"icon-minus\"></i></a>"
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

    $('.updateParams').click( function(){
        updateParams();
    });

    $('.updateParams').live("click", function(){
        updateParams();
    });

    $('.makeCall').click(function(){
        var url = document.getElementById('showUrl').innerHTML;
//        document.getElementById('callWindow').innerHTML = "<iframe src=\""+url+"\" height=\"200px\" width=\"100%\"></iframe>";
        makeRequest(url);
    });

    function makeRequest(url) {
        var xmlHttp = getXMLHttp();
        xmlHttp.onreadystatechange = function() {
            if(xmlHttp.readyState == 4) {
                HandleResponse(xmlHttp.responseText);
            }
        }
        xmlHttp.open("GET", "/makeCall?url="+url, true);
        xmlHttp.send(null);
    }

    function getXMLHttp() {
        var xmlHttp
        try {
            xmlHttp = new XMLHttpRequest();
        }
        catch(e) {
            try {
                xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
            }
            catch(e) {
                try {
                    xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
                }
                catch(e) {
                    alert("Your browser does not support AJAX!");
                    return false;
                }
            }
        }
        return xmlHttp;
    }

    function HandleResponse(response) {
        document.getElementById('callWindow').innerHTML = response;
        $('#callWindow').show('fast');
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
        $('#hiddenField').val(url);
    }


    function setupDeleteButtons(){

        $("a.delete").each(function(index, element){
            var me = $(element);
            var confirm = me.clone();
            me.parent().append(confirm);
            confirm.attr("id", me.attr("id")+"-confirm");
            confirm.text("Confirm " + me.text());

            confirm.hide();
            me.click(function(event){
                event.preventDefault();
                me.hide();
                confirm.show();
                confirm.popover({trigger:"manual", title:"Are you sure?", content:"Click again to confirm"});
                confirm.popover("show");
            });
            confirm.mouseout(function(event){
                me.show();
                confirm.hide();
                confirm.popover("hide");
            });
        });
    }
    function setupTooltipsAndPopovers(){

        // If this constant is true, text input fields show tips on focus
        // If false, tips are shown on hover (default behavior)
        var TEXT_INPUT_TIP_ON_FOCUS = false;
        var TOOLTIP_DELAY_SHOW = 1000;
        var TOOLTIP_DELAY_HIDE = 0;

        $('[rel=tooltip]').each(function(index, element) {
            var me = $(element);
            if (TEXT_INPUT_TIP_ON_FOCUS && (me.prop('tagName') === "INPUT") && (me.prop('type') === 'text')) {
                me.tooltip({trigger:"focus", delay:{show: TOOLTIP_DELAY_SHOW, hide: TOOLTIP_DELAY_HIDE}});
            } else {
                me.tooltip({trigger:"hover", delay:{show: TOOLTIP_DELAY_SHOW, hide: TOOLTIP_DELAY_HIDE}});
            }
        });

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

});


