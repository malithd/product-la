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
            var html = "";
            $.each(res, function (key, alert) {
                html += createTable(alert);
            });
            $("#alert-list-table").append(html);
        },
        error:function(res){
            alert(res);
        }
    });
}

function createTable(alert){
    return '<tr><td>'+ alert.alertName +'</td><td>' + alert.description + '</td><td><a  onclick=deleteAlert(\''+alert.alertName+'\')>Delete</a></td><td><a  onclick=updateContent(\''+alert.alertName+'\')>Update</a></td></tr>';
}

function deleteAlert(alertName){
    jQuery.ajax({
        type:"DELETE",
        url:serverUrl+"/api/alert/delete/"+alertName,
        success:function(res){
            window.location=serverUrl+'/loganalyzer/site/alert/alert.jag';
        },
        error:function(res){
            alert(res);
        }
    });
}

function updateContent(alertName){
    $(".inner-container").show();
    $(".alert-list").hide();
    $("#alert-save-btn").hide();
    $("#alert-update-btn").show();
    jQuery.ajax({
        type:"GET",
        url:serverUrl+"/api/alert/getAlertContent/"+alertName,
        success:function(res){
            $("#alert-name-txt").val(res.alertName);
            $("#alert-name-txt").attr('disabled', 'disabled');
            $("#alert-des-txa").val(res.description);
            $("#filter-txt").val(res.query);
            $("#timestamp-from").val(res.timeFrom);
            $("#timestamp-to").val(res.timeTo);
            $("#cron-exp").val(res.cronExpression);
            $("#cond-type").val(res.condition);
            $("#cmp-val").val(res.conditionValue);
             $("#alert-action").val(res.alertActionType);
            if(res.alertActionType=='logger'){
                loadAction();
                $("#action-logger-uniqueId").val(res.alertActionProperties.uniqueId);
                $("#logger-message").val(res.alertActionProperties.message);
            }
            else if(res.alertActionType=='email'){
                loadAction();
                $("#action-email-address").val(res.alertActionProperties.email_address);
                $("#action-email-subject").val(res.alertActionProperties.email_subject);
                $("#action-email-type").val(res.alertActionProperties.email_type);
                $("#email-message").val(res.alertActionProperties.message);
            }
            else if(res.alertActionType=='sms') {
                loadAction();
                $("#action-sms-phoneNo").val(res.alertActionProperties.sms_no);
                $("#sms-message").val(res.alertActionProperties.message);
            }
        },
        error:function(res){
            alert(res);
        }
    });
}

function saveAlert(){

    var alertName=$("#alert-name-txt").val();
    var cmpValue=$("#cmp-val").val();
    var query=$("#filter-txt").val();
    var valuesSlt=false;
    if(!isValidName(alertName)){
        alert("Invalid Alert Name");
        return;
    }
    if (!isValidNumber(cmpValue)||cmpValue=="") {
        alert("Invalid Trigger Value");
        return;
    }
    if (query=="") {
        alert("Search can't be empty");
        return;
    }
    var payload={};
    var fields={};
    var count=0;
    var action={};
   var name=jQuery.trim($("#alert-name-txt").val());
    payload.alertName=alertName;
    payload.description=$("#alert-des-txa").val();
    payload.query=query;
    payload.timeFrom = $("#timestamp-from").val();
    payload.timeTo=$("#timestamp-to").val();
    payload.cronExpression=$("#cron-exp").val();
    payload.condition=$("#cond-type").val();
    payload.conditionValue=cmpValue;
    payload.alertActionType=$("#alert-action").val();
    $('input[name="columns"]:checked').each(function() {
        valuesSlt=true;
        var fieldName="field"+count;
        var field;
        fields[fieldName]=this.value;
        count+=1;
       // fields."field"+count=this.value;
      //  fields.push(this.value);
      //  console.log(this.text());
    });
    payload.fields=fields;
    if (payload.alertActionType=="logger"){
        var uniqueId=$("#action-logger-uniqueId").val();
        var loggerMessage=$("#logger-message").val();
        if (uniqueId == "") {
            alert("Unique Id can't be empty");
            return;
        }
        if (loggerMessage == "") {
            alert("Message can't be empty");
            return;
        }
        if (valuesSlt) {
            loggerMessage+=" {{values}}"
        }
        if ($("#countSlt").is(":checked")) {
            loggerMessage+=" {{count}}";
        }
        action.uniqueId=uniqueId;
        action.message=loggerMessage;
    }
    if (payload.alertActionType=="email"){
        var emailAddressesTxt=$("#action-email-address").val();
        var emailAddresses = emailAddressesTxt.split(",");
        var subject=$("#action-email-subject").val();
        var emailMessage=$("#email-message").val();
        for (var i in emailAddresses){
            var emailAddress= emailAddresses[i].trim();
            if (!isValidEmail(emailAddress)) {
                alert("Invalid Email Address "+emailAddress);
                return;
            }
        }
        if (subject == "") {
            alert("Subject can't be empty");
            return;
        }
        if (subject == "") {
            alert("Message can't be empty");
            return;
        }
        if (valuesSlt) {
            emailMessage+=" Log Values {{values}}"
        }
        if ($("#countSlt").is(":checked")) {
            emailMessage+=" Result Count {{count}}";
        }

        action.email_address=emailAddressesTxt;
        action.email_subject=subject;
        action.email_type=$("#action-email-type").val();
        action.message=emailMessage;
    }
    if (payload.alertActionType=="sms"){
        var phoneNo = $("#action-sms-phoneNo").val();
        var smsMessage =$("#sms-message").val();
        if (!isValidPhoneNo(phoneNo)) {     //||phoneNo == ""
            alert("Invalid Phone Number");
            return;
        }
        if (smsMessage == "") {
            alert("Message can't be empty");
            return;
        }
        if (valuesSlt) {
            smsMessage+=" {{values}}"
        }
        if ($("#countSlt").is(":checked")) {
            smsMessage+=" {{count}}";
        }
        action.sms_no=phoneNo;
        action.message=smsMessage;
    }
    payload.alertActionProperties=action;
    var data=JSON.stringify(payload);
    jQuery.ajax({
        type: "POST",
        data : data,
        contentType : "application/json; charset=utf-8",
        url: serverUrl + "/api/alert/save",
        success: function(res) {
            window.location=serverUrl+'/loganalyzer/site/alert/alert.jag';
        },
        error: function(res) {
            alert(res.responseText);
        }
    });

}

