var serverUrl = window.location.origin;
var resultTable =  $('#results-table').DataTable( {
    "processing": false,
    "serverSide": true,
    "ajax" : {
        "url": serverUrl + "/api/search",
        "type": "POST",
        "dataType": "json",
        "contentType": "application/json; charset=utf-8",
        "data": function (payload) {
            payload.query = $("#search-field").val();
            payload.timeFrom = parseInt($("#timestamp-from").val());
            payload.timeTo = parseInt($("#timestamp-to").val());
            return JSON.stringify(payload)
        }
    },
    "columns": [
        {"data": "values._message"}
    ],
    "searching": false
});

/* Formatting a table to insert when a row is clicked */
function format(data) {

    var tableStr = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">';
    for(var key in data.values) {
        tableStr = tableStr +
        '<tr>' +
        '<td>' + capitalizeFirstLetter(key.replace("_","")) + '</td>' +
        '<td>' + data.values[key] + '</td>' +
        '</tr>';
    }
    tableStr = tableStr + '</table>';
    return tableStr;
}

/* Capitalize first letter of a given string*/
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

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
            $('.timepicker').timepicker(
                {
                    'step': '20',
                    'minTime': '9:00am',
                    'maxTime': '12:00pm',
                    'timeFormat': 'H:i:s'
                }
            );
        }
    });

    $('#results-table').find('tbody').on( 'click', 'tr', function () {
        var tr = $(this).closest('tr');
        var row = resultTable.row( tr );

        if ( row.child.isShown() ) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        }
        else {
            // Open this row
            row.child( format(row.data()) ).show();
            tr.addClass('shown');
        }
    } );

    $('#save-options').click(function () {
        if ("pdf" == $("#save-options").val()) {
            var doc = new jsPDF();
            doc.fromHTML($('#tab-preview').html(), 15, 15, {
                'width': 170
            });
            var string = doc.output('datauristring');
            var x = window.open();
            x.document.open();
            x.document.location = string;
        } else if ("csv" == $("#save-options").val()) {
            tableToCSV($('#results-table').dataTable(), 'table.table');
        }
    });

    if (window.location.search.indexOf('query') > -1) {
        urlParams = splitUrl();
        var decodeQuery=(urlParams["query"]);
        $("#search-field").val(window.atob(urlParams["query"]));
        $("#timestamp-from").val(window.atob(urlParams["timeFrom"]));
        $("#timestamp-to").val(window.atob(urlParams["timeTo"]));
        searchActivities();
    }

});

function splitUrl() {
    var urlParams;
    var match,
        pl = /\+/g,  // Regex for replacing addition symbol with a space
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) {
            return decodeURIComponent(s.replace(pl, " "));
        },
        query = window.location.search.substring(1);
    urlParams = {};
    while (match = search.exec(query))
        urlParams[decode(match[1])] = decode(match[2]);
    return urlParams;
}

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

function changeTime(value, timestampFrom, timestampTo) {
    var buttonHeight = $("#date-time-select").outerHeight();
    var buttonWidth = $("#date-time-select").outerWidth();
    $("#date-time-select").css({ 'height': buttonHeight});
    $("#date-time-select").css({ 'width': buttonWidth});
    $("#timestamp-from").val(timestampFrom);
    $("#timestamp-to").val(timestampTo);
    $("#date-time-select").text(value);
}

function assignDateRange() {
    var dateFrom = $("#dateRangeDatePickerFrom").val();
    var dateTo = $("#dateRangeDatePickerTo").val();
    changeTime(dateFrom + "-" + dateTo, new Date(dateFrom).getTime(), new Date(dateTo).getTime());
}

function assignDateTimeRange() {
    var dateFrom = $("#dateTimeRangeDatePickerFrom").val();
    var timeFrom = $("#dateTimeRangeTimePickerFrom").val();
    var dateTo = $("#dateTimeRangeDatePickerTo").val();
    var timeTo = $("#dateTimeRangeTimePickerTo").val();
    changeTime(dateFrom + ":" + timeFrom + "-" + dateTo + ":" + timeTo, new Date(dateFrom + " " + timeFrom).getTime(),
        new Date(dateTo + " " + timeTo).getTime());
}

function getLastWeek(){
    var today = new Date();
    return lastWeek = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 7);
}

function getLastMonth(){
    var today = new Date();
    return lastWeek = new Date(today.getFullYear(), today.getMonth() -1, today.getDate());
}

function searchActivities(data){
  resultTable.ajax.reload();
}

function tableToCSV(table, tableElm) {
    var csv = [];

    // Get header names
    $(tableElm+' thead').find('th').each(function() {
        var $th = $(this);
        var text = $th.text();
        if(text != "") csv.push(text);
    });

    // get table data
    var total = table.fnSettings().fnRecordsTotal();
    for(i = 0; i < total; i++) {
        var row = table.fnGetData(i).values['_message'];
        csv.push(row);
    }

    var csvContent = "data:text/csv;charset=utf-8,";
    csv.forEach(function(infoArray, index){

        var dataString = infoArray;
        csvContent += index < csv.length ? dataString+ "\n" : dataString;

    });
-link
    var encodedUri = encodeURI(csvContent);
    var link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "search_result.csv");
    link.click();
}

/*----------------Delete---------------------------*/
$("#alert-link").click(function(){
    window.location = serverUrl + '/loganalyzer/site/alert/alert.jag';
});

$("#save-options").change(function(){

    var query = $("#search-field").val();
    alert(query);
    var timeFrom = parseInt($("#timestamp-from").val());
   var timeTo = parseInt($("#timestamp-to").val());
    if($(this).val()=='alert'){
        window.location=serverUrl+'/loganalyzer/site/alert/alert.jag?'+"query="+query + "&" + "timefrom="+timeFrom + "&" + "timeto="+timeTo;
    }
});



/*---------------------Delete above----------------------------*/