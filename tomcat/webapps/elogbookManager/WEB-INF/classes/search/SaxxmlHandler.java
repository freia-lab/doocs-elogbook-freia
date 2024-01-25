package search;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
//import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
//import java.io.FileInputStream;
import java.io.FileReader;
//import java.util.Iterator;
import java.util.HashMap;

public class SaxxmlHandler extends DefaultHandler {

  static char dirSep = System.getProperty("file.separator").charAt(0);

  private StringBuffer  	elementBuffer = new StringBuffer();/** A buffer for each XML element */
  private HashMap 		attributeMap = null;
  int				TextStart = 0;
  int				TitleStart = 0;
  public static Document 	doc = null;


/******************/
  public static String uid(File f) {
    // Append path and date into a string in such a way that lexicographic
    // sorting gives the same results as a walk of the file hierarchy.  Thus
    // null (\u0000) is used both to separate directory components and to
    // separate the path from the date.
         String sub = f.getPath();
	 int p = sub.indexOf("/data");
	 sub = f.getPath().substring(0, p);
	 p = sub.lastIndexOf('/');
	 sub = f.getPath().substring(p);
	 sub = sub.replace(dirSep, '\u0000');
	 sub = sub + "\u0000" + DateTools.timeToString(f.lastModified(), DateTools.Resolution.MILLISECOND);
	 //System.out.println("uid for " + f.getPath() + " is " + sub);
         //return f.getPath().replace(dirSep, '\u0000') + "\u0000" + DateTools.timeToString(f.lastModified(), DateTools.Resolution.MILLISECOND);
  	 return sub;
  }

/******************/
  public static String uid2url(String uid) {
    String url = uid.replace('\u0000', '/');	   // replace nulls with slashes
    return url.substring(0, url.lastIndexOf('/')); // remove date from end
  }


/******************/
  public Document getDocument(File file) throws IOException {

    InputStream   	ins;
    SAXParserFactory    spf = SAXParserFactory.newInstance();
    boolean		toDelete = false;

    try {

	     if(  file.getPath().endsWith(".xml") ||
                 (file.getPath().indexOf("shiftsum.xml") != -1)
	     ){

			BufferedReader 		in = new BufferedReader(new FileReader(file));
			StringBuffer 		xmlout = new StringBuffer();
			String 			iso = new String();
			String 			str = new String();

			xmlout.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
			xmlout.append("<entry>");

			int li = file.getPath().lastIndexOf('/');
			String sub = new String(file.getPath().substring(0, li));
			String sub2 = new String (sub.substring(sub.lastIndexOf('/')+1));
			xmlout.append("<dir>" + sub2 + "</dir>");

			sub2 = new String(sub);
			int ind = sub2.indexOf("/data");
                        if (ind ==-1)  ind = 0;
			//String docs = sub2.substring(ind + 6);
                        String docs = new String(sub2.substring(ind));
                        sub2 = sub2.substring(0, ind);
                        ind = sub2.lastIndexOf('/');
                        xmlout.append("<dirpath>" + sub.substring(ind) + "</dirpath>");
                        
			try{
				docs = docs.substring(6);
				Integer.parseInt(docs.substring(0,3));
				xmlout.append("<docs>no</docs>");
				//System.out.println(file.getPath() + " : is logbook entry");
			}catch(NumberFormatException e ){
				xmlout.append("<docs>yes</docs>");
				//System.out.println(file.getPath() + " : is document");
			}catch(IndexOutOfBoundsException e ){
				xmlout.append("<docs>yes</docs>");
				//System.out.println(file.getPath() + " : is document in /data?");
				//System.out.println(file.getPath() + " : is document");
			}

			if(file.getPath().indexOf("shiftsum") != -1){
		   		int shsfopen = 0;
		   		xmlout.append("<severity>NONE</severity>\n");

				while ((str = in.readLine()) != null){

					//str = str + " ";
				if(str.indexOf("<shiftsum>") != -1) continue;
                                    if(
			    		(str.indexOf("</shiftsum>")    == -1)  &&
			    		(str.indexOf("<coordinator>")  == -1)  &&
			    		(str.indexOf("<tcoordinator>") == -1)  &&
			    		(str.indexOf("<operator>")     == -1)  &&
			    		(str.indexOf("<other>")        == -1)  &&
			    		(str.indexOf("<goal>")         == -1)  &&
					(str.indexOf("<usersum>")      == -1)  &&
			    		(str.indexOf("<achievements>") == -1)  &&
			    		(str.indexOf("<difficulties>") == -1)  &&
					(str.indexOf("<photon_coord>") == -1)  &&
					(str.indexOf("<photon_oper>")  == -1)  &&
					(str.indexOf("<users>")        == -1)  &&
			    	   (
			     		(str.indexOf("<title>")    != -1)  	||
			     		(str.indexOf("<author>")   != -1)  	||
			     		(str.indexOf("<isodate>")  != -1)	||
			     		(str.indexOf("<time>")     != -1)	||
			     		(str.indexOf("<category>") != -1)	||
			     		(str.indexOf("<severity>") != -1)	||
					(str.indexOf("<keywords>") != -1)	||
			     		(str.indexOf("<metainfo>") != -1)
			    	   )
					){
			 			int is = str.indexOf("<isodate>");
		      			if(is != -1 && str.charAt(is+9) != '<'){
		 						is = is + 9;
								iso = "<luc_date>" + str.substring(is, is+4)  + str.substring(is+5, is+7) + str.substring(is+8, is+10) + "</luc_date>";
		      			}
						xmlout.append(str);
					}else{
						if(shsfopen == 0){
							xmlout.append("<text>");
							shsfopen = 1;
						}
						if(str.indexOf("<coordinator>") != -1){
							/*str.replace(str.charAt(str.indexOf("</coordinator>")) , '\0');*/
							String a = str.substring(str.indexOf("<coordinator>") + 13, str.indexOf("</coordinator>"));
							xmlout.append("\nCoordinator: " + a);
						}
						if(str.indexOf("<tcoordinator>") != -1){
							String a = str.substring(str.indexOf("<tcoordinator>") + 14, str.indexOf("</tcoordinator>"));
							xmlout.append("\nTcoordinator: " + a);
						}
						if(str.indexOf("<operator>") != -1){
							String a = str.substring(str.indexOf("<operator>") + 10, str.indexOf("</operator>"));
							xmlout.append("\nOperator: " + a);
						}
						if(str.indexOf("<other>") != -1){
							String a = str.substring(str.indexOf("<other>") + 7, str.indexOf("</other>"));
							xmlout.append("\nOther: " + a);
						}
						if(str.indexOf("<goal>") != -1){
							String a = str.substring(str.indexOf("<goal>") + 6, str.indexOf("</goal>"));
							xmlout.append("\nGoal: " + a);
						}
						if(str.indexOf("<usersum>") != -1){
							String a = str.substring(str.indexOf("<usersum>") + 9, str.indexOf("</usersum>"));
							xmlout.append("\nFEL users summary: " + a);
						}
						if(str.indexOf("<achievements>") != -1){
							String a = str.substring(str.indexOf("<achievements>") + 14, str.indexOf("</achievements>"));
							xmlout.append("\nAchievements: " + a);
						}
						if(str.indexOf("<difficulties>") != -1){
							String a = str.substring(str.indexOf("<difficulties>") + 14, str.indexOf("</difficulties>"));
							xmlout.append("\nDifficulties: " + a);
						}
						if(str.indexOf("<photon_coord>") != -1){
							String a = str.substring(str.indexOf("<photon_coord>") + 14, str.indexOf("</photon_coord>"));
							xmlout.append("\nPhoton coordinator: " + a);
						}
						if(str.indexOf("<photon_oper>") != -1){
							String a = str.substring(str.indexOf("<photon_oper>") + 13, str.indexOf("</photon_oper>"));
							xmlout.append("\nPhoton operator: " + a);
						}
						if(str.indexOf("<users>") != -1){
							String a = str.substring(str.indexOf("<users>") + 7, str.indexOf("</users>"));
							xmlout.append("\nUsers: " + a);
						}

						if(str.indexOf("</shiftsum>") != -1) xmlout.append("</text>\n");
					}
				}
				/*System.out.println("adding shiftsum: " + file.getPath() + ":\n " + xmlout.toString() +
				"\n*************\n\n");*/

			}else{
		   		while ((str = in.readLine()) != null){
					str = str + " ";
					if(str.toLowerCase().indexOf("<severity>delete</severity>") != -1){
						toDelete = true;
						break;
					}
					int is = str.indexOf("<isodate>");
		        		if(is != -1 && str.charAt(is+9) != '<'){
		 				is = is + 9;
						iso = "<luc_date>" + str.substring(is, is+4)  + str.substring(is+5, is+7) + str.substring(is+8, is+10) +"</luc_date>";
		      			}
		      			xmlout.append(str+"\n");
					str = "";
		   		}
			}

			in.close();
			if(!toDelete){
				if (iso != null)  xmlout.append(iso);
				xmlout.append("</entry>\n");

				ByteArrayInputStream 	bais = new ByteArrayInputStream(xmlout.toString().getBytes());
                                ins = bais;

				doc = new Document();

				//doc.add( Field.UnIndexed("path", file.getPath() ) );

				// Add the url as a field named "url".  Use an UnIndexed field, so
				// that the url is just stored with the document, but is not searchable.
				//doc.add(Field.UnIndexed("url", file.getPath().replace(dirSep, '/')));
				doc.add(new Field("url", file.getPath().replace(dirSep, '/'), Field.Store.YES, Field.Index.NO));

				// Add the last modified date of the file a field named "modified".  Use a
				// Keyword field, so that it's searchable, but so that no attempt is made
				// to tokenize the field into words.
				//doc.add(Field.Keyword("modified", DateField.timeToString(file.lastModified())));

				// Add the uid as a field, so that index can be incrementally maintained.
				// This field is not stored with document, it is indexed, but it is not
				// tokenized prior to indexing.
				//doc.add(new Field("uid", uid(file), false, true, false));
				doc.add(new Field("uid", uid(file), Field.Store.NO, Field.Index.NOT_ANALYZED));

				/*doc.add(Field.Keyword("uid2", uid(file).replace('\u0000', '-')) );*/

                            SAXParser parser = spf.newSAXParser();
                            parser.parse(ins, this);
			}/*else{
				System.out.println("SKIPPING file " + file.getPath() + " is marked to be deleted...");
			}*/
	  }

    } catch (IOException e) {
      System.err.println( "Cannot parse XML document " + file.getPath() + ": " + e.toString());
      return null;
    }
    catch (ParserConfigurationException e) {
      System.err.println( "Cannot parse XML document " + file.getPath() + ": " + e.toString());
      return null;
    }
    catch (SAXException e) {
      System.err.println( "Cannot parse XML document " + file.getPath() + ": " + e.toString());
      return null;
    }

    if(!toDelete)   return doc;
    else	    return null;
  }

/******************/
  public void startDocument() {
    //doc = new Document();  ==> do this before parsing in "getDocument", thus comment out
  }

/******************/
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException {

    if (qName.compareTo("text") == 0)  TextStart = 1;
	if (qName.compareTo("title") == 0)  TitleStart = 1;
    elementBuffer.setLength(0);

    if(attributeMap != null ) attributeMap.clear();
    if (atts.getLength() > 0) {
      attributeMap = new HashMap();
      for (int i = 0; i < atts.getLength(); i++) {
        attributeMap.put(atts.getQName(i), atts.getValue(i));
      }
    }
  }

