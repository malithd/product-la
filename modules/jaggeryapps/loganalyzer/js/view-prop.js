function genTable(a) {
    for (var key in a) {
        switch (key) {
            case 'logStream':
                $('#ttt > tbody:last-child').append('<tr><th width="10%" id="ttt">Log Stream:</th><td id="ttt">' + a[key] + '</td></tr>');
                break;
            case 'fileName':
                $('#ttt > tbody:last-child').append('<tr><th width="10%" id="ttt">File Name:</th><td id="ttt">' + a[key] + '</td></tr>');
                break;
            case 'delimeter':
                if( a[key]!='') {
                    $('#ttt > tbody:last-child').append('<tr><th width="10%" id="ttt">Delimeter:</th><td id="ttt">' + a[key] + '</td></tr>');
                }
                break;
            case 'regPatterns':
                if( jQuery.isEmptyObject(a[key])) {
                    $('#ttt > tbody:last-child').append('<tr><th width="10%" id="ttt">RegEx Patterns:</th><td id="ttt">' + a[key] + '</td></tr>');
                }
                break;
        }
    }
    $('#ttt > tbody:last-child').append('<tr><td colspan="2" id="ttt"><button style="margin-left: 35%;" class="btn btn-main" id="searchLog">Search</button></td></tr>');
}