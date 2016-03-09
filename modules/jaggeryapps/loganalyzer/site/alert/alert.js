/**
 * Created by nalaka on 2/19/16.
 */
var serverUrl = window.location.origin;

jQuery(document).ready(function() {

    jQuery('.alert-tabs .tab-links a').on('click', function(e)  {
        var currentAttrValue = jQuery(this).attr('href');

        // Show/Hide Tabs
        jQuery('.alert-tabs ' + currentAttrValue).show().siblings().hide();

        // Change/remove current tab to active
        jQuery(this).parent('li').addClass('active').siblings().removeClass('active');

        e.preventDefault();
    });

    $("#daily-timepicker").timepicker({  timeFormat: 'H:i'
        //,interval:30  // 15
    });

    $("#weekly-timepicker").timepicker({  timeFormat: 'H:i'
        //,interval:30  // 15
    });

    $("#monthly-timepicker").timepicker({  timeFormat: 'H:i'
        //,interval:30  // 15
    });

    $(".monthly-datepicker").datepicker({
        dateFormat: 'dd',
        changeYear:true,
        changeMonth:true,
        onSelect: function(dateText, inst) {
            alert(dateText); // alerts the day name
        }
    });


    if (window.location.search.indexOf('query') > -1) {
        $(".inner-container").show();
        urlParams = splitUrl();
        setParams(urlParams);
        $(".alert-list").hide();

    }

    getAllAlerts();

    loadAction();
    loadContent();
    loadCompare();
});

function getAllAlerts(){
    jQuery.ajax({
        type:"GET",
        url:serverUrl+"/api/alert/getAllScheduleAlerts",
        success: function(res){
            alert(res);
            var html = "";
            $.each(res, function (key, alert) {
                html += createTable(alert);
            });
            $("#alert-list").append(html);
        },
        error:function(res){
            alert(res);
        }
    });
}

function createTable(alert){
    return '<tr><td>'+ alert.alertName +'</td><td>' + alert.description + '</td><td><a  onclick=deleteAlert(\''+alert.alertName+'\')>Delete</a></td><td><a  onclick=updateAlert(\''+alert.alertName+'\')>Update</a></td></tr>';
}

function deleteAlert(alertName){
    alert(alertName);
    jQuery.ajax({
        type:"DELETE",
        url:serverUrl+"/api/alert/delete/"+alertName
    });
}

function updateAlert(alertName){
    alert(alertName);
}












function callAlert(){
    var payload={};
    payload.streamName="loganalyzer";
    payload.alertName="Alert1";
    payload.description="This is an alert";
    payload.alertType="Real Time";
    payload.filter="logType=WARN"
    payload.fields=[
        {"field0":"timestam"},
        {"field1":"javaClass"}
    ]
    jQuery.ajax({
        type: "POST",
        data : JSON.stringify(payload),
        dataType : "json",
        contentType : "application/json; charset=utf-8",
        url: serverUrl + "/api/alet/save",
        success: function(res) {
            alert(res.responseText);
        },
        error: function(res) {
            alert(res.responseText);
        }
    });
    alert(JSON.stringify(payload));

}

/*-------------Tab Pane handeling-----------------*/

jQuery(document).ready(function() {

    jQuery('.alert-tabs .tab-links a').on('click', function(e)  {
        var currentAttrValue = jQuery(this).attr('href');

        // Show/Hide Tabs
        jQuery('.alert-tabs ' + currentAttrValue).show().siblings().hide();

        // Change/remove current tab to active
        jQuery(this).parent('li').addClass('active').siblings().removeClass('active');

        e.preventDefault();
    });

    $("#daily-timepicker").timepicker({  timeFormat: 'H:i'
        //,interval:30  // 15
    });

    $("#weekly-timepicker").timepicker({  timeFormat: 'H:i'
        //,interval:30  // 15
    });

    $("#monthly-timepicker").timepicker({  timeFormat: 'H:i'
        //,interval:30  // 15
    });

    $(".monthly-datepicker").datepicker({
        dateFormat: 'dd',
        changeYear:true,
        changeMonth:true,
        onSelect: function(dateText, inst) {
            alert(dateText); // alerts the day name
        }
    });


    if (window.location.search.indexOf('query') > -1) {
        $(".inner-container").show();
        urlParams = splitUrl();
        setParams(urlParams);
        $(".alert-list").hide();

    }

    loadContent();
    loadCompare();
});

    $("#add-alert-btn").click(function () {
        $(".inner-container").show();

        $(".alert-list").hide();

    });
/*---------------------------------------------------------------
*/

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

function setParams(values){
    for(var key in values){
        switch(key){
            case "query":
                $("#filter-txt").val(values[key]);
                break;
        }
    }
}

function loadCompare(){
    var value=$("#trigger-type").val();
    switch(value){
        case "0":
            $("#compare").show();
            break;
    }
}

