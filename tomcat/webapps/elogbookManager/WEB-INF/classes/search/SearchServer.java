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
import org.apache.lucene.index.*;
//import org.apache.lucene.store.*;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Vector;
import helper.LogHelper;
import settings.Settings;

public class SearchServer implements Runnable {
    private static Thread               worker = null;
    
    private static Vector <Fragments>   fragments;
    private static Vector <Writers>     writerVect;

    private static final Object         lck = new Object();
    private static boolean              thrMustStop = false;
    private static String               docroot = "/export/web/htdocs";
    private static int                  DEBUG_LEVEL = 1;
   
    private static SearchServer       _instance = null;

    private SearchServer() {
        writerVect = new Vector();
        fragments = new Vector();
    }
       
    public static SearchServer getInstance(){
        if (_instance == null) {
            if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("starting a Search Server instance at " + (new Date()).toString());
            _instance = new SearchServer();
            //numRequests = 0;
            //requests = new Vector();
        }
        return _instance;
    }
    
    public void setDebug(String level){
        if(level == null || level.equals("")) return;
        if(level.toUpperCase().equals("NODEBUG"))   DEBUG_LEVEL = 0;
        if(level.toUpperCase().equals("NORMAL"))    DEBUG_LEVEL = 1;
        if(level.toUpperCase().equals("HIGH"))      DEBUG_LEVEL = 2;
        if(level.toUpperCase().equals("HIGHEST"))   DEBUG_LEVEL = 3;
        return;
    }
    
    public boolean isAlive(){
        if(worker != null && worker.isAlive())
            return true;
        return false;
    }
    
    public void startServer(){
           if(_instance == null){
               _instance = new SearchServer();
           }
           worker = new Thread(_instance);
           worker.start();
    }
    
    public void setParam(String param){
        docroot = param;
    }

    /*public void setRequest(String req){
        if(req != null) requests.add(req);
    }*/

    //public void getRequest(String req){
    public void setRequest(String req){
        String crt_fl = getParameter("create", req);
        String logname = getParameter("logbook", req); 
        String delete =  getParameter("delete", req); 
        String flag = getParameter("flag", req); 
        String datapath = getParameter("path", req);  
        String stopmThr = getParameter("stop", req);
        boolean create = false, explicit_delete = false;
        
        if(logname == null && datapath ==null) {
            if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Illegal request to SearchServer, skipping : " + req);
            return;
        }

        if(crt_fl != null && (crt_fl.equals("true") || crt_fl.equals("yes")))
            create = true;
        
        if(delete != null && (delete.equals("true") || delete.equals("yes")))
            explicit_delete = true;

        try{
            newReq(logname, datapath, create, explicit_delete);
        }catch(Exception e){

        }
    }
    
    public void getInfo(){
        if(_instance != null && worker != null && worker.isAlive())
             LogHelper.getInstance().log("getInfo(): Search stared , worker active");
        else LogHelper.getInstance().log("getInfo(): Search is stopped or not started yet...");
        return;
    }

    public void ThreadMustStop(){
         thrMustStop = true;
         return;
    }

    public class Writers {
        String           logbookname = "";
        IndexWriter      iwriter;
        //IndexReader      ireader;
        File             index;

        public Writers (String logname , boolean create){

            if(logname.contains("/")){
                logname = logname.replace("/", " ");
                logname = logname.trim();
            }

            logbookname = logname;

            try{
                   index = new File(docroot + "/" + logbookname + "/work/index");

                   if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("New writer on index is " + docroot + "/" + logbookname + "/work/index");

                   iwriter = new IndexWriter (FSDirectory.open(index), new StandardAnalyzer(Version.LUCENE_CURRENT),
                           create, new IndexWriter.MaxFieldLength(1000000));
                   
                   iwriter.setRAMBufferSizeMB(48);
                  // ireader = IndexReader.open(FSDirectory.open(index), true);

                   if (DEBUG_LEVEL >= 3){
                       if(iwriter !=null) LogHelper.getInstance().log("new IWRITER: " + iwriter.toString());
                       else LogHelper.getInstance().log("Search Server : new IWRITER is null !!! Failed to create !");
                   }
                   
                   //if(ireader !=null ) LogHelper.getInstance().log("new IREADER: " + ireader.toString());
                   //else LogHelper.getInstance().log("new IREADER is null !!!: ");
                   //ireader = iwriter.getReader();

            } catch (Exception e){
                  try{
                       if(iwriter!=null){
                           if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: error on creating writer instance for " + logbookname );
                           iwriter.close();
                       }
                       //if(iwriter!=null) ireader.close();

                  }catch(AlreadyClosedException ee){       
                      if (DEBUG_LEVEL >= 1)  LogHelper.getInstance().log("Search Server: iwriter instance is already closed: " + logbookname);

                  }catch(LockObtainFailedException ee){
                       if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: LockObtainFailedException occured in Writers constructor "+ logbookname);

                  }catch (CorruptIndexException ee){
                       if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: couldn't normally close writers in Writers constructor : "+ e.toString());

                  }catch(IOException eee){
                       if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: couldn't normally close writers in Writers constructor: "+ e.toString());
                  }
                  //iwriter = null;
                  //ireader = null;
                  logbookname = "";
                  //index = null;
            }
       }

