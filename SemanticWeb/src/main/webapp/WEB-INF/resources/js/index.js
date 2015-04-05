var globalproperty = new Array();
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
                    a.setAttribute("data-property", response[property]);
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
    if ($(elem).text().split(" ")[0] == "is") {
        res[1] = "is " + res[1];
    }
    var selectedValues = {};
    var elems = $(".panel-body .list-group .list-group-item.active");
//        var elems = document.getElementsByClassName("select");
    for (var i = 0; i < elems.length; i++) {
        var text;
        $(elems[i]).contents().each(function () {
            if (this.nodeType === 3) {
                text = this.wholeText;
                if (text.charAt(0) != "\"")
                    text = text.split(" ")[0];
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
            var pages = Math.ceil(JSON.parse(response).length / 5);
            globalproperty["collapse" + res[1]] = response;
            console.log(globalproperty.length);
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
                        "<small class='sort_by'>sort by <a href='#!' onclick='jsonSortByName(this,collapse" + res[1] + ")'>name</a> <a href='#!' onclick='jsonSortByCount(this,collapse" + res[1] + ")'>count</a></small>" +
                        "</h4>" +
                        "</div>" +
                        "<div data-property=" + $(elem).text() + " id='collapse" + res[1] + "' class='panel-collapse collapse in' role='tabpanel' aria-labelledby='heading" + res[1] + "'>" +
                        "<div class='panel-body'>" +
                        "<div class='list-group'>" +
                        propertyHTMLFromJson(JSON.parse(response), 0) +
                        "</div>" +
                        "<nav class='nav_pager'>" +
                        "<ul class='pager' data-curpage='0'>" +
                        "<li>" +
                        "<input type='number' class='form-control input-sm' id='pagebox' value='1' min='1' max='" + pages + "'>/" + pages +
                        "<button class='btn btn-primary btn-sm' onclick='gotopage(this," + pages + ")'>Go</button>" +
                        "</li>" +
                        "<li class='previous disabled'><a href='#!' onclick='previousPage(this," + pages + ")'><span aria-hidden='true'>&larr;</span> Previous</a></li>";

                if (pages <= 1) {
                    html += "<li class='next disabled'>";
                } else {
                    html += "<li class='next' >";
                }
                html += "<a href='#!' onclick='nextPage(this," + pages + ")'>Next <span aria-hidden='true'>&rarr;</span></a></li>" +
                        "</ul>" +
                        "</nav>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>";
                $("#property").append(html);
            }
        }
    });
}
function gotopage(elem, pagesize) {
    var page = $("#pagebox").val() - 1;
    if (page >= 0 && page < pagesize) {

        $(elem).parents("ul").attr("data-curpage", page);
        $(elem).parent("li").siblings(".next").removeClass("disabled");
        $(elem).parent("li").siblings(".previous").removeClass("disabled");
        if (page == 0) {
            $(elem).parent("li").siblings(".previous").addClass("disabled");
        }
        if (page == pagesize - 1) {
            $(elem).parent("li").siblings(".next").addClass("disabled");
        }
        // console.log(globalproperty[$(elem).parents(".panel-collapse").attr("id")]);
        $(elem).parents(".panel-collapse").children().children(".list-group").html(propertyHTMLFromJson(JSON.parse(globalproperty[$(elem).parents(".panel-collapse").attr("id")]), page));
    }
}
function jsonSortByName(elem, collapseID) {

    console.log("sortName");
    $(elem).siblings().removeClass("active");
    $(elem).addClass("active");
    var currentPage = $(elem).parents(".panel-group.property").find(".pager").attr("data-curpage");
    var property = JSON.parse(globalproperty[$(collapseID).attr("id")]);
    if (property[0].label != "undefined")
{property.sort(function (a, b) {
        return a.label.localeCompare(b.label);
    });}else {
    property.sort(function (a, b) {
        return a.elem.localeCompare(b.elem);
    });
    }
    
    globalproperty[$(collapseID).attr("id")] = JSON.stringify(property);
    $(collapseID).children(".panel-body").children(".list-group").html(propertyHTMLFromJson(property, currentPage));
}
function jsonSortByCount(elem, collapseID) {
    console.log("sortCount");
    $(elem).siblings().removeClass("active");
    $(elem).addClass("active");
    var currentPage = $(elem).parents(".panel-group.property").find(".pager").attr("data-curpage");
    var property = JSON.parse(globalproperty[$(collapseID).attr("id")]);
    property.sort(function (a, b) {
        return b.count - a.count;
    });
    globalproperty[$(collapseID).attr("id")] = JSON.stringify(property);
    $(collapseID).children(".panel-body").children(".list-group").html(propertyHTMLFromJson(property, currentPage));
}
function removeProperty(elem) {
    console.log(globalproperty.length);
    var target = $($(elem).attr("data-target"));
    delete globalproperty[$(elem).parents(".panel-group.property").find(".panel-collapse").attr("id")];
    target.remove();
}
function previousPage(elem, pages) {
    //if($(elem).attr("class"))
    //console.log(globalproperty.length);
    var currentPage = $(elem).parents("ul").attr("data-curpage");
    //console.log("pre "+currentPage);

    if (currentPage > 0) {
        $(elem).parent("li").siblings().removeClass("disabled");
        var json = $(elem).parents(".panel-group.property").attr("data-json");
        $(elem).parents("ul").attr("data-curpage", parseInt(currentPage) - 1);
        $("#pagebox").val(parseInt(currentPage));
        $(elem).parents(".panel-collapse").children().children(".list-group").html(propertyHTMLFromJson(JSON.parse(globalproperty[$(elem).parents(".panel-collapse").attr("id")]), parseInt(currentPage) - 1));
        if (parseInt(currentPage) == 1) {
            $(elem).parent("li").addClass("disabled");
        }
    }
}