function updateAlert(){
    var query=$("#filter-txt").val();
    var cmpValue=$("#cmp-val").val();
    var payload={};
    var action={};
    var count=0;
    var valuesSlt=false;
    if (query=="") {
        alert("Search can't be empty");
        return;
    }
    if (!isValidNumber(cmpValue)||cmpValue=="") {
        alert("Invalid Trigger Value");
        return;
    }
    payload.alertName=$("#alert-name-txt").val();
    payload.description=$("#alert-des-txa").val();
    payload.query=query;
    payload.timeFrom = $("#timestamp-from").val();
    payload.timeTo=$("#timestamp-to").val();
    payload.cronExpression=$("#cron-exp").val();
    payload.condition=$("#cond-type").val();
    payload.conditionValue=cmpValue;
    payload.alertActionType=$("#alert-action").val();
    $('input[name="columns"]:checked').each(function() {
        valuesSlt=true;
        var fieldName="field"+count;
        var field;
        fields[fieldName]=this.value;
        count+=1;
        // fields."field"+count=this.value;
        //  fields.push(this.value);
        //  console.log(this.text());
    });
    payload.fields=fields;
    if (payload.alertActionType=="logger"){
        var uniqueId=$("#action-logger-uniqueId").val();
        var loggerMessage=$("#logger-message").val();
        if (uniqueId == "") {
            alert("Unique Id can't be empty");
            return;
        }
        if (loggerMessage == "") {
            alert("Message can't be empty");
            return;
        }
        if (valuesSlt) {
            loggerMessage+=" {{values}}"
        }
        if ($("#countSlt").is(":checked")) {
            loggerMessage+=" {{count}}";
        }
        action.uniqueId=uniqueId;
        action.message=loggerMessage;
    }
    if (payload.alertActionType=="email"){
        var emailAddressesTxt=$("#action-email-address").val();
        var emailAddresses = emailAddressesTxt.split(",");
        var subject=$("#action-email-subject").val();
        var emailMessage=$("#email-message").val();
        for (var i in emailAddresses){
            if (!isValidEmail(emailAddresses[i])) {
                alert("Invalid Email Address "+emailAddresses[i]);
                return;
            }
        }
        if (subject == "") {
            alert("Subject can't be empty");
            return;
        }
        if (subject == "") {
            alert("Message can't be empty");
            return;
        }
        if (valuesSlt) {
            emailMessage+=" Log Values {{values}}"
        }
        if ($("#countSlt").is(":checked")) {
            emailMessage+=" Result Count {{count}}";
        }

        action.email_address=emailAddressesTxt;
        action.email_subject=subject;
        action.email_type=$("#action-email-type").val();
        action.message=emailMessage;
    }
    if (payload.alertActionType=="sms"){
        var phoneNo = $("#action-sms-phoneNo").val();
        var smsMessage =$("#sms-message").val();
        if (!isValidPhoneNo(phoneNo)) {     //||phoneNo == ""
            alert("Invalid Phone Number");
            return;
        }
        if (smsMessage == "") {
            alert("Message can't be empty");
            return;
        }
        if (valuesSlt) {
            smsMessage+=" {{values}}"
        }
        if ($("#countSlt").is(":checked")) {
            smsMessage+=" {{count}}";
        }
        action.sms_no=phoneNo;
        action.message=smsMessage;
    }
    payload.alertActionProperties=action;
    var data=JSON.stringify(payload);
    jQuery.ajax({
        type: "PUT",
        data : data,
        contentType : "application/json; charset=utf-8",
        url: serverUrl + "/api/alert/update",
        success: function(res) {
            window.location=serverUrl+'/loganalyzer/site/alert/alert.jag';
        },
        error: function(res) {
            alert(res.responseText);
        }
    });
}

    $("#add-alert-btn").click(function () {
        $(".inner-container").show();

        $(".alert-list").hide();

    });
