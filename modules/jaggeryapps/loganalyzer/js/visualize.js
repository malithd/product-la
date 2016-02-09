/**
 * Created by vithulan on 1/29/16.
 */
var serverUrl = window.location.origin;
var fields;
var tableResults;
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
    var x = document.getElementById("fieldsName").value;

    x=x.replace("\"","");
    x=x.replace("\"","");
    var payload ={};
    payload.query = x;
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
            addRow();
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
    //var row = table.insertRow(0);
    //var cell1 = row.insertCell(0);
    //var cell2 = row.insertCell(1);
    var i;
    var j = tableResults.length;
    var tester;
    document.getElementById("json_string").innerHTML=j;
    for(i=0;i<j;i++){
      //  cell1.innerHTML=tableResults[i];
        tester=tableResults[i].split(" : ");
        tester[0].replace('"',"");
        tester[1].replace(/"/g,"");
        tester[1].replace("|","");

        //tester[1].replace("\"","");
        tester[1].replace("]","");
        $("#data-table").append('<tr><td>'+tester[0]+'</td><td>'+tester[1]+'</td></tr>');
    }


}