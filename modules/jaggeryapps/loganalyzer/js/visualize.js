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
        if(fields[i]!="Message") {
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
    payload.count = 100;
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
    document.getElementById("json_string").innerHTML = j;
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
    $("#dsWelcome").hide();
    var query = $('#ftexte').val();
    var fieldN = $("#fieldsName").val();
    fieldN = simpleFirstLetter(fieldN);
    if (fieldN != "logstream") {
        fieldN = "_" + fieldN;
    }

    if (query == "") {
        selectField2();
    }
    else {
        //document.getElementById("json_string2").innerHTML=fieldName;
        query = query + ",," + fieldN;

        //
        var payload = {};
        payload.query = query;
        payload.start = 0;
        payload.count = 1000000;
        payload.timeFrom = 0;
        payload.tableName = "LOGANALYZER";
        payload.timeTo = 8640000000000000;
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
        temp.push(json[i][0], parseInt(json[i][1]));
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
    document.getElementById("json_string3").innerHTML = id;
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
        query = query + ",," + fieldN+",,"+ interval;

        //
        var payload = {};
        payload.query = query;
        payload.start = 0;
        payload.count = 100;
        payload.timeFrom = 0;
        payload.tableName = "LOGANALYZER";
        payload.timeTo = 8640000000000000;
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
    payload.count = 100;
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
                option.id=-1;
                select.add(option, 0);
            }

        },
        error: function (res) {
            var response = JSON.stringify(res);
            alert(res.error);
        }
    });
}
var facetCount =0;

var facetObj = {};
var testObj ={};

function addChildLogStream(val,idVal){
    var idInt= parseInt(idVal);
    //document.getElementById("json_string3").innerHTML = val;

    if(testObj.hasOwnProperty(idVal)){
        for(var key in testObj){
            if(key>idVal){
               // $("#"+key).remove();
                delete testObj[key];
            }
        }
    }
    if(!facetObj.hasOwnProperty(idVal)){
        facetObj[idVal] = val ;



        //facetData.push(val);

    }
    else{
        for(var key in facetObj){
            if(key>=idInt) {
                delete facetObj[key];
                //delete facetObj;
                if(key!=idInt) {

                    $("#"+key).remove();
                }
            }
        }

        facetCount = idInt;
        facetObj[idInt] = val ;
        //facetData.push(val);

    }

    var facetData = [] ;
    for(var key in facetObj){
        facetData.push(facetObj[key]);
    }
    var facetpath = facetData;
    document.getElementById("json_string3").innerHTML = JSON.stringify(facetObj)+"  "+idVal+"  "+val+"  "+facetpath ;
    //document.getElementById("json_string3").innerHTML=facetpath +"  "+JSON.stringify(facetObj);

    var payload = {};
    var logstream = "logstream";
    var seperator =",,";
    payload.query = logstream+seperator+facetpath;
    payload.start = 0;
    payload.count = 100;
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
            var streamDiv = document.getElementById("logStreamDiv");
            var selectList = document.createElement("select");
            facetCount++;
            if (!testObj.hasOwnProperty(facetCount)){
                selectList.id = facetCount;
            testObj[facetCount] = "test";

            //selectList.onchange = addChildLogStream(this.value,this.id);
            selectList.setAttribute("onchange", "addChildLogStream(this.value,this.id)");
            streamDiv.appendChild(selectList);
            var option1 = document.createElement('option');
            option1.text = option1.value = "None";
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


}


