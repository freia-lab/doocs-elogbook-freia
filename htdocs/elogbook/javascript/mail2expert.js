function verify()
{
    // 0 Object is not initialized; 1 Loading object is loading data; 2 Loaded object has loaded data
    // 3 Data from object can be worked with; 4 Object completely initialized
    if (xmlFile.readyState != 4){
        return false;
    }
    else return true;
}

function importXML(list)
{
    // Used for all browsers except IE
    if(window.XMLHttpRequest){
        xmlDoc = new window.XMLHttpRequest();
        xmlDoc.open("GET",list,false);
        xmlDoc.send("");
        startFunction();
    }
    // IE needs this
    else if (window.ActiveXObject)
    {
        xmlFile = new ActiveXObject("Microsoft.XMLDOM");
        xmlFile.async="false";
        xmlFile.load(list);
        xmlFile.onreadystatechange=verify;
        xmlDoc=xmlFile.documentElement;
        if (xmlFile.readyState == 4) startFunction();
    } else{
        alert("XML loading not supported.");
        return null;
    }
}

function startFunction()
{
    var elems = xmlDoc.responseXML.getElementsByTagName('subject');
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
    elems = xmlDoc.responseXML.getElementsByTagName('subject')[selected_n];

    var firstname = elems.getElementsByTagName('f_name');	
    var lastname = elems.getElementsByTagName('l_name');
    var expert = elems.getElementsByTagName('expert');
    document.inputForm.expertlist.options[0] = new Option("-----");
    for (var i = 0; i < lastname.length; i++){
        if ( expert[i].hasChildNodes() ) {
	    if(firstname[i].hasChildNodes()) {
		document.inputForm.expertlist.options[i+1] = new Option(lastname[i].firstChild.nodeValue + ", " + firstname[i].firstChild.nodeValue + " (" + expert[i].firstChild.nodeValue + ")");
	    } else {
		document.inputForm.expertlist.options[i+1] = new Option(lastname[i].firstChild.nodeValue + " (" + expert[i].firstChild.nodeValue + ")");
	    }
        }
        else {
	    if(firstname[i].hasChildNodes()) {
		document.inputForm.expertlist.options[i+1] = new Option(lastname[i].firstChild.nodeValue + ", " + firstname[i].firstChild.nodeValue);
	    } else {
		document.inputForm.expertlist.options[i+1] = new Option(lastname[i].firstChild.nodeValue);
	    }
        }
    }
}

function addExpert() {
    var selected_n = document.inputForm.topic.selectedIndex;
    elems = xmlDoc.responseXML.getElementsByTagName('subject')[selected_n];

    var lastname = elems.getElementsByTagName('l_name');
    var mail = elems.getElementsByTagName('mail');
    var EInteger = document.inputForm.expertlist.selectedIndex-1;
    var EString = lastname[EInteger].firstChild.nodeValue;
    var MString = mail[EInteger].firstChild.nodeValue;
    document.inputForm.experts.value = document.inputForm.experts.value + EString + " ";
    document.inputForm.email.value = document.inputForm.email.value + MString + " ";
}

function addFreeEmail() {
    var femail = document.inputForm.femail.value;
    var filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    if (!filter.test(femail)) {
        alert('Invalid email address!');
        femail.focus;
        return false;
    }
    document.inputForm.email.value = document.inputForm.email.value +  femail + " ";
    document.inputForm.experts.value = document.inputForm.experts.value +  femail + " ";
    document.inputForm.femail.value = "";
}

function clearExpert() {
    document.inputForm.experts.value = "";
    document.inputForm.email.value = "";
}