       private void write_fragment(File root, boolean explicit_delete){
           String rootname = root.getAbsolutePath();
           rootname = rootname.substring(rootname.indexOf(logbookname));
           String act = "add";
           rootname = rootname.replace('/', '=');

           if(explicit_delete) act = "delete";
           rootname += "-" + act;

           File f = new File(Settings.FRAGMENTS + "/" + rootname); // new File("/home/anna/test/fragments/" + fname);
           try {
                   if(!f.exists()) f.createNewFile();
           } catch (IOException ioe){
                   if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: couldn't create new fragments file " + Settings.FRAGMENTS  +rootname + ", "
                           + ioe.toString());
           }
       }

       public boolean startIndexing(File root, boolean create, boolean explicit_delete) throws Exception{
           if(iwriter == null){
               if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: iwriter instance is null for " + root.getAbsolutePath());
               write_fragment(root, explicit_delete);
               return false;
           }
           
           boolean error = false;
           boolean deleting = false;
           String logtxt = "";

           try{
                if(!create){
                    deleting = true;
                    if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: create = false, running incrementally index update for " + root.getAbsolutePath());
                    indexDocs(root,  create, deleting, explicit_delete);
                    deleting = false;
                }
                
                if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: adding new docs to index: " + root.getAbsolutePath());
                indexDocs(root, create, deleting, explicit_delete);  // add new docs

                if(iwriter != null) iwriter.commit();
                
           } catch (OutOfMemoryError e){
                    logtxt = "Search Server: OutOfMemoryError occured for " + root.getAbsolutePath();
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    /*try{
                        iwriter.close();
                    }catch(Exception ee){
                    }
                    iwriter = null;*/
                    //ThreadMustStop();
                    error = true;
          }catch (AlreadyClosedException e){
                    logtxt = "Search Server: AlreadyClosedException occured for " + root.getAbsolutePath();
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    error = true;
                    write_fragment(root, explicit_delete);

         }catch (CorruptIndexException e){
                    logtxt = "Search Server: CorruptIndexException occured for " + root.getAbsolutePath();
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    error = true;
                    write_fragment(root, explicit_delete);
          } catch (LockObtainFailedException e){
                    logtxt = "Search Server: LockObtainFailedException occured for "+ root.getAbsolutePath();
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    error = true;
                    write_fragment(root, explicit_delete);
          }  catch (IllegalStateException e){
                    logtxt = "Search Server: IllegalStateException occured for " + root.getAbsolutePath();
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    error = true;
                    write_fragment(root, explicit_delete);
          }catch (Exception e){
                if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: exception during startIndexing: " + e.toString());
                error = true; 
                write_fragment(root, explicit_delete);
          }
           
          return error;

       }

       private void indexDocs(File root, boolean create, boolean deleting, boolean explicit_delete) throws Exception {
            TermEnum    uidIter = null;
            // IndexReader reader;
            
            String  rootname = root.getAbsolutePath();
            String sub = rootname;
            int p = rootname.indexOf("/data");
            if(p != -1){
                p = (rootname.substring(0,p)).lastIndexOf("/");
                if(p!= -1) sub = rootname.substring(p);
            }
    
            if(!sub.equals(rootname)) rootname = sub;
            
            if (!create ) {    // incrementally update
                if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: !----Try opening a reader on index: " + index.getAbsolutePath());
                IndexReader ireader = IndexReader.open(FSDirectory.open(index), true);
                uidIter = ireader.terms(new Term("uid", ""));  // init uid iterator

                uidIter = indexDocs(root, rootname, uidIter, deleting, explicit_delete);
      
                if (deleting && uidIter != null) {				  // delete rest of stale docs
                    while (uidIter.term() != null && uidIter.term().field().equals("uid")) {
                        if (SaxxmlHandler.uid2url(uidIter.term().text()).startsWith(rootname)){
                            if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: deleting stale doc: " + SaxxmlHandler.uid2url(uidIter.term().text()));
                            if(iwriter != null  && !thrMustStop) iwriter.deleteDocuments(uidIter.term());
                        }
                        uidIter.next();
                    }
                    deleting = false;
                }

                uidIter.close();
                ireader.close();
                if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: !---- Closed the reader and uidIterator on index: " + index.getAbsolutePath());
               
            } else  // don't have exisiting index
                indexDocs(root, rootname, null,deleting, explicit_delete);
        }

