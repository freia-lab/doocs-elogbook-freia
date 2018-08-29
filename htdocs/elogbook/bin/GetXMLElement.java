/*
 *
 *
 *
 */

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.*;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * This program prints out the value for a given XML
 * element.
 *
 * @author Raimund Kammering, DESY
 *
 * @version $Id: GetXMLElement.java
 */
public class GetXMLElement {

    /** Prints the specified elements in the given document. */
    public static void print(PrintWriter out, Document document,
                             String elementName, String attributeName) {

        // get elements that match
        NodeList elements = document.getElementsByTagName(elementName);

        // is there anything to do?
        if (elements == null) {
            return;
        }

        // print all elements
        if (attributeName == null) {
            int elementCount = elements.getLength();
            for (int i = 0; i < elementCount; i++) {
                Element element = (Element)elements.item(i);
                if (element.hasChildNodes()) {
                    print(out, element, element.getAttributes());
                }
            }
        }

        // print elements with given attribute name
        else {
            int elementCount = elements.getLength();
            for (int i = 0; i < elementCount; i++) {
                Element      element    = (Element)elements.item(i);
                NamedNodeMap attributes = element.getAttributes();
                if (attributes.getNamedItem(attributeName) != null) {
                    print(out, element, attributes);
                }
            }
        }

    }

    /** Prints the specified element. */
    protected static void print(PrintWriter out,
                                Element element, NamedNodeMap attributes) {

	//        out.print(element.getNodeName());
	Node child = element.getFirstChild();
	out.print(child.getNodeValue());
	out.println();
	out.flush();
    }

    /** Main program entry point. */
    public static void main(String argv[]) {

        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }

        // variables
        PrintWriter out = new PrintWriter(System.out);
	DOMParser parser = null;
        String elementName = "*";
        String attributeName = null;

        // process arguments
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                String option = arg.substring(1);
                if (option.equals("e")) {
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -e option.");
                    }
		    if (option.equals("h")) {
			printUsage();
			continue;
		    }
                    elementName = argv[i];
                    continue;
                }
            }

	    // create parser
	    try {
		parser = new DOMParser();
 	    }
 	    catch (Exception e) {
 		System.err.println("error: Unable to instantiate parser");
 		continue;
 	    }

            // parse file
            try {
                parser.parse(arg);
		Document document = parser.getDocument();
                GetXMLElement.print(out, document, elementName, attributeName);
            }
            catch (SAXParseException e) {
                // ignore
            }
            catch (Exception e) {
                System.err.println("error: Parse error occurred - "+e.getMessage());
                if (e instanceof SAXException) {
                    e = ((SAXException)e).getException();
                }
                e.printStackTrace(System.err);
            }
        }

    }

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java GetXMLElement (options) uri ...");
        System.err.println();

        System.err.println("options:");
        System.err.println("  -e name  Specify element name for search. Prints element content.");
        System.err.println("  -h       This help screen.");
        System.err.println();
    }

}
