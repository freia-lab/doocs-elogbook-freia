/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import java.io.File;
import java.util.Date;
import java.util.Arrays;

public class IndexFiles {
  private static boolean 		deleting = false;	  	// true during deletion pass
  private static IndexReader 		reader;		  			// existing index
  private static IndexWriter 		writer;		  			// new index being built
  private static TermEnum 		uidIter;		      	// document id iterator
  private static boolean 		htm_fl = false;
  private static boolean 		xml_fl = false;
  private static boolean 		pdf_fl = false;
  private static boolean 		explicit_delete = false;
  private static String  		rootname = null;

	    
  public static void main(String[] argv) {
      int 		count = 0;
      File 		lock = null;
      File		root = null;
      String 		index = "index";
      boolean 		create = false;
 

      String usage = "IndexFiles [-create] [-index <index>] [-htm/-html] [-xml] [-pdf] <root_directory>";

      if (argv.length == 0) {
		System.err.println("Usage: " + usage);
		return;
      }

      for (int i = 0; i < argv.length; i++) {
		if (argv[i].equals("-index")) {		  // parse -index option
	  		index = argv[++i];
		} else if (argv[i].equals("-create")) {	  // parse -create option
	  		create = true;
		} else if (argv[i].equals("-delete")) {	  // parse -delete option
	  		explicit_delete = true;
		} else if (argv[i].contains("-htm")) {	  // parse -htm option
	  		htm_fl = true;
		} else if (argv[i].equals("-xml")) {	  // parse -xml option
	  		xml_fl = true;
		} else if (argv[i].equals("-pdf")) {	  // parse -pdf option
	  		pdf_fl = true;
		} else if (i != argv.length-1) {
	  		System.err.println("Usage: " + usage);
	  		return;
		} else{
	  		root = new File(argv[i]);
			rootname = argv[i];
		}
      }

      try{
       lock = new File(index, "lucene.lock");
	   while(true){
		  if(lock.exists()){
		    System.out.println(index + "/lucene.lock file exists, waiting...");
			Thread.sleep(30000);
			if(count == 10){
				System.out.println("Waiting for " + index + "/lucene.lock too long ( > 5 min ), exit! ");
				return;
			}
		  }else{
		    lock.createNewFile();
			lock.setReadOnly();
			lock.deleteOnExit();
			//System.out.println("Lock file created in " + lock.getPath() );
			break;
		  }
		  count++;
	   }		
      }catch(Exception e){
		System.out.println("problems reading/creating lock file..." + lock.getPath());
		return;
	  }

	try{
      Date start = new Date();

      if (!create) {				  // delete stale docs
		deleting = true;
		indexDocs(root, index, create);
      }
	  
	  writer = new IndexWriter(index, new StandardAnalyzer(), create);
	  //writer.maxFieldLength = 1000000;
	  writer.setMaxFieldLength(1000000);
	  
	  indexDocs(root, index, create);		  // add new docs

	  //System.out.println("Optimizing index...");
	  writer.optimize();
	  writer.close();

	  Date end = new Date();
  
      //System.out.print(end.getTime() - start.getTime());
      //System.out.println(" total milliseconds");		 	  

    } catch (Exception e) {
      System.out.println(" caught a " + e.getClass() +  "\n with message: " + e.getMessage());
    }
   
  }
  
  /* Walk directory hierarchy in uid order, while keeping uid iterator from
  /* existing index in sync.  Mismatches indicate one of: (a) old documents to
  /* be deleted; (b) unchanged documents, to be left alone; or (c) new
  /* documents, to be indexed.
   */

  private static void indexDocs(File file, String index, boolean create) throws Exception 
  {	
    if (!create) {				  	// incrementally update

      reader = IndexReader.open(index); 		// open existing index	  
      uidIter = reader.terms(new Term("uid", ""));	// init uid iterator

      indexDocs(file);

      if (deleting) {				  				// delete rest of stale docs
		while ( uidIter.term() != null && uidIter.term().field() == "uid" ){	
		   if (SaxxmlHandler.uid2url(uidIter.term().text()).startsWith(rootname)){
			   //System.out.println("deleting stale doc: " + SaxxmlHandler.uid2url(uidIter.term().text()));
	  		   //reader.delete(uidIter.term());
			   reader.deleteDocuments(uidIter.term());
		   }
	  	   uidIter.next();
		}
		deleting = false;
      }

      uidIter.close();				  	// close uid iterator
      reader.close();				  	// close existing index

    } else					  			// don't have exisiting
      indexDocs(file);
  }
  
