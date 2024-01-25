
// path to the servlet
var _servletPath = "Manager";

// timeout in ms for cookie
var _timeout = 10000;

// name of the communication cookie
var _cookieName = "rqstCookie";

// flag if client is an internet explorer or not
var isIE;


/*********************************************************
* initialization
*********************************************************/
function init()
{
    isIE = document.all?true:false;
    hideOther();
}


/*********************************************************
* sets a cookie with a given value
*********************************************************/
function setCookie( cName, cValue)//, expires, path, domain, secure ) 
{
	var expires_date = new Date( new Date().getTime() + _timeout );
	document.cookie = cName + "=" + cValue +
						";expires =" + expires_date;
}

/*********************************************************
* Sends creation Order
*********************************************************/
function send()
{
    alert("TODO ein neues Logbuch anlegen");
}

function shiftChanged()
{
	
	if(document.forms[0].shift.value=="Other")
	{
		showOther();
	}
	else
	{		
		hideOther();
	}
}

function hideOther()
{
	$(otherShift).setStyle({display: 'none'});
	$(emptyEntry).setStyle({display: 'block'});
}

function showOther()
{
	$(otherShift).setStyle({display: 'block'});
	$(emptyEntry).setStyle({display: 'none'});
}

