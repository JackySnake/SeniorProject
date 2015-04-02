var globalproperty=[];
$(function () {
    $('#search_tab a:first').tab('show');
    $('#advanceSearch').submit(function (event) {
        $.ajax({
            url: $("#advanceSearch").attr("action"),
            data: 'searchString=' + $("#queryString").val(),
            type: "GET",
            success: function (response) {
                createTable(response);
            }
        });
        event.preventDefault();
    });

    $("#category + .dropdown-menu li a").click(function () {
        console.log("select category");
        $("#category .selection").text($(this).text());
        $.ajax({
            url: $("#category").attr("action"),
            data: 'category=' + $(this).text(),
            type: "GET",
            datatype: "json",
            success: function (response) {
                if ($("#dropdownAddProperty *") !== null) {
                    $("#dropdownAddProperty *").remove();
                }
                if ($("#property *") !== null) {
                    $("#property *").remove();
                }
                if ($("#search_result *") !== null) {
                    $("#search_result *").remove();
                }
                $("#dropdownAddProperty").append("" +
                        "<label for='addProperty' class='col-sm-4 control-label'>Add property</label>" +
                        "<div class='dropup'>" +
                        "<button class='btn btn-default dropdown-toggle' type='button' id='addProperty' action='" + ctx + "/addProperty' data-toggle='dropdown' aria-expanded='true'>" +
                        "<span class='selection'></span>" +
                        "<span class='caret'></span>" +
                        "</button>" +
                        "<ul class='dropdown-menu scrollable-menu' role='menu' aria-labelledby='addProperty'>" +
                        "</ul>" +
                        "</div>");


                for (property in response) {
                    var li = document.createElement('li');
                    li.setAttribute("role", "presentation");
                    var a = document.createElement('a');
                    a.setAttribute("role", "menuitem");
                    a.setAttribute("tabindex", "-1");
                    a.setAttribute("href", "#!");
                    a.setAttribute("onclick", "addProperty(this)");
                    a.setAttribute("data-property",response[property] );
                    a.appendChild(document.createTextNode(response[property]));
                    li.appendChild(a);
                    $("#dropdownAddProperty > div > ul").append(li);
                }
            }
        });
    });
});
function addProperty(elem) {
    $("#addProperty .selection").text($(elem).text());
    var res = $(elem).text().split(":");
    if($(elem).text().split(" ")[0]=="is"){
        console.log("isValueOf");
        res[1]="is "+res[1];
    }
    var selectedValues = {};
    var elems = $(".panel-body .list-group .list-group-item.active");
//        var elems = document.getElementsByClassName("select");
     for (var i = 0; i < elems.length; i++) {
        var text;
        $(elems[i]).contents().each(function(){
            if(this.nodeType===3){
                text= this.wholeText;
                if(text.charAt(0)!="\"") text=text.split(" ")[0];
            }
        });
        selectedValues[$(elems[i]).parent().parent().parent().attr("data-property")] = text;
    }
    $.ajax({
        url: $("#addProperty").attr("action"),
        data: {
            loadProds: 3,
            category: $("#category .selection").text(),
            property: $(elem).text(),
            selectedValues: JSON.stringify(selectedValues)
        },
        type: "GET",
        datatype: "json",
        success: function (response) {
            var pages = Math.ceil(JSON.parse(response).length/5);
            globalproperty["collapse" + res[1]]=response;
            if ($("#accordion" + res[1]).length <= 0) {
                var html = "" +
                        "<div class='panel-group property' id='accordion" + res[1] + "' role='tablist' aria-multiselectable='true'>" +
                            "<div class='panel panel-default'>" +
                                "<div class='panel-heading' role='tab' id='heading" + res[1] + "'>" +
                                    "<h4 class='panel-title'>" +
                                        "<a data-toggle='collapse' data-parent='#accordion" + res[1] + "' href='#collapse" + res[1] + "' aria-expanded='true' aria-controls='collapse" + res[1] + "'>" +
                                            res[1] +
                                        "</a>" +
                                        "<button type='button' class='close' onclick='removeProperty(this)' data-target='#accordion" + res[1] + "' aria-label='Close'><span aria-hidden='true'>&times;</span></button>" +
                                        "<small class='sort_by'>sort by <a href='#!' class='active' onclick='jsonSortByName(this,collapse"+res[1]+","+response+")'>name</a> <a href='#!' onclick='jsonSortByCount(this,collapse"+res[1]+","+response+")'>count</a></small>"+
                                    "</h4>" +
                                "</div>" +
                            "<div data-property=" + $(elem).text() + " id='collapse" + res[1] + "' class='panel-collapse collapse in' role='tabpanel' aria-labelledby='heading" + res[1] + "'>" +
                                "<div class='panel-body'>" +
                                    "<div class='list-group'>"+
                                        propertyHTMLFromJson(JSON.parse(response),0)+ 
                                    "</div>" +
                                    "<ul class='pager' data-curpage='0'>"+
                                        "<li><a href='#!' onclick='previousPage(this,"+pages+")'>Previous</a></li>"+
                                        "<li><a href='#!' onclick='nextPage(this,"+pages+")'>Next</a></li>"+
                                    "</ul>"+
                                "</div>" +
                            "</div>" +
                        "</div>"+
                    "</div>";
                $("#property").append(html);
            }
        }
    });
}
function removeProperty(elem) {
    console.log(globalproperty.length);
    var target = $($(elem).attr("data-target"));
    delete globalproperty[$(elem).parents(".panel-group.property").find(".panel-collapse").attr("id")];
    console.log(globalproperty.length);
    target.remove();
}
function previousPage(elem,pages){
    //console.log(globalproperty.length);
    var currentPage = $(elem).parents("ul").attr("data-curpage");
    //console.log("pre "+currentPage);
    if(currentPage>0){
        var json = $(elem).parents(".panel-group.property").attr("data-json");
        $(elem).parents("ul").attr("data-curpage",parseInt(currentPage)-1);
        $(elem).parents(".panel-collapse").children().children(".list-group").html(propertyHTMLFromJson(JSON.parse(globalproperty[$(elem).parents(".panel-collapse").attr("id")]),parseInt(currentPage)-1));
    }
}

