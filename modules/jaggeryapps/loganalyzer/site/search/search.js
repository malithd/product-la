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
    var showPopover = $.fn.popover.Constructor.prototype.show;
    $.fn.popover.Constructor.prototype.show = function () {
        showPopover.call(this);
        if (this.options.showCallback) {
            this.options.showCallback.call(this);
        }
    };

    $("#date-time-select").popover({
        html: true,
        content: function() {
            return $('#timeListContent').html();
        },
        showCallback: function () {
            $('.datepicker').datepicker();
            //$('.datetimepicker').datetimepicker();
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
    $("#date-time-select").text(value);
}

function assignDateRange() {
    var dateFrom = $("#datePickerFrom").val();
    var dateTo = $("#datePickerTo").val();
    changeTime(dateFrom + "-" + dateTo);
}

function assignDateTimeRange() {
    var dateTimeFrom = $("#dateTimePickerFrom").val();
    var dateTimeTo = $("#dateTimePickerTo").val();
    changeTime(dateTimeFrom + "-" + dateTimeTo);
}

function searchActivities(data){
  resultTable.ajax.reload();
}

