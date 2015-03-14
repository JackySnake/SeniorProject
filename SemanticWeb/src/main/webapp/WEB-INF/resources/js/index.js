$(function () {
    $('#search_tab a:first').tab('show');
    $('#advanceSearch').submit(function (event) {
        $.ajax({
            url: $("#advanceSearch").attr("action"),
            data: $("#queryString"),
            type: "GET",
            success: function (response) {
                createTable(response);
            }
        });
        event.preventDefault();
    });

    $('#facetedSearch').submit(function (event) {

        $.ajax({
            url: $("#facetedSearch").attr("action"),
            data: 'searchString=' + $("#keyword").val() + "&type=" + $("#type").val(),
            type: "GET",
            success: function (response) {
                createTable(response);
            }
        });
        event.preventDefault();
    });


    $(".dropdown-menu li a").click(function () {
        $(".selection").text($(this).text());
        $(".selection").val($(this).text());
        $("#type").val($(this).text());
    });


});
function createTable(result) {
    var json = JSON.parse(result);

    if (document.getElementById("result_table") != null) {
        document.getElementById("result_table").remove();
    }

    var body = document.getElementById('search_result');
    var tbl = document.createElement('table');
    tbl.setAttribute("class", "table table-hover");
    tbl.setAttribute("id", "result_table");
    var tbdy = document.createElement('tbody');
    var tr = document.createElement('tr');
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
//        tbdy.appendChild(tr);
//    }

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
}