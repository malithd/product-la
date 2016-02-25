/**
 * Created by vithulan on 1/29/16.
 */
var serverUrl = window.location.origin;
var fields;
var tableResults;
var fieldName;
var trem;
var chartType;
window.onload = function () {
    checkApi();
    addLogstream();

    //console.log("Hello world");
}

function openDashboard() {
    //window.location= baseUrl +'/loganalyzer/site/visualize.jag';
    // retrieveColum();
    window.open(serverUrl + '/loganalyzer/site/dashboard/visualize.jag');
}

function retrieveColum() {
    jQuery.ajax({
        type: "GET",
        url: serverUrl + "/analytics/tables/loganalyzer/schema",
        //async: false,

        beforeSend: function (xhr) {
            xhr.setRequestHeader("Authorization", "Basic " + btoa("admin" + ":" + "admin"));
        },

        success: function (res) {
            // alert(res);
            // CreateHtmlTable(res);

            var fields = jsonQ(res);
            var colms = fields.find('columns');
            var reslt = colms.nthElm('0');
            //var fild = res.columns[0].type;
            var hell = JSON.parse(res)
            var Jasonstr = JSON.stringify(reslt);
            var bla = JSON.stringify(hell.length);
            // document.getElementById("json_string").innerHTML=res.columns[1];
            //  document.getElementById("json_string").innerHTML=Jasonstr;
        },
        error: function (res) {
            alert(res.responseText);
        }
    });

}

function checkApi() {
    jQuery.ajax({
        type: "GET",
        url: serverUrl + "/api/dashboard/getFields",
        success: function (res) {
            var Jasonstr = JSON.stringify(res);
            Jasonstr = Jasonstr.replace("[", "");
            Jasonstr = Jasonstr.replace("]", "");
            fields = Jasonstr.split(',');


            //  document.getElementById("json_string").innerHTML=fields[0];

            addFields();
        },
        error: function (res) {
            alert(res.responseText);
        }
    });


}

function addFields() {
    var select = document.getElementById("fieldsName");
    var i;
    var j = fields.length;

    for (i = 0; i < j; i++) {

        fields[i] = fields[i].replace("\"", "")
        fields[i] = fields[i].replace("\"", "")
        fields[i] = capitalizeFirstLetter(fields[i].replace("_", ""));
        if (fields[i] != "Message") {
            var option = document.createElement('option');
            option.text = option.value = fields[i];
            select.add(option, 0);
        }
    }

}
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function simpleFirstLetter(string) {
    return string.charAt(0).toLowerCase() + string.slice(1);
}

function selectField2() {
    if ($("#0").val() == "None") {
        facetPath = "None";
    }
    // document.getElementById("json_string2").innerHTML=facetPath;
    fieldName = arguments[0];
    var charttype = arguments[1];
    trem = fieldName;
    fieldName = simpleFirstLetter(fieldName);
    if (fieldName != "logstream") {
        fieldName = "_" + fieldName;
    }

    var payload = {};
    payload.query = fieldName;
    payload.start = 0;
    payload.length = 100000;
    payload.timeFrom = 0;
    payload.tableName = "LOGANALYZER";
    payload.timeTo = 8640000000000000;
    var jsonnn = JSON.stringify(payload);


    jQuery.ajax({
        url: serverUrl + "/api/dashboard/fieldData",
        type: "POST",
        contentType: "application/json",
        dataType: "json",
        data: jsonnn,
        success: function (res) {
            var response = JSON.stringify(res);
            console.log(response);
            $("#data-table").empty();
            $("#data-table").append('<tr><th>' + trem + '</td><th>' + 'Hits' + '</th></tr>');
            addRow(res, fieldName);
        },
        error: function (res) {
            var response = JSON.stringify(res);
            alert(res.error);
        }
    });


}

function addRow() {
    var table = document.getElementById("data-table");
    var reslt = arguments[0];
    var i;
    var j = arguments[0].length;
    //var tester;
    //  document.getElementById("json_string").innerHTML = j;
    for (i = 0; i < j; i++) {
        $("#data-table").append('<tr><td>' + reslt[i][0] + '</td><td>' + reslt[i][1] + '</td></tr>');
    }

    draw(reslt, arguments[1]);
}
/*
 $(document).ready(function(){
 $("form").submit(function(){
 var query = $('#ftexte').val();
 document.getElementById("json_string").innerHTML=query;
 });
 });*/