function nextPage(elem, pages) {
    // console.log(globalproperty[$(elem).parents(".panel-collapse").attr("id")]);
    var currentPage = $(elem).parents("ul").attr("data-curpage");
    // console.log("next "+currentPage);
    // console.log("cur "+currentPage+" page "+pages);
    if (currentPage < pages - 1) {
        $(elem).parent("li").siblings().removeClass("disabled");
        var json = $(elem).parents(".panel-group.property").attr("data-json");

        $(elem).parents("ul").attr("data-curpage", parseInt(currentPage) + 1);
        $("#pagebox").val(parseInt(currentPage) + 2);
        $(elem).parents(".panel-collapse").children().children(".list-group").html(propertyHTMLFromJson(JSON.parse(globalproperty[$(elem).parents(".panel-collapse").attr("id")]), parseInt(currentPage) + 1));
        if (parseInt(currentPage) + 1 == pages) {
            $(elem).parent("li").addClass("disabled");
        }
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
        $(elems[i]).contents().each(function () {
            if (this.nodeType === 3) {
                text = this.wholeText;
                if (text.charAt(0) != "\"")
                    text = text.split(" ")[0];
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
                        "<h4 class='list-group-item-heading'>" + json[i].head + "</h4>" +
                        "<p class='list-group-item-text'>" + json[i].value + "</p>" +
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
                json.sort(function (a, b) {
                    return a.name.localeCompare(b.name);
                });
                
        html+="<dt>image</dt>"+
                "<dd><img src='http://upload.wikimedia.org/wikipedia/en/thumb/5/5e/SavedbytheBelleTITLE.jpg/220px-SavedbytheBelleTITLE.jpg'></img></dd>";
                for (var i = 0; i < json.length; i++) {
                    html += "<dt>" + json[i].name + "</dt>" +
                            "<dd>";

                    if (json[i].url.substring(0,4) == "http") {
       
                        html += "<a href=" + json[i].url + ">";
                    }else if(json[i].label.substring(0,4)=="http"){
                        html += "<a href=" + json[i].label + " >";
                    }
                    html += json[i].label + "</a></dd>";

                }
                html += "</dl>";
                $(elem).next().children().html(html);
            }
        });
    }
}



function propertyHTMLFromJson(json, page) {
    var htmlBuffer = [];
    for (var i = page * 5; i < json.length; i++) {
        //   console.log(json[i]);
        if (i >= page * 5 + 5)
            break;
        console.log("i " + i + " elem " + json[i].elem);
        htmlBuffer.push("<a href='#!' class='list-group-item' data-property=" + json[i].elem + " onclick='selectValue(this)'>" +
                "<span class='badge'>" + json[i].count + "</span>" +
                json[i].elem + " <small>" + json[i].label + "</small></a>");
    }
    return htmlBuffer.join("\n");
}

function createTable(result) {
    var json = JSON.parse(result);

    var html = "<table class='table table-hover' id='result_table'>" +
            "<tbody>" +
            "<tr>";
    for (var i = 0; i < json[0].length; i++) {
        html += "<th>" + json[0][i] + "</th>";
    }

    html += "</tr>";
    for (var i = 1; i < json.length; i++) {
        html += "<tr>";
        for (var j = 0; j < json[i].length; j++) {
            console.log(json[i][j]);
            html += "<td>" + json[i][j] + "</td>";
        }
        html += "</tr>";

    }
    html += "</tbody>" +
            "</table>";
    $("#search_result").html(html);
//    var body = document.getElementById('search_result');
//    var tbl = document.createElement('table');
//    tbl.setAttribute("class", "table table-hover");
//    tbl.setAttribute("id", "result_table");
//    var tbdy = document.createElement('tbody');
//    var tr = document.createElement('tr');
//
//    for (var i = 0; i < json[0].length; i++) {
//        var th = document.createElement('th');
//        th.appendChild(document.createTextNode(json[0][i]));
//        tr.appendChild(th);
//    }
//    tbdy.appendChild(tr);
//    for (var i = 1; i < json.length; i++) {
//        var tr = document.createElement('tr');
//        for (var j = 0; j < json[i].length; j++) {
//            var td = document.createElement('td');
//            td.appendChild(document.createTextNode(json[i][j]));
//            tr.appendChild(td);
//        }
//        tbdy.appendChild(tr);
//    }
//    tbl.appendChild(tbdy);
//    body.appendChild(tbl);
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
