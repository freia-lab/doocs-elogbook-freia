package search;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import java.io.File;
import java.util.Date;
import java.util.Arrays;
//import java.io.FileOutputStream;

/** Indexer for HTML files. */
public class IndexFiles {
  private IndexFiles() {}

  private static boolean deleting = false;	  // true during deletion pass
  private static IndexReader reader;		  // existing index
  private static IndexWriter writer;		  // new index being built
  private static TermEnum uidIter;		  // document id iterator

  private static boolean htm_fl = false;
  private static boolean xml_fl = false;
  private static boolean pdf_fl = false;
  private static boolean explicit_delete = false;
  private static String  rootname = null;
  private static String  indexname = null;
  private static String  rejected_dir = "rejected";
  private static File    rejected = null;
  private static boolean create = false;


  /** Indexer for HTML files.*/
  public static void main(String[] argv) {  
    
    try {
      File index = new File("index");
      indexname = "index";
      //File lockfile = new File("index/write.lock");
      
      
      File root = null;

      String usage = "IndexFiles [-create] [-index <index>] [-xml] [-html] [-pdf] <root_directory>";

      if (argv.length == 0) {
        System.err.println("Usage: " + usage);
        return;
      }

      for (int i = 0; i < argv.length; i++) {
        if (argv[i].equals("-index")) {
          int l = ++i;              // parse -index option
          index = new File(argv[l]);
          indexname = argv[l];
          //lockfile = new File(argv[l] + "/write.lock");
          /*rejected = new File(index.getParentFile(),"rejected");
          if (!rejected.exists()){
            boolean cr = (new File(index.getParentFile(),"rejected")).mkdir();
            if (cr) System.out.println("dir created: " + rejected.getAbsolutePath());
          }*/

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
        } else {
          root = new File(argv[i]);
          rootname = argv[i];
        }
      }

      if(root == null) {
        System.err.println("Specify directory to index");
        System.err.println("Usage: " + usage);
        return;
      }

      Date start = new Date();

     /* if (lockfile.exists()) {
          System.out.println("lockfile exists: " + lockfile.getAbsolutePath());
          if(System.currentTimeMillis() - lockfile.lastModified() > 3600000) { // if older than 60 min
            System.out.println("lockfile too old, abnormal exit? Trying to delete the lockfile...");
            try{
                lockfile.delete();
            }catch(Exception e){
                System.out.println("Couldn't delete the stale lockfile, exit ...");
                return;
            }
          }else {
              System.out.println("Index is locked , try later. Exiting without changes...");

              return;
          }
      }
      try{
            lockfile.createNewFile();
            lockfile.setReadOnly();
            lockfile.deleteOnExit();
      } catch (Exception e) {
            System.out.println("Couldn't create lockfile fot indexing, exit ...");

            return;
      }*/

      if (!create) {				  // delete stale docs
        System.out.println("Open IndexReader to delete stale docs...");
        deleting = true;
        indexDocs(root, index, create);
      }
      System.out.println("Creating an IndexWriter...");
      writer = new IndexWriter(FSDirectory.open(index), new StandardAnalyzer(Version.LUCENE_CURRENT), create,
                               new IndexWriter.MaxFieldLength(1000000));


      //writer.setMergeFactor(15);
      //writer.setRAMBufferSizeMB(48);

      indexDocs(root, index, create);		  // add new docs

      System.out.println("Optimizing index...");

      writer.optimize();
      writer.close();

      Date end = new Date();

      System.out.print(end.getTime() - start.getTime());
      System.out.println(" total milliseconds");

    } catch (LockObtainFailedException e){

        System.out.println("Exception getting lock: " + e.toString());
        System.out.print("try indexing later: ");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* Walk directory hierarchy in uid order, while keeping uid iterator from
  /* existing index in sync.  Mismatches indicate one of: (a) old documents to
  /* be deleted; (b) unchanged documents, to be left alone; or (c) new
  /* documents, to be indexed.
   */

  private static void indexDocs(File root, File index, boolean create)
       throws Exception {
    if (!create) {				  // incrementally update

      reader = IndexReader.open(FSDirectory.open(index), false);		  // open existing index
      uidIter = reader.terms(new Term("uid", "")); // init uid iterator

      indexDocs(root);

      if (deleting) {				  // delete rest of stale docs
        while (uidIter.term() != null && uidIter.term().field().equals("uid")) {
          //System.out.println("deleting " +   HTMLDocument.uid2url(uidIter.term().text()));
          //reader.deleteDocuments(uidIter.term());

          if (SaxxmlHandler.uid2url(uidIter.term().text()).startsWith(rootname)){
              System.out.println("deleting stale doc: " + SaxxmlHandler.uid2url(uidIter.term().text()));
              reader.deleteDocuments(uidIter.term());
          }
          uidIter.next();
        }
        deleting = false;
      }

      uidIter.close();				  // close uid iterator
      reader.close();				  // close existing index

    } else					  // don't have exisiting
      indexDocs(root);
  }

  private static void indexDocs(File file) throws Exception {
    if (file.isDirectory()) {			  // if a directory
      String[] files = file.list();		  // list its files
      if(files != null) Arrays.sort(files);	  // sort the files
      else{
          System.out.println("Cannot list: File is not drectory or other exception occured: " + file.getAbsolutePath());
          return;
      }
      
      for (int i = 0; i < files.length; i++)	  // recursively index them
        indexDocs(new File(file, files[i]));

    } else if ( htm_fl && (file.getPath().endsWith(".html") ||
                file.getPath().endsWith(".htm")             ||
                file.getPath().endsWith(".txt"))            ||  // index .html, .htm, .txt files
                xml_fl && file.getPath().endsWith(".xml")   ||  // index .xml files
                pdf_fl && file.getPath().endsWith(".pdf")  )      	// index .pdf files
    { // index .txt files

      if (uidIter != null) {
        //String uid = HTMLDocument.uid(file);	  // construct uid for doc
	String uid = SaxxmlHandler.uid(file);	  // construct uid for doc

        while (uidIter.term() != null && uidIter.term().field().equals("uid") &&
            uidIter.term().text().compareTo(uid) < 0) {
          if (deleting && SaxxmlHandler.uid2url(uidIter.term().text()).startsWith(rootname)) {			  // delete stale docs
            //System.out.println("deleting " + HTMLDocument.uid2url(uidIter.term().text()));
            System.out.println("deleting " + SaxxmlHandler.uid2url(uidIter.term().text()));
            reader.deleteDocuments(uidIter.term());
          }
          uidIter.next();
        }

        if (uidIter.term() != null && uidIter.term().field().equals("uid") &&
            uidIter.term().text().compareTo(uid) == 0) {

            if (explicit_delete){
		reader.deleteDocuments(uidIter.term());
            }else
                uidIter.next();	// keep matching docs

        } else if (!deleting) {			  // add new docs

          //Document doc = HTMLDocument.Document(file);\
          Document doc = null;

	 /*if ( htm_fl && (file.getPath().endsWith(".html") ||
		file.getPath().endsWith(".htm")  	    ||
                file.getPath().endsWith(".txt")) )
	      doc = HTMLDocument.Document(file);*/

		if ( xml_fl && file.getPath().endsWith(".xml")	    &&
		     file.getPath().indexOf("init") == -1 	    &&
                     file.getPath().indexOf("mean") == -1	    &&
		     file.getPath().indexOf("oracle") == -1	    &&
		     file.getPath().indexOf("DAQ-status") == -1  )
                  doc = (new SaxxmlHandler()).getDocument(file);



		/*if ( pdf_fl && file.getPath().endsWith(".pdf") ){
			//doc = LucenePDFDocument.getDocument(file);
			doc = LucenePDFinXML.getDocument(file);
		}*/

	  	if(doc != null ){
                    //System.out.println("adding " + doc.get("dirpath"));
                    writer.addDocument(doc);
		}
  
        }
      } else {					  // creating a new index

        //Document doc = HTMLDocument.Document(file);
        //System.out.println("adding " + doc.get("path"));
        //writer.addDocument(doc);		  // add docs unconditionally

	Document doc = null;

	/*if ( htm_fl && (file.getPath().endsWith(".html") ||
		     	 file.getPath().endsWith(".htm")  		 ||
		     	 file.getPath().endsWith(".txt"))  )
            doc = HTMLDocument.Document(file);*/

	if ( xml_fl && file.getPath().endsWith(".xml")		&&
            file.getPath().indexOf("init") == -1 	  	&&
            file.getPath().indexOf("mean") == -1		&&
            file.getPath().indexOf("oracle") == -1		&&
            file.getPath().indexOf("DAQ-status") == -1  )
	  doc = (new SaxxmlHandler()).getDocument(file);


	/*if ( pdf_fl && file.getPath().endsWith(".pdf") ){
		//doc = LucenePDFDocument.getDocument(file);
		doc = LucenePDFinXML.getDocument(file);
	}*/

	if(doc != null )    writer.addDocument(doc); // add docs unconditionally

      }
    }
  }
}
