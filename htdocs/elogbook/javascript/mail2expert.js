function verify()
{
    // 0 Object is not initialized; 1 Loading object is loading data; 2 Loaded object has loaded data
    // 3 Data from object can be worked with; 4 Object completely initialized
    if (xmlFile.readyState != 4){ return false; }
    else return true;
}

function importXML(list,funcname)
{
    // Used for all browsers except IE
    if (document.implementation && document.implementation.createDocument)
	{
	    xmlDoc = document.implementation.createDocument("", "", null);
	    xmlDoc.onload = funcname;
	    xmlDoc.load(list);
	}
    // IE needs this
    else if (window.ActiveXObject)
	{
	    xmlFile = new ActiveXObject("Microsoft.XMLDOM");
	    xmlFile.async="false";
	    xmlFile.load(list);
	    xmlFile.onreadystatechange=verify;
	    xmlDoc=xmlFile.documentElement;
	    if (xmlFile.readyState == 4) funcname();
 	}
    else return;
}

function startFunction()
{
    var elems = xmlDoc.getElementsByTagName('subject');
    for (var i = 0; i < elems.length; i++){
	document.inputForm.topic.options[i] = new Option(elems[i].getAttribute('name'));
    }
    createList();
}

function createList()
{	
    for (var d = 0; d < document.inputForm.expertlist.options.length; i++){
	document.inputForm.expertlist.options[d] = null;
    }

    var selected_n = document.inputForm.topic.selectedIndex;
    elems = xmlDoc.getElementsByTagName('subject')[selected_n];

    var firstname = elems.getElementsByTagName('f_name');	
    var lastname = elems.getElementsByTagName('l_name');
    var expert = elems.getElementsByTagName('expert');
    for (var i = 0; i < lastname.length; i++){
	if ( expert[i].hasChildNodes() ) {
	    document.inputForm.expertlist.options[i] = new Option(lastname[i].firstChild.nodeValue + ", " +
								  firstname[i].firstChild.nodeValue +
								  " (" + expert[i].firstChild.nodeValue + ")");
	}
	else {
	    document.inputForm.expertlist.options[i] = new Option(lastname[i].firstChild.nodeValue + ", " +
								  firstname[i].firstChild.nodeValue);
	}
	if (i==0)
	    document.inputForm.expertlist.options[i].setAttribute("onclick","addExpert()");
    }
}

function addExpert() {
    var selected_n = document.inputForm.topic.selectedIndex;
    elems = xmlDoc.getElementsByTagName('subject')[selected_n];

    var lastname = elems.getElementsByTagName('l_name');
    var mail = elems.getElementsByTagName('mail');
    var EInteger = document.inputForm.expertlist.selectedIndex;
    var EString = lastname[EInteger].firstChild.nodeValue;
    var MString = mail[EInteger].firstChild.nodeValue;
    document.inputForm.experts.value = document.inputForm.experts.value + EString + " ";
    document.inputForm.email.value = document.inputForm.email.value + MString + " ";
}

function clearExpert() {
    document.inputForm.experts.value = "";
    document.inputForm.email.value = "";
}