function nextPage(elem,pages){
   // console.log(globalproperty[$(elem).parents(".panel-collapse").attr("id")]);
    var currentPage = $(elem).parents("ul").attr("data-curpage");
   // console.log("next "+currentPage);
    if(currentPage<pages){
        var json = $(elem).parents(".panel-group.property").attr("data-json");
        $(elem).parents("ul").attr("data-curpage",parseInt(currentPage)+1);
        $(elem).parents(".panel-collapse").children().children(".list-group").html(propertyHTMLFromJson(JSON.parse(globalproperty[$(elem).parents(".panel-collapse").attr("id")]),parseInt(currentPage)+1));
    }
}

function selectValue(elem) {
    var old = $(elem).parent().find(".active");
    old.removeClass("active");
    $(elem).addClass("active");
    $(elem).parents(".panel-group.property").nextAll(".panel-group.property").remove();
    var values = {};
    var elems = $(".panel-body .list-group .list-group-item.active");
//        var elems = document.getElementsByClassName("select");
    for (var i = 0; i < elems.length; i++) {
        var text;
        $(elems[i]).contents().each(function(){
            if(this.nodeType===3){
                text= this.wholeText;
                if(text.charAt(0)!="\"") text=text.split(" ")[0];
                console.log(text);
            }
        });
        values[$(elems[i]).parent().parent().parent().attr("data-property")] = text;
    }
    $.ajax({
        url: ctx + "/selectValue",
        data: {
            loadProds: 2,
            category: $("#category .selection").text(),
            values: JSON.stringify(values)
        },
        type: "GET",
        datatype: "json",
        success: function (response) {
            var json = JSON.parse(response);
            var html = "<div class='list-group'>";
            for (var i = 0; i < json.length; i++) {
                html += "<a href='#collapse" + i + "' onclick='selectResult(this)' class='list-group-item' data-toggle='collapse' aria-expanded='false' aria-controls='collapse" + i + "'>" +
                        "<h4 class='list-group-item-heading'>" + json[i].value + "</h4>" +
                        "<p class='list-group-item-text'>" + json[i].name + "</p>" +
                        "</a>" +
                        "<div class='collapse' id='collapse" + i + "'>" +
                        "<div class='well'>" +
                        "</div>" +
                        "</div>";

            }
            html += "</div>";
            $("#search_result").html(html);
        }
    });
}

