/**
 * Created by vithulan on 1/29/16.
 */
var serverUrl = window.location.origin;
var fields;
var tableResults;
var fieldName;
window.onload = function(){
    checkApi();
    //console.log("Hello world");
}

function selectField() {
    var x = document.getElementById("fieldsName").value;

    x=x.replace("\"","");
    x=x.replace("\"","");
    var payload ={};
    payload.query = x;
    payload.start = 0;
    payload.count =-1 ;
    payload.timeFrom = 0;
    payload.tableName="LOGANALYZER";
    payload.timeTo = 8640000000000000;
    var jsonnn=JSON.stringify(payload);
    document.getElementById("json_string2").innerHTML = "You selected: " + x;
    $('#data-table').DataTable({
        "ajax":{
        "url": serverUrl + "/api/dashboard/fieldData",
        "type" : "POST",
        "contentType": "application/json",
        "dataType": "json",
        "data" : jsonnn,
        "dataSrc" : function(d){
            return d;
        }
        },
        "columns":[
            {"mData":"values"}
        ],
            "paging": false,
            "searching": false


        }

    );
}

function openDashboard (){
    //window.location= baseUrl +'/loganalyzer/site/visualize.jag';
   // retrieveColum();
    window.open(serverUrl +'/loganalyzer/site/dashboard/visualize.jag');
}

function retrieveColum(){
    jQuery.ajax({
        type: "GET",
        url: serverUrl + "/analytics/tables/loganalyzer/schema",
       //async: false,

        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", "Basic " + btoa("admin" + ":" + "admin"));
        },

        success: function (res) {
           // alert(res);
           // CreateHtmlTable(res);

            var fields =jsonQ(res);
          var colms = fields.find('columns');
            var reslt=colms.nthElm('0');
            //var fild = res.columns[0].type;
            var hell =  JSON.parse(res)
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

function checkApi(){
    jQuery.ajax({
        type: "GET",
        url: serverUrl + "/api/dashboard/getFields",
        success: function (res) {
            var Jasonstr = JSON.stringify(res);
            Jasonstr=Jasonstr.replace("[","");
            Jasonstr=Jasonstr.replace("]","");
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
    var j=fields.length;

    for(i=0;i<j;i++){
        var option = document.createElement('option');
        option.text = option.value = fields[i];
        select.add(option, 0);
    }

}

function selectField2() {

    fieldName = document.getElementById("fieldsName").value;

    fieldName=fieldName.replace("\"","");
    fieldName=fieldName.replace("\"","");
    var payload ={};
    payload.query = fieldName;
    payload.start = 0;
    payload.count = 100;
    payload.timeFrom = 0;
    payload.tableName="LOGANALYZER";
    payload.timeTo = 8640000000000000;
    var jsonnn=JSON.stringify(payload);
    //document.getElementById("json_string2").innerHTML = "You selected: " + x;

    jQuery.ajax({
        url: serverUrl + "/api/dashboard/fieldData",
        type : "POST",
        contentType: "application/json",
        dataType: "json",
        data : jsonnn,
        success : function (res) {
            var response = JSON.stringify(res);
            console.log(response);
            //var datas = jsonQ(res);
            //  var rr = datas.find("values");
            tableResults=response.split("||%\",\"");
            $("#data-table").empty();
            //test(res);
            //document.getElementById("json_string").innerHTML=res[0][0];
           addRow(res,fieldName);
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

function addRow(){
    var table = document.getElementById("data-table");
    var reslt= arguments[0];
    var i;
    var j = arguments[0].length;
    //var tester;
    document.getElementById("json_string").innerHTML=j;
    for(i=0;i<j;i++){
        $("#data-table").append('<tr><td>'+reslt[i][0]+'</td><td>'+reslt[i][1]+'</td></tr>');
    }

    draw(reslt,arguments[1]);
}
/*
$(document).ready(function(){
    $("form").submit(function(){
        var query = $('#ftexte').val();
        document.getElementById("json_string").innerHTML=query;
    });
});*/

function filter(){
    var query = $('#ftexte').val();
    if(query==""){
        selectField2();
    }
    else {
    //document.getElementById("json_string2").innerHTML=fieldName;
        query = query + ",," + fieldName;

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
function draw(){
    var dataValue=[];
    var temp= [];
    var json=arguments[0];
    for(var i=0;i<json.length;i++){
        var temp= [];
        temp.push(json[i][0],parseInt(json[i][1]));
        dataValue.push(temp);
    }
    //document.getElementById("json_string").innerHTML=JSON.stringify(dataValue);
    var data =  [
        {
            "metadata" : {
                "names" : [arguments[1],"count"],
                "types" : ["ordinal","linear"]
            },
            "data":dataValue
                //[["INFO", 30], ["WARN",40],["ERROR",50]]
        }
    ];
    //JSON.stringify(arguments[0]);
    var config = {
        x : arguments[1],
        charts : [
            {type: "bar",  y : "count"}
        ],
       // maxLength: 10,
        width: 1000,
        height: 400
    }
    var lineChart = new vizg(data, config);
    lineChart.draw("#dChart");
}
function test(){
   // var res = JSON.parse(arguments);
    var res= arguments[0];
    document.getElementById("json_string").innerHTML=arguments[0];
}

function drawTime(){
    var json=arguments[0];
    var dataValue=[];

    for(var i=0;i<json.length;i++){
        var temp= [];
        temp.push(json[i][0],json[i][1],parseInt(json[i][2]));
        dataValue.push(temp);
    }
    var data =  [
        {
            "metadata" : {
                "names" : ["day","type","count"],
                "types" : ["ordinal","ordinal","linear"]
            },
            "data":dataValue
            //[["INFO", 30], ["WARN",40],["ERROR",50]]
        }
    ];
    //JSON.stringify(arguments[0]);
    var config = {
        x : "day",
        charts : [
            {type: "bar",  y : "count", color:"type"}
        ],
        // maxLength: 10,
        width: 1000,
        height: 400
    }
    var lineChart = new vizg(data, config);
    lineChart.draw("#timeChart");
}
function timeData(){
    var query = $('#ftexte').val();
    if(query==""){
        selectField2();
    }
    else {
        //document.getElementById("json_string2").innerHTML=fieldName;
        query = query + ",," + fieldName;

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
            url: serverUrl + "/api/dashboard/epochTimeData",
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
                drawTime(res,fieldName);
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
$(function() {
    $( "#dChart" ).draggable();

    $("#timeChart").draggable();
   // $("#fieldsName").selectmenu();
   // $( "#dChart").resizable();

    //$("#timeChart").resizable();
});
function test(){
    // var res = JSON.parse(arguments);
    var res= arguments[0];
    document.getElementById("json_string").innerHTML=arguments[0];
}