    private static void indexDocs(File file) throws Exception {
    if (file.isDirectory()) {			    		// if a directory
    
      String[] files = file.list();		    		// list its files
      Arrays.sort(files);			    			// sort the files
      for (int i = 0; i < files.length; i++){	    // recursively index them
        try{
        	File newfile = new File(file, files[i]);
			indexDocs(newfile);
		}catch(Exception e){
			System.out.println("Exception in  indexDocs(" + file.getPath()+ "/" + files[i] + ") : " + e.toString());		
		}
      }

    } else if ( htm_fl && ( file.getPath().endsWith(".html") 	|| 
			file.getPath().endsWith(".htm")  	||
			file.getPath().endsWith(".txt") ) 	||  		// index .html .htm .txt files
	       		xml_fl && file.getPath().endsWith(".xml") ||  		// index .xml files
	       		pdf_fl && file.getPath().endsWith(".pdf")      		// index .pdf files
           ) {
        if (uidIter != null) {
		String uid = SaxxmlHandler.uid(file);	  // construct uid for doc		

	   	while(  uidIter.term() != null && uidIter.term().field() == "uid" &&
	         		uidIter.term().text().compareTo(uid) < 0) 
		{
			if (deleting && SaxxmlHandler.uid2url(uidIter.term().text()).startsWith(rootname)) {	// delete stale docs
				//System.out.println("deleting " + SaxxmlHandler.uid2url(uidIter.term().text()));
	    			//reader.delete(uidIter.term());
				reader.deleteDocuments(uidIter.term());
	  		}
	  		uidIter.next();
	    	}
	  
	    	if (uidIter.term() != null && uidIter.term().field() == "uid" &&
	    		uidIter.term().text().compareTo(uid) == 0 ) 
	    	{	
				if (explicit_delete){
					 reader.deleteDocuments(uidIter.term());				
				}else{			
					uidIter.next();			  // keep matching docs
				}
	    	} else if (!deleting) {
			
	    		// add new docs		  				
				Document doc = null; 
		
	  			if ( htm_fl && (file.getPath().endsWith(".html") ||
		    		 file.getPath().endsWith(".htm")  			 ||
		     		 file.getPath().endsWith(".txt"))
		   		) 
		        	doc = HTMLDocument.getDocument(file);
			
				if ( xml_fl && file.getPath().endsWith(".xml")	&& 
				 	 file.getPath().indexOf("init") == -1 	  	&& 
				 	 file.getPath().indexOf("mean") == -1		&& 
				 	 file.getPath().indexOf("oracle") == -1		&& 
				 	 file.getPath().indexOf("DAQ-status") == -1  
				){
					doc = (new SaxxmlHandler()).getDocument(file);
				}
				

				if ( pdf_fl && file.getPath().endsWith(".pdf") ){
					//doc = LucenePDFDocument.getDocument(file);
					doc = LucenePDFinXML.getDocument(file);
				}
							  			
	  			if(doc != null ){
					writer.addDocument(doc);
				}
	    	}
	  
        } else {					  // creating a new index
	
			Document doc = null;
		
	  		if ( htm_fl && (file.getPath().endsWith(".html") ||
		     	 file.getPath().endsWith(".htm")  		 ||
		     	 file.getPath().endsWith(".txt"))
		   	) 
				doc = HTMLDocument.getDocument(file);
		     
			if ( xml_fl && file.getPath().endsWith(".xml")		&& 
				 file.getPath().indexOf("init") == -1 	  	&& 
				 file.getPath().indexOf("mean") == -1		&& 
				 file.getPath().indexOf("oracle") == -1		&& 
				 file.getPath().indexOf("DAQ-status") == -1  
			){
				doc = (new SaxxmlHandler()).getDocument(file);
			}
			
					     
			if ( pdf_fl && file.getPath().endsWith(".pdf") ){
				//doc = LucenePDFDocument.getDocument(file);
				doc = LucenePDFinXML.getDocument(file);
			}
					
			if(doc != null ){ 
				writer.addDocument(doc);		  // add docs unconditionally
			}
        }
    }
  }
}