/*---------------------------------------------------------------
*/
function isValidName(string){
    var alertNamePattern = /^([a-z]|[A-Z]|_|\.|-)([a-z]|[A-Z]|[0-9]|_|\.|-)*$/i;
    return (alertNamePattern.test(string));
}

function isValidNumber(number){
    var integerPattern= /^([0]|[1-9])*$/g;
    return (integerPattern.test(number));
}

function isValidPhoneNo(number){
    var PhoneNoPattern = /^(?:\+?(\d{1,3}))?([-. (]*(\d{3})[-. )]*)?((\d{3})[-. ]*(\d{2,4})(?:[-.x ]*(\d+))?)*$/gm;
    return (PhoneNoPattern.test(number));
}

function isValidEmail(email) {
    var emailPattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return emailPattern.test(email);
}
///^(?:\+?(\d{1,3}))?([-. (]*(\d{3})[-. )]*)?((\d{3})[-. ]*(\d{2,4})(?:[-.x ]*(\d+))?)*$/gm

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

function backward(){
    window.location=serverUrl+'/loganalyzer/site/alert/alert.jag';
}

function getColumns(){
    jQuery.ajax({
        type:"GET",
        url: serverUrl+"/api/alert/getColumns",
        success:function(res){
                var htmlcolunm = "";
                $.each(res, function (count) {
                    var value=res[count];
                    htmlcolunm += createList(value);
                });
            htmlcolunm+='<input type="checkbox" id="countSlt" name="count" value="count">'+"Result Count";
                $("#columns").append(htmlcolunm);
        },
        error:function (res) {
            alert(res);
        }
    });
}

function createList(column) {
    $("#columns").empty();
    return  '<input type="checkbox" id="columnsslt" name="columns" value=\"'+column+'\">'+column+'<br>';
}

//function callAlert(){
//    var payload={};
//    payload.streamName="loganalyzer";
//    payload.alertName="Alert1";
//    payload.description="This is an alert";
//    payload.alertType="Real Time";
//    payload.filter="logType=WARN"
//    payload.fields=[
//        {"field0":"timestam"},
//        {"field1":"javaClass"}
//    ]
//    jQuery.ajax({
//        type: "POST",
//        data : JSON.stringify(payload),
//        dataType : "json",
//        contentType : "application/json; charset=utf-8",
//        url: serverUrl + "/api/alet/save",
//        success: function(res) {
//            alert(res.responseText);
//        },
//        error: function(res) {
//            alert(res.responseText);
//        }
//    });
//    alert(JSON.stringify(payload));
//
//}

/*-------------Tab Pane handeling-----------------*/

//jQuery(document).ready(function() {
//
//    jQuery('.alert-tabs .tab-links a').on('click', function(e)  {
//        var currentAttrValue = jQuery(this).attr('href');
//
//        // Show/Hide Tabs
//        jQuery('.alert-tabs ' + currentAttrValue).show().siblings().hide();
//
//        // Change/remove current tab to active
//        jQuery(this).parent('li').addClass('active').siblings().removeClass('active');
//
//        e.preventDefault();
//    });
//
//    $("#daily-timepicker").timepicker({  timeFormat: 'H:i'
//        //,interval:30  // 15
//    });
//
//    $("#weekly-timepicker").timepicker({  timeFormat: 'H:i'
//        //,interval:30  // 15
//    });
//
//    $("#monthly-timepicker").timepicker({  timeFormat: 'H:i'
//        //,interval:30  // 15
//    });
//
//    $(".monthly-datepicker").datepicker({
//        dateFormat: 'dd',
//        changeYear:true,
//        changeMonth:true,
//        onSelect: function(dateText, inst) {
//            alert(dateText); // alerts the day name
//        }
//    });
//
//
//    if (window.location.search.indexOf('query') > -1) {
//        $(".inner-container").show();
//        urlParams = splitUrl();
//        setParams(urlParams);
//        $(".alert-list").hide();
//
//    }
//
//    loadContent();
//    loadCompare();
//});

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