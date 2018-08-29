function intPad(i) {
    if (i<10)
	return "0" + i;
    else	  
	return "" + i;
}

function prtDate(format) {
    var d    = new Date();
    var yyyy = d.getFullYear();
    var MM   = intPad(d.getMonth()+1);
    var dd   = intPad(d.getDate());
    if      (format == "MM/dd/yyyy") return MM + "/" + dd + "/" + yyyy;
    else if (format == "dd.MM.yyyy") return dd + "." + MM + "." + yyyy;
    else if (format == "yyyy-MM-dd") return yyyy + "-" + MM + "-" + dd;
    else return "Date format error!";
}

function prtTime() {
    var d = new Date();
    var hh = intPad(d.getHours());
    var mm = intPad(d.getMinutes());
    var ss = intPad(d.getSeconds());
    return hh + ":" + mm + ":" + ss;
}
    