function filter() {
    if ($("#0").val() == "None") {
        facetPath = "None";
    }
    $("#dsWelcome").hide();
    var query = $('#ftexte').val();
    var fieldN = $("#fieldsName").val();
    var filterSelect = $("#FilterType").val();
    fieldN = simpleFirstLetter(fieldN);
    if (fieldN != "logstream") {
        fieldN = "_" + fieldN;
    }

    if (query == "" && filterSelect == "None") {
        selectField2($("#fieldsName").val());
    }
    else {
        //document.getElementById("json_string2").innerHTML=fieldName;
        var quer;
        if (query == "") {
            quer = filterSelect;
        }
        else {
            quer = query;
        }
        query = quer + ",," + fieldN;

        //
        var payload = {};
        // document.getElementById("json_string2").innerHTML = facetPath.toString();
        payload.query = query;
        payload.start = 0;
        payload.length = 1000000;
        payload.timeFrom = 0;
        payload.tableName = "LOGANALYZER";
        payload.timeTo = 8640000000000000;
        payload.facetPath = facetPath.toString();
        var jsonnn = JSON.stringify(payload);
        //document.getElementById("json_string2").innerHTML = "You selected:!!" + query+"!!";

        jQuery.ajax({
            url: serverUrl + "/api/dashboard/filterData",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: jsonnn,
            success: function (res) {
                var response = JSON.stringify(res);
                console.log(response);
                //var datas = jsonQ(res);
                //  var rr = datas.find("values");
                //tableResults=response.split("||%\",\"");
                $("#data-table").empty();
                $("#data-table").append('<tr><th>' + trem + '</td><th>' + 'Hits' + '</th></tr>');
                addRow(res, fieldName);
                //document.getElementById("json_string").innerHTML=response;
            },
            error: function (res) {
                var response = JSON.stringify(res);
                // console.log(response);
                //document.getElementById("json_string").innerHTML=response;
                alert(res.error);
            }
        });
    }
}
function draw() {
    var chartType = $('#chartType').val();
    var dataValue = [];
    var temp = [];
    var json = arguments[0];
    //var charttype=arguments[2];
    for (var i = 0; i < json.length; i++) {
        var temp = [];
        var xVal = json[i][0];
        var xArr = [];
        xArr = json[i][0].toString().split(".");
        var vall = xArr[xArr.length - 1];
        if (vall.length > 11) {
            vall = vall.slice(0, 11);
        }
        // var xArr = json[i][0].split(".");
        //document.getElementById("json_string").innerHTML=xArr[xArr.length-1]+" ";
        temp.push(vall, parseInt(json[i][1]));
        dataValue.push(temp);
    }
    //document.getElementById("json_string").innerHTML=JSON.stringify(dataValue);
    var data = [
        {
            "metadata": {
                "names": [arguments[1], "count"],
                "types": ["ordinal", "linear"]
            },
            "data": dataValue
            //[["INFO", 30], ["WARN",40],["ERROR",50]]
        }
    ];
    //JSON.stringify(arguments[0]);
    var config = {
        x: arguments[1],
        charts: [
            {type: chartType, y: "count"}
        ],
        // maxLength: 10,
        width: 1000,
        height: 400
    }
    var lineChart = new vizg(data, config);
    lineChart.draw("#dChart");
}
function test(val) {
    // var res = JSON.parse(arguments);
    //var res = arguments[0];
    var id = val;
    // document.getElementById("json_string3").innerHTML = id;
}

function drawTime() {
    var json = arguments[0];
    var dataValue = [];
    var chartType = $("#chartType2").val();
    for (var i = 0; i < json.length; i++) {
        var temp = [];
        temp.push(json[i][0], json[i][1], parseInt(json[i][2]));
        dataValue.push(temp);
    }
    var data = [
        {
            "metadata": {
                "names": ["day", "type", "count"],
                "types": ["ordinal", "ordinal", "linear"]
            },
            "data": dataValue
            //[["INFO", 30], ["WARN",40],["ERROR",50]]
        }
    ];
    //JSON.stringify(arguments[0]);
    var config = {
        x: "day",
        charts: [
            {type: chartType, y: "count", color: "type"}
        ],
        // maxLength: 10,
        width: 1000,
        height: 400
    }
    var lineChart = new vizg(data, config);
    lineChart.draw("#timeChart");
}
function timeData() {
    if ($("#0").val() == "None") {
        facetPath = "None";
    }
    $("#dsWelcome").hide();
    $("#timeChart").show();
    var query = $('#ftexte').val();
    var fieldN = $("#fieldsName").val();
    var interval = $("#timeD").val();
    fieldN = simpleFirstLetter(fieldN);
    if (fieldN != "logstream") {
        fieldN = "_" + fieldN;
    }
    if (query == "") {
        alert("Please add a filter to generate timely chart");
    }
    else {
        //document.getElementById("json_string2").innerHTML=fieldName;
        query = query + ",," + fieldN + ",," + interval;

        //
        var payload = {};
        payload.query = query;
        payload.start = 0;
        payload.length = 100000;
        payload.timeFrom = 0;
        payload.tableName = "LOGANALYZER";
        payload.timeTo = 8640000000000000;
        payload.facetPath = facetPath.toString();
        var jsonnn = JSON.stringify(payload);
        //document.getElementById("json_string2").innerHTML = "You selected:!!" + query+"!!";

        jQuery.ajax({
            url: serverUrl + "/api/dashboard/epochTimeDataFinal",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: jsonnn,
            success: function (res) {
                var response = JSON.stringify(res);
                console.log(response);
                //var datas = jsonQ(res);
                //  var rr = datas.find("values");
                //tableResults=response.split("||%\",\"");
                // $("#data-table").empty();
                drawTime(res, fieldName);
                //addRow(res, fieldName);
                // document.getElementById("json_string3").innerHTML=res[0];
            },
            error: function (res) {
                var response = JSON.stringify(res);
                // console.log(response);
                //document.getElementById("json_string").innerHTML=response;
                alert(res.error);
            }
        });
    }


}
$(function () {
    $("#fromTime").datepicker();
    $("#toTime").datepicker();
    $(document).tooltip();

    // $("#dChart").draggable();
    //$("#timeChart").hide();
    // $("#timeChart").draggable();
    //$("#fieldsName").selectmenu();
    // $( "#dChart").resizable();

    //$("#timeChart").resizable();
});
function selectChart() {
    chartType = document.getElementById("chartType").value;

    //document.getElementById("json_string3").innerHTML=chartType;

}

