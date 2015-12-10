var serverUrl = window.location.origin;
var resultTable =  $('#results-table').DataTable( {
    "ajax" : {
        "url": serverUrl + "/api/search",
        "type": "POST",
        "dataType": "json",
        "contentType": "application/json; charset=utf-8",
        "data": function (payload) {
            payload.query = $("#search-field").val()
            payload.start = 0
            payload.count = 100
            return JSON.stringify(payload)
        },
        "dataSrc" : function(d){
            return d
        }
    },
    "columns": [
        {"data": "values._message"}
    ]

});

$(document).ready(function () {
    $('[data-toggle="popover"]').popover({
        html : true,
        content: function() {
            return $('#timeListContent').html();
        }
    });
});

function searchActivities2() {
    var payload = {};
    payload.query = $("#search-field").val()
    payload.start = 0
    payload.count = 100
    console.log(payload)
    jQuery.ajax({
        type: "POST",
        data : JSON.stringify(payload),
        dataType : "json",
        contentType : "application/json; charset=utf-8",
        url: serverUrl + "/api/search",
        success: function(res) {
            appendDataToTable(res);
        },
        error: function(res) {
            alert(res.responseText);
        }
    });
}

function changeTime(value) {
    $("#time-set-btn").text(value);
}

function searchActivities(data){
  resultTable.ajax.reload();
}