function loadContent(){
    var value=$("#sch-type").val();
    if(value==0){
        $("#load-content-daily").hide();
        $("#load-content-weekly").hide();
        $("#load-content-monthly").hide();
        $("#load-content-cronExp").hide();
        $("#load-content-hourly").css("display", "flex");

    }
    if(value==1){
        $("#load-content-hourly").hide();
        $("#load-content-weekly").hide();
        $("#load-content-monthly").hide();
        $("#load-content-cronExp").hide();
        $("#load-content-daily").css("display", "flex");

    }
    if(value==2){
        $("#load-content-hourly").hide();
        $("#load-content-daily").hide();
        $("#load-content-monthly").hide();
        $("#load-content-cronExp").hide();
        $("#load-content-weekly").css("display", "flex");

    }
    if(value==3){
        $("#load-content-hourly").hide();
        $("#load-content-daily").hide();
        $("#load-content-weekly").hide();
        $("#load-content-cronExp").hide();
        $("#load-content-monthly").css("display", "flex");

    }
    if(value==4){
        $("#load-content-hourly").hide();
        $("#load-content-daily").hide();
        $("#load-content-weekly").hide();
        $("#load-content-monthly").hide();
        $("#load-content-cronExp").show();
    }

}

function cronBuilder (){
    var value=$("#sch-type").val();
    if(value==0){
        var minute=$("#slt-minute").val();
        $("#cron-exp").val("0 0/"+minute+" * * * ?");
    }
    if(value==1){
        var time=$("#daily-timepicker").val().split(":");
        $("#cron-exp").val("0 "+time[1]+" "+time[0]+" * * ?");

    }
    if(value==2){
        var day=$("#slt-weekday").val();
        var time=$("#weekly-timepicker").val().split(":");
        $("#cron-exp").val("0 "+time[1]+" "+time[0]+" ? * "+day);

    }
    if(value==3){
        var date=$("#monthly-datepicker").val();
        var time=$("#monthly-timepicker").val().split(":");
        $("#cron-exp").val("0 "+time[1]+" "+time[0]+" "+date+" * ?");    //0 15 10 15 * ?

    }
    if(value==4){
        var minute=$("#slt-minute").val();
        $("#cron-exp").val("0 0/"+minute+" * * * ?");

    }

    alert($("#cron-exp").val());

}

function saveAlert(){
    var payload={};
    var action={};
    payload.alertName=$("#alert-name-txt").val();
    payload.description=$("#alert-des-txa").val();
    payload.query=$("#filter-txt").val();
    payload.timeFrom = $("#timestamp-from").val();
    payload.timeTo=$("#timestamp-to").val();
    payload.cronExpression=$("#cron-exp").val();
    payload.condition=$("#cond-type").val();
    payload.conditionValue=$("#cmp-val").val();
    payload.alertActionType=$("#alert-action").val();
    if (payload.alertActionType=="logger"){
        action.uniqueId=$("#action-logger-uniqueId").val();
        action.message=$("#message").val();
    }
    if (payload.alertActionType=="email"){
        action.email_address=$("#action-email-address").val();
        action.email_subject=$("#action-email-subject").val();
        action.email_type=$("#action-email-type").val();
        action.message=$("#message").val();
    }
    if (payload.alertActionType=="sms"){
        action.sms_no=$("#action-sms-phoneNo").val();
        action.message=$("#message").val();
    }
    payload.alertActionProperties=action;
    var data=JSON.stringify(payload);
    alert(data);
    jQuery.ajax({
        type: "POST",
        data : data,
        dataType : "json",
        contentType : "application/json; charset=utf-8",
        url: serverUrl + "/api/alert/save",
        success: function(res) {
            alert(res);
        },
        error: function(res) {
            alert(res.responseText);
        }
    });


}

function loadAction(){
    var value=$("#alert-action").val();
    if(value=='logger'){
        $("#action-email").hide();
        $("#action-sms").hide();
        $("#action-logger").show();
    }
    if(value=='email'){
        $("#action-sms").hide();
        $("#action-logger").hide();
        $("#action-email").show();
    }
    if(value=='sms'){
        $("#action-email").hide();
        $("#action-logger").hide();
        $("#action-sms").show();
    }

}

/*
 $(function () {
 $("#realTime-alert").click(function () {
 var url = serverUrl + '/loganalyzer/site/alert/realtime.jag'
 $("#alert-content").load(url);

 });
 });
 $(function () {
 $("#schedule-alert").click(function () {
 var url = serverUrl + '/loganalyzer/site/alert/schedule.jag'
 $("#alert-content").load(url);

 });
 });
 */


/*if (window.location.search.indexOf('track=yes') > -1) {
 alert('track present');
 } else {
 alert('track not here');
 }*/