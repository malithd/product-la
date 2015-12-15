function deleteRow(row){
    //Get the id of the current row
    var rowId = $(row).parent().parent().data('rowId');
    delete a[rowId];
    $(row).parent().parent().remove();
}

$(function(){
    $('#tab-saved-patterns').on('click',function(){
        //Clean the existing table
        $('#saved-regex-table > tbody').html('');
        for(var key in a) {
            $('#saved-regex-table > tbody:last-child').append('<tr data-row-id="' + key + '"><td>'+key+'</td><td>'+a[key]+'</td><td><a href="#" onclick="deleteRow(this)">Delete</a></td></tr>');
        }
        //$('#regEx_patterns tbody').append("<tr><td class='line'>"+regName+"</td><td class='line'>"+regVal+"</td></tr>");
    });
});