function replaceSubstring(inputString, fromString, toString) {
// Goes through the inputString and replaces every occurrence of fromString with toString
	var temp = inputString;
	if (fromString == "") {
	      	return inputString;
	}
	if (toString.indexOf(fromString) == -1) { 
		// If the string being replaced is not a part of the replacement string (normal situation)
                while (temp.indexOf(fromString) != -1) {
			var toTheLeft = temp.substring(0, temp.indexOf(fromString));
			var toTheRight = temp.substring(temp.indexOf(fromString)+fromString.length, temp.length);
			temp = toTheLeft + toString + toTheRight;
		}
	} else { 
		// String being replaced is part of replacement string (like "+" being replaced with "++")
		// - prevent an infinite loop
                var midStrings = new Array("~", "`", "_", "^", "#");
                var midStringLen = 1;
                var midString = "";
                // Find a string that doesn't exist in the inputString to be used
                // as an "inbetween" string
                while (midString == "") {
                	for (var i=0; i < midStrings.length; i++) {
				var tempMidString = "";
				for (var j=0; j < midStringLen; j++) { tempMidString += midStrings[i]; }
				if (fromString.indexOf(tempMidString) == -1) {
                                        midString = tempMidString;
                                        i = midStrings.length + 1;
				}
                	}
		} // Keep on going until we build an "inbetween" string that doesn't exist

                // Now go through and do two replaces - first, replace the "fromString" with the "inbetween" string
		while (temp.indexOf(fromString) != -1) {
                	var toTheLeft = temp.substring(0, temp.indexOf(fromString));
                	var toTheRight = temp.substring(temp.indexOf(fromString)+fromString.length, temp.length);
                	temp = toTheLeft + midString + toTheRight;
		}

		// Next, replace the "inbetween" string with the "toString"
		while (temp.indexOf(midString) != -1) {
			var toTheLeft = temp.substring(0, temp.indexOf(midString));
			var toTheRight = temp.substring(temp.indexOf(midString)+midString.length, temp.length);
			temp = toTheLeft + toString + toTheRight;
		}

	} // Ends the check to see if the string being replaced is part of the replacement string or not

	return temp; // Send the updated string back to the user
} // Ends the "replaceSubstring" function


function assign(elogadr){

	var str_phr = document.search_form.Search.value;

	var serv = elogadr;

	var ans;	
	var req = "";
	var str = "";
	var i;
	
	while(str_phr!= ""){
	  
	  if(str != ""){ 
		/*while ( (i=str.search(' ')) != -1){
	 		str = str.replace(' ', '_');
		}
		while ( (i=str.search('%')) != -1){
	 		str = replaceSubstring(str, "%", "percent25");
		}
		while ( (i=str.search('\'')) != -1){
	 		str = str.replace('\'', '_');
		}*/
		
		while ( (i=str.search(/\+/)) != -1){
	 		str = replaceSubstring(str, "+", "%2b");
		}
		while ( (i=str.search('<')) != -1){
	 		str = replaceSubstring(str, "<", "%3c");
		}
		while ( (i=str.search('#')) != -1){
	 		str = replaceSubstring(str, "#", "%23");
		}
		/*while ( (i=str.search('=')) != -1){
	 		str = replaceSubstring(str, "=", "%3d");
		}*/
		while ( (i=str.search('&')) != -1){
	 		//str = replaceSubstring(str, "&", "%26");
			str = replaceSubstring(str, "&", " ");
		}
		while ( (i=str.search('ä')) != -1){
	 		str = replaceSubstring(str, "ä", "%C3%A4");
		}
		while ( (i=str.search('Ä')) != -1){
	 		str = replaceSubstring(str, "Ä", "%C3%84");
		}
		while ( (i=str.search('å')) != -1){
	 		str = replaceSubstring(str, "å", "%C3%A5");
		}
		while ( (i=str.search('Å')) != -1){
	 		str = replaceSubstring(str, "Å", "%C3%85");
		}
		while ( (i=str.search('ö')) != -1){
	 		str = replaceSubstring(str, "ö", "%C3%B6");
		}
		while ( (i=str.search('Ö')) != -1){
	 		str = replaceSubstring(str, "Ö", "%C3%96");
		}
		/* while ( (i=str.search('ü')) != -1){
	 		str = replaceSubstring(str, "ü", "ue");
		}
		while ( (i=str.search('Ü')) != -1){
	 		str = replaceSubstring(str, "Ü", "Ue");
		}
		while ( (i=str.search('ß')) != -1){
	 		str = replaceSubstring(str, "ß", "ss");
		}
		*/
		if(str_phr != ""){ 
			ans = "&request_phr=" + str;
			str_phr = "";
		}
		
		req += ans;
	  }
	  
	  if(str_phr != ""){
	  	str = str_phr;
		continue;
	  }
	}

	ans = serv + req;
	//alert(serv);
	top.frames[3].location.href=ans;
	return;
}