 /******************/
  public void characters(char[] text, int start, int length) {

    if (TextStart != 1 && TitleStart != 1 ) elementBuffer.setLength(0);
    elementBuffer.append(text, start, length);

  }

 /******************/
  public void endElement(String uri, String localName, String qName) throws SAXException {

	if(qName.compareTo("entry") == 0 ){
		 elementBuffer.setLength(0);
		 return;
	}
	if( qName.compareTo("text") == 0 ) TextStart = 0;
	if( qName.compareTo("title") == 0 ) TitleStart = 0;

	if( (qName.compareTo("dir") 	== 0) 	||
	    /*(qName.compareTo("dirpath") == 0) 	||*/
	    (qName.compareTo("metainfo")== 0) 	||
	    (qName.compareTo("file") 	== 0)  	||
	    (qName.compareTo("link") 	== 0)	||
	    (qName.compareTo("isodate") == 0)   ||
	    (qName.compareTo("time") 	== 0)
	){
		doc.add(new Field(qName, elementBuffer.toString(), Field.Store.YES,  Field.Index.NO));
	}else{
		/*if ((qName.compareTo("category") == 0) ||
		    (qName.compareTo("severity") == 0) ||
		    (qName.compareTo("keywords") == 0)
		){
			doc.add(Field.Keyword(qName, elementBuffer.toString()));
		}else*/
      		doc.add(new Field(qName, elementBuffer.toString(), Field.Store.YES,  Field.Index.ANALYZED));
    }
  }


/******************/
  public static void main(String args[]) throws Exception {
    SaxxmlHandler 	handler = new SaxxmlHandler();

  }
}