        private TermEnum indexDocs(File file, String rootname, TermEnum uidIter, boolean deleting, boolean explicit_delete)
                throws Exception {

            if (file.isDirectory()) {			  // if a directory
                String[] files = file.list();		  // list its files
                if(files != null) Arrays.sort(files);	  // sort the files
                else{
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Cannot list: File is not drectory or other exception occured: " + file.getAbsolutePath());
                    return uidIter;
                }
      
                for (int i = 0; i < files.length; i++)	  // recursively index them
                    indexDocs(new File(file, files[i]), rootname, uidIter,deleting, explicit_delete);

            } else if ( file.getPath().endsWith(".xml") 	    &&
                         file.getPath().indexOf("init") == -1 	    &&
                         file.getPath().indexOf("mean") == -1	    &&
                         file.getPath().indexOf("oracle") == -1	    &&
                         file.getPath().indexOf("DAQ-status") == -1  ) {

                if (uidIter != null) {
                    String uid = SaxxmlHandler.uid(file);	  // construct uid for doc
                    //if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: compare existing uids with current file uid: " + file.getAbsolutePath());//SaxxmlHandler.uid2url(uidIter.term().text()));
                    
                    while (uidIter.term() != null && uidIter.term().field().equals("uid") &&
                        uidIter.term().text().compareTo(uid) < 0) {
                        
                        //if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: current term is: " + SaxxmlHandler.uid2url(uidIter.term().text()));

                        if (deleting && SaxxmlHandler.uid2url(uidIter.term().text()).startsWith(rootname)) {	// delete stale docs
                           if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: try to delete " + SaxxmlHandler.uid2url(uidIter.term().text()));
                            if(iwriter != null  && !thrMustStop){
                                if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: writer in not null, deleting...");
                                iwriter.deleteDocuments(uidIter.term());
                            }
                        }
                        uidIter.next();
                    }

                    if (uidIter.term() != null && uidIter.term().field().equals("uid") &&
                                            uidIter.term().text().compareTo(uid) == 0    ) {

                        //if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: uid and term are equal,i.e. file is already indexed, current term is: " + SaxxmlHandler.uid2url(uidIter.term().text()));

                        if (explicit_delete){
                            if(iwriter != null && !thrMustStop){
                                if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: deleting file " + uidIter.term().text());
                                iwriter.deleteDocuments(uidIter.term());
                            }
                        }else
                            uidIter.next();         // keep matching docs

                    } else if (!deleting) {		// add new docs

                        Document doc = (new SaxxmlHandler()).getDocument(file);

                        if(doc != null && !thrMustStop && iwriter !=  null){
                            //if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: adding document: " + file.getAbsolutePath());
                            iwriter.addDocument(doc);
                        }else{
                            if (DEBUG_LEVEL >= 2){
                                LogHelper.getInstance().log("Search Server: could not add document " + file.getAbsolutePath() +", reason follows:");
                                if (doc == null)        LogHelper.getInstance().log("doc is null");
                                if(thrMustStop)         LogHelper.getInstance().log("thrMustStop is true");
                                if(iwriter == null)     LogHelper.getInstance().log("iwriter is null");
                            }
                        }
                    }
                } else {
                    // creating a new index
                    Document doc = (new SaxxmlHandler()).getDocument(file);

                    if(doc != null && !thrMustStop && iwriter != null){
                        if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: Adding document: " + file.getAbsolutePath());
                        iwriter.addDocument(doc);
                    }else{  // add docs unconditionally
                        if (DEBUG_LEVEL >= 3) {
                            LogHelper.getInstance().log("Search Server: could not add document " + file.getAbsolutePath() +", reason :");
                            if (doc == null)        LogHelper.getInstance().log("doc is null");
                            if(thrMustStop)         LogHelper.getInstance().log("thrMustStop is true");
                            if(iwriter == null)     LogHelper.getInstance().log("iwriter is null");
                        }
                    }
                }
            }
            return uidIter;
        }
    }   
    
    public class Fragments{
        boolean     explicit_delete = false;
        boolean     create = false;
        String      logbookname = "";
        String      datapath = "";
        String      flag = "";
        String      action = "";
        String      filename = "";

        // filename is potentially a fragment name in /var/tmp/elogbook/fragments,
        // name = DOOCSelog, ex.
        // path = /data/...
        public Fragments (String name, String path, String fl, boolean cr, String act){
            logbookname = name;
            datapath = path;
            flag = fl;
            create = cr;
            action = act;

            if(!path.startsWith("/")) filename = name + "/" + path;
            else
                filename = name + path;

            File fpath = new  File(docroot + filename);

            if(fpath.exists() && fpath.isDirectory()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    filename = filename + "=" + sdf.format(new Date()) + "-00.dir";
            }

            filename = filename.replace('/', '=');

            filename += "-" + action;

            if(action.equals("delete") || action.startsWith("delete")) explicit_delete = true;
        }
   }

   public Writers addNewWriter(String logname, boolean create) throws Exception{
       Writers mw = null;
       try{
            if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: try creating new Writer object for " +
                                logname  + " at " + (new Date()).toString());
            mw = new Writers(logname, create);
            synchronized(lck){
                if(mw != null && mw.iwriter != null){
                    writerVect.add(mw);
                    if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: new Writer added: " + mw.logbookname+ ", class: " + mw.toString());
                }
                else{
                    mw = null;
                    if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: couldn't add new  Writer for: " + mw.logbookname );
                }
            }
       }catch(Exception e){
           if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: couldn't create new Writer object for " +
                                logname  + " at " + (new Date()).toString());
           mw = null;
       }
       return mw;
   }
   
   public void newReq(String logname, String datapath, boolean create, boolean explicit_delete) throws Exception {

       if(!thrMustStop){

        String logtxt = "";
        try{
              if(logname.equals("")) return;
              Writers mywriter = getWriterByName(logname);
              if(mywriter == null){
                  if (DEBUG_LEVEL >= 3)  LogHelper.getInstance().log("Search Server: no Writer found for " + logname + ", create new...");
                  mywriter = addNewWriter(logname, create);
              }

              if(!thrMustStop && mywriter != null){
                  File root = new File(docroot + "/" + logname +"/"+ datapath);
                  if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: got new request --> indexing path: " + root.getAbsolutePath() + " deleting ? ==> " + Boolean.toString(explicit_delete));
                  boolean end = mywriter.startIndexing(root, false, explicit_delete);
                  if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server:  <---new request done indexing " + root.getAbsolutePath() );
              }

          } catch (OutOfMemoryError e){
                    logtxt = "Search Server: OutOfMemoryError occured for " +logname + datapath;
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    thrMustStop = true;
                    
          } catch (IllegalStateException e){
                    logtxt = "Search Server: IllegalStateException occured for " + logname + datapath;
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());

          }catch (Exception e){
                if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: exception during newReq handling: " + e.toString());
          }
      }
    }

    private String getParameter(String paraName, String request)
    {
        String arr[] = request.replaceAll("&","=").split("=");
        for (int i = 0; i < arr.length; i++)
        {
            if (i%2==0)
            {
                if (arr[i].equals(paraName))
                {
                    if ((i+1)<arr.length)
                    {
                        return arr[i+1];
                    }
                    else
                    {
                        return "";
                    }

                }
            }
        }
        return null;
    }

    private Writers getWriterByName(String name){
        int i;
        String mname = "";

        synchronized (lck){
          if(name.contains("/")){
                mname = name.replace("/", " ");
                mname = mname.trim();
          }else if(name.equals("")){
              return null;
          }else
                mname = name;

          if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: Find logbookname in Vector: " + mname +" ... ");
          for(i = 0; i < writerVect.size();i++){
            if(writerVect.get(i).logbookname.equals(mname)){
                if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: found " + mname + " at " + i);
                if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: writerVect.size() = " + writerVect.size());
                break;
            }else {
               if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: writerVect["+i+"] = " +  writerVect.get(i).logbookname);
            }              
          }
          if (i >= writerVect.size()) return null;
          return writerVect.get(i);
        }
    }

   private int check_fragments(){   
     String [] fnames = {""};

     try{
         String  current_fragname = "";
         File fragm_dir = new File(Settings.FRAGMENTS); 
         if(fragm_dir.exists() && fragm_dir.isDirectory()){

            fnames = fragm_dir.list();
            Arrays.sort(fnames);

            if (fnames.length != 0){
                if (DEBUG_LEVEL >= 2){
                    LogHelper.getInstance().log("");
                    LogHelper.getInstance().log("Search Server: " +  fnames.length + " fragments found");
                }
                for(int i = 0; i < fnames.length ; i++){
                    current_fragname = fnames[i];
                    if (DEBUG_LEVEL >= 3 ) LogHelper.getInstance().log(i + " : " + fnames[i]);
                    boolean create = false;
                    String  logname= "", datapath = "", flag = "xml", action = "";
                    int size = (int) current_fragname.length();

                    if (size != 0){
                        String str = current_fragname.replace('=', '/');

                        if(str.endsWith("-add") || str.endsWith("-delete"))
                              str = str.substring(0, str.lastIndexOf("-"));
                        else  continue;

                        if(str.endsWith(".xml")) flag = "xml";
                        else if(str.endsWith(".pdf")) flag = "pdf";
                        else if(str.endsWith(".html") || str.endsWith(".htm")) flag = "html";

                        if(str.endsWith(".dir")) str = str.substring(0, str.lastIndexOf("/"));

                        if(str.indexOf("/") != -1){
                            datapath = str.substring(str.indexOf("/"));
                            logname = str.substring(0, str.indexOf("/"));
                        }

                        int p = fnames[i].lastIndexOf('-');
                        if(p != -1) action = fnames[i].substring(p + 1);
                        
                        if(!action.startsWith("add") && !action.startsWith("delete")) {
                            action = "add";
                        }

                        if(!logname.equals("") && !datapath.equals("")) {
                            Fragments fr = new Fragments(logname, datapath, flag, create, action);
                            fragments.add(fr);
                        }
                    }
                }
            }
         }else{
             return fnames.length;
         }
      }catch (Exception ioe){
            if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Exception occured during fragments check: " +  ioe.toString());
            //return false;
      }

      //return true;
      return fnames.length;
    }

    public boolean handle_fragments(){
        int size = 0;
        int i = 0;
        try{
           size = fragments.size();

           Fragments f1, f2;
           for (i = 0 ; size > 0 && i <= (size - 1); i++){

               if(size == 1){
                    f1 = fragments.get(0);
                    newReq(f1.logbookname, f1.datapath, f1.create, f1.explicit_delete);
                    
                    File f = new File(Settings.FRAGMENTS + "/" + fragments.get(0).filename);
                    if(f.exists()) f.delete();
                    fragments.remove(0);
                    return true;
                }

                f2 = fragments.get(i + 1);
                f1 = fragments.get(i);

                String s1, s2;
                s1 = f1.logbookname + f1.datapath; // datapath should start with "/"
                s2 = f2.logbookname + f2.datapath;
                
                
                int p = s1.lastIndexOf("-");
                if(p == -1)  p = s1.length();

                if(s2.startsWith(s1.substring(0, p))){

                    File f = new File(Settings.FRAGMENTS + "/" + f1.filename);
                    if(f.exists()) f.delete();
                    fragments.remove(i);
                    i--;
                    size = fragments.size();
                    continue;

                } else {
                    newReq(f1.logbookname, f1.datapath, f1.create, f1.explicit_delete);

                    File f = new File(Settings.FRAGMENTS +"/" + f1.filename);
                    if(f.exists()) f.delete();
                    fragments.remove(i);
                    i--;
                    size = fragments.size();
                }
             }

        }catch(OutOfMemoryError ome){
            thrMustStop = true;
            return false;
        } catch (Exception e){
            if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Exception during fragments handling: " +  e.toString());
            fragments.remove(i);
            return false;
        }
        return true;
    }

    public boolean check_optimize_time(){
        String[] times = {"6:00", "14:00", "22:00"}; //Settings.OPTIMIZE_TIMES;
        String next = "";
        Date newDate = new Date();

        Date plusMinute = newDate;
        plusMinute.setTime(plusMinute.getTime() + 60000);

        for(int i = 0; i < 2 ; i++){
            next = times[i];

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String now = sdf.format(newDate);
            next = sdf.format(plusMinute);
            if( now.startsWith(times[i]) || next.startsWith(times[i])){
                return true;
            }
        }
        return false;
    }

    public void start_optimization(){
        int size = 0;
        String logtxt = "";
        if (!writerVect.isEmpty()){
            size = writerVect.size();
            for (int i = 0; i < size; i++){
                try{
                    //if(writerVect.get(i).iwriter.){
                        try{
                                if(!writerVect.get(i).iwriter.getReader().isOptimized())
                                    writerVect.get(i).iwriter.optimize();
                                if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: optimizing index " +   writerVect.get(i).logbookname);
                                writerVect.get(i).iwriter.commit();
                        }catch(OutOfMemoryError ee) {
                                writerVect.get(i).iwriter.close();
                        }
                    //}
                } catch (OutOfMemoryError e){
                    logtxt = "Search Server: OutOfMemoryError occured on start_optimization ";
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                    thrMustStop = true;
                    
                } catch (IllegalStateException e){
                    logtxt = "Search Server: IllegalStateException occured for ";
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log(logtxt + " : " + e.toString());
                     
                }catch(CorruptIndexException e){
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: exception occured: " + e.toString());

                }catch(IOException e){
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: exception occured: " + e.toString());

                }catch (Exception e){
                    if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: exception during newReq handling: " + e.toString());
                  
                }
            }
        }
    }
    
    @Override
    public void run(){
       int    fr_num = 0;

       while(!thrMustStop){
            fr_num = check_fragments();
            try{
                handle_fragments();
                
                if(!thrMustStop && check_optimize_time()){
                    start_optimization();
                }
                if(!thrMustStop) Thread.sleep(60000);
            }catch(InterruptedException e){
                if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log( "Search Server: InterruptedException in run: " + e.toString());
                break;
            }catch(Exception e){
                if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log( "Search Server: Exception in run: " + e.toString());
                break;
            } catch (OutOfMemoryError e){
                if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log( "Search Server: Exception in run: " + e.toString());
                break;     
            }
       }

       try{
           closeWriters();
           closeFragments();
       } catch (Exception e){
       }finally{
           if(writerVect !=null && !writerVect.isEmpty()){
               writerVect.clear();
               writerVect = null;
           }
           if(fragments != null && !fragments.isEmpty()){
               fragments.clear();
               fragments = null;
           }
       }
       
       if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: closed Writers, stopping the Main Search thread at: " + (new Date()).toString());
       return;
    }

    public void stopServer(){
           _instance = null;
           if(worker.isAlive()) worker.interrupt();
           if(worker != null) worker = null;
    }

    public void closeFragments(){
        int size = fragments.size();
        for(int i = 0; i < size;){
            fragments.remove(i);
            size = fragments.size();
        }
    }

    public void closeWriters(){
        String str = "";
        boolean error = false /*, waitforMerges = false*/;
        
           try{ 
             if (DEBUG_LEVEL >= 2) LogHelper.getInstance().log("Search Server: closing writers:"+writerVect.size());
             synchronized(lck){    
                for(int i = 0; i < writerVect.size() ; i++){
                     //if(numRequests != 0) waitforMerges = true;
                     Writers w = writerVect.get(i);
                     if(w != null){
                        if(w.iwriter != null) writerVect.get(i).iwriter.close();
                     } 
                }
             }

            }catch(CorruptIndexException e){
                  if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Could not close all writers: " + e.toString());
                  error = true;
            }catch(IOException e){
                  if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Could not close all writers: " + e.toString());
                  error = true;
            }catch(OutOfMemoryError e){
                  if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Could not close all writers: " + e.toString());
                  error = true;
            }catch (NullPointerException ne){
                  if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: NullPointer caught: " + ne.toString());
            }
            catch (Exception e){
                  if (DEBUG_LEVEL >= 1) LogHelper.getInstance().log("Search Server: Could not close all writers: " + e.toString());
            }
        
            if (DEBUG_LEVEL >= 3) LogHelper.getInstance().log("Search Server: try to remove Writer vector elements: " + writerVect.size());

            synchronized(lck){
                int size = writerVect.size();
                for(int i = 0; i < size ;){
                     writerVect.remove(i);
                     size = writerVect.size();
                }
            }
        //}
        //if(error) return false;
        //return true;
    }

}