function selectResult(elem) {
    console.log("selectResult");
    var old = $("#search_result").find(".active");
    if (!old.is($(elem))) {

        old.removeClass("active");
        old.next().removeClass("collapse in");
        old.next().addClass("collapse");
        $(elem).addClass("active");
        $.ajax({
            url: ctx + "/selectResult",
            data: {
                loadProds: 2,
                category: $("#category .selection").text(),
                result: $(elem).children("p").text()
            },
            type: "GET",
            datatype: "json",
            success: function (response) {
                var html = "<dl class='dl-horizontal'>";

                var json = JSON.parse(response);

                for (var i = 0; i < json.length; i++) {
                    html += "<dt>"+json[i].name+"</dt>" +
                            "<dd>";
                    console.log(json[i].value.substr(0,4));
                            if(json[i].value.substr(0,4)=="http"){
                    html+="<a href="+json[i].value+">";        
                    }
                    html+=json[i].value+"</a></dd>";
                    
                }
                html += "</dl>";
                $(elem).next().children().html(html);
            }
        });
    }
}
function searchFor(elem){
     $("#category + .dropdown-menu li a[data-category='"+$(elem).parent().siblings("dt").text()+"']").click();
//     console.log($("#dropdownAddProperty div ul li a").first().attr("data-property"));
//     //$("#dropdownAddProperty ul li a[data-property='"+$(elem).text()+"']").click();
//     addProperty($("#dropdownAddProperty ul li a[data-property='"+$(elem).text()+"']"));
     
}

function jsonSortByName(elem,collapseID,json){
    console.log("sortName");
    $(elem).siblings().removeClass("active");
    $(elem).addClass("active");
    json.sort(function(a, b){
        return a.elem.localeCompare(b.elem);
    });
    $(collapseID).children().children().html(propertyHTMLFromJson(json));
}
function jsonSortByCount(elem,collapseID,json){
    console.log("sortCount");
    $(elem).siblings().removeClass("active");
    $(elem).addClass("active");
    json.sort(function(a, b){
        return b.count-a.count;
    });
    $(collapseID).children().children().html(propertyHTMLFromJson(json));
}

function propertyHTMLFromJson(json,page){
   // console.log("pphtmlfjson "+json.length);
   // var j = JSON.parse("\""+json+"\"");
   // console.log(j);
    var htmlBuffer = [];
    for (var i=page;i<json.length;i++) {
     //   console.log(json[i]);
        if(i>=page+5) break;
        //console.log("i "+i+" elem "+json[i].elem);
         htmlBuffer.push("<a href='#!' class='list-group-item' data-property="+json[i].elem+" onclick='selectValue(this)'>" +
                 "<span class='badge'>"+json[i].count+"</span>"+
                  json[i].elem + "</a>");
    }
    return htmlBuffer.join("\n");
}

function createTable(result) {
    var json = JSON.parse(result);

    if (document.getElementById("result_table") !== null) {
        document.getElementById("result_table").remove();
    }

    var body = document.getElementById('search_result');
    var tbl = document.createElement('table');
    tbl.setAttribute("class", "table table-hover");
    tbl.setAttribute("id", "result_table");
    var tbdy = document.createElement('tbody');
    var tr = document.createElement('tr');

    for (var i = 0; i < json[0].length; i++) {
        var th = document.createElement('th');
        th.appendChild(document.createTextNode(json[0][i]));
        tr.appendChild(th);
    }
    tbdy.appendChild(tr);
    for (var i = 1; i < json.length; i++) {
        var tr = document.createElement('tr');
        for (var j = 0; j < json[i].length; j++) {
            var td = document.createElement('td');
            td.appendChild(document.createTextNode(json[i][j]));
            tr.appendChild(td);
        }
        tbdy.appendChild(tr);
    }
    tbl.appendChild(tbdy);
    body.appendChild(tbl);
    //    For Jena
//    for (var i = 0; i < json.head.vars.length; i++) {
//        var th = document.createElement('th');
//        th.appendChild(document.createTextNode(json.head.vars[i]));
//        tr.appendChild(th);
//    }
//    tbdy.appendChild(tr);
//    for (var i = 0; i < json.results.bindings.length; i++) {
//        var tr = document.createElement('tr');
//        for (var b in json.results.bindings[i]) {
//            var td = document.createElement('td');
//            if(json.results.bindings[i][b].type=="uri"){
//                var link = document.createElement('a');
//                link.setAttribute("href", json.results.bindings[i][b].value);
//                link.appendChild(document.createTextNode(json.results.bindings[i][b].value));
//                td.appendChild(link)
//            }else {
//                td.appendChild(document.createTextNode(json.results.bindings[i][b].value)); 
//            }
//            tr.appendChild(td);
    //        }
    //        tbdy.appendChild(tr); //    }

}