function visualize() {
    $("#dsWelcome").hide();
    $("#dChart").show();
    fieldName = $("#fieldsName").val();
    selectField2(fieldName);
    //document.getElementById("json_string3").innerHTML=fieldN;
}

function addLogstream() {
    var payload = {};
    var logstream = "logstream";
    var seperator = ",,";
    payload.query = logstream + seperator + " ";
    payload.start = 0;
    payload.length = 100000;
    payload.timeFrom = 0;
    payload.tableName = "LOGANALYZER";
    payload.timeTo = 8640000000000000;
    var jsonnn = JSON.stringify(payload);


    jQuery.ajax({
        url: serverUrl + "/api/dashboard/logStreamData",
        type: "POST",
        contentType: "application/json",
        dataType: "json",
        data: jsonnn,
        success: function (res) {
            //console.log(response);
            //document.getElementById("json_string3").innerHTML=res.length;
            var select = document.getElementById("0");
            for (var i = 0; i < res.length; i++) {
                var option = document.createElement('option');
                option.text = option.value = res[i];
                option.id = -1;
                select.add(option, 0);
            }

        },
        error: function (res) {
            var response = JSON.stringify(res);
            alert(res.error);
        }
    });
}
var facetCount = 0;

var facetObj = {};
var testObj = {};

function addChildLogStream(val, idVal) {
    var idInt = parseInt(idVal);

    //if(testObj.hasOwnProperty(idVal)){
    for (var key in testObj) {
        if (key > idVal) {
            $("#" + key).remove();
            delete testObj[key];
        }
    }
    if (val != "None") {
        // }
        if (!facetObj.hasOwnProperty(idVal)) {
            facetObj[idVal] = val;


            //facetData.push(val);

        }
        else {
            for (var key in facetObj) {
                if (key >= idInt) {
                    delete facetObj[key];
                    //delete facetObj;
                    if (key != idInt) {

                        $("#" + key).remove();
                    }
                }
            }

            facetCount = idInt;
            facetObj[idInt] = val;
            //facetData.push(val);

        }
        //document.getElementById("logTest").innerHTML = "Val "+val+"  idVal"+idVal+"  facetObj "+JSON.stringify(facetObj)+"  testObj "+JSON.stringify(testObj);

        var facetData = [];
        for (var key in facetObj) {
            facetData.push(facetObj[key]);
        }
        facetPath = facetData;
        //document.getElementById("json_string3").innerHTML=facetpath +"  "+JSON.stringify(facetObj);
        // $("#facetPath").val(facetPath);
        var payload = {};
        var logstream = "logstream";
        var seperator = ",,";
        payload.query = logstream + seperator + facetPath;
        payload.start = 0;
        payload.length = 10000;
        payload.timeFrom = 0;
        payload.tableName = "LOGANALYZER";
        payload.timeTo = 8640000000000000;
        var jsonnn = JSON.stringify(payload);


        jQuery.ajax({
            url: serverUrl + "/api/dashboard/logStreamData",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: jsonnn,
            success: function (res) {
                //console.log(response);
                //document.getElementById("json_string3").innerHTML=res.length;
                //var select = document.getElementById("logstreamSelect");

                facetCount++;
                if (!testObj.hasOwnProperty(facetCount)) {
                    var LogstreamDiv = document.getElementById("logStreamDiv");
                    var selectList = document.createElement("select");
                    selectList.id = facetCount;
                    testObj[facetCount] = "test";
                    //document.getElementById("json_string3").innerHTML = JSON.stringify(facetObj);
                    //selectList.onchange = addChildLogStream(this.value,this.id);
                    selectList.setAttribute("onchange", "addChildLogStream(this.value,this.id)");
                    LogstreamDiv.appendChild(selectList);
                    var option1 = document.createElement('option');
                    option1.value = "None";
                    option1.text = "Select a category"
                    selectList.add(option1);
                    for (var i = 0; i < res.length; i++) {
                        var option = document.createElement('option');
                        option.text = option.value = res[i];
                        selectList.add(option, 0);
                    }
                }
            },
            error: function (res) {
                var response = JSON.stringify(res);
                alert(res.error);
            }
        });

        //document.getElementById("logTest").innerHTML = facetpath;
    }
}

