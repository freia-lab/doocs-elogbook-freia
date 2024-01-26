package settings;
/*
 * Settings.java
 *
 * Created on September 15, 2008, 10:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 * Class to save the constant values for the application
 *
 * @author jstrampe
 */
public class Settings 
{
    /** will read confs from a fix path if enabled */
    public static boolean DEBUG = false;
    
    /** absolute path to the logfile */
    public static String LOGFILE = "/var/tmp/elogbook/logs/cronjob.log";

    /** path of the fragments directory */
    public static String FRAGMENTS = "/var/tmp/elogbook/fragments";
    
    /** max size of the logfile in bytes */
    public static int LOGFILE_MAXSIZE = 100000;

    /** when true there will be much scheduler log messages in the logfile */
    public static boolean LOG_ALL_SCHEDULER_ACTIONS = false;
    
    /** name of the session cookie */
    public static String SESSION_COOKIE = "sessionId";
    /** name of the login cookie */
    public static String LOGIN_COOKIE = "lgin";
    /** name of the request cookie */
    public static String REQUEST_COOKIE = "rqstCookie";
    
    /** opening response tag */
    public static String RESPONSE_OPEN = "<response>";
    
    /** closing response tag */
    public static String RESPONSE_SUB_CLOSE = "</response>";
    
    /** entry xml element used for message from servlet to client */
    public static String ELEMENT_ENTRY = "entry";
        
    /** label xml element used for message from servlet to client */
    public static String ELEMENT_LABEL = "label";
    
    /** status xml element used for message from servlet to client */
    public static String ELEMENT_STATUS = "status";
    
    /** command xml element used for message from servlet to client */
    public static String ELEMENT_COMMAND = "command";
    
    /** edit xml element used for message from servlet to client */
    public static String ELEMENT_EDIT = "edit";
    
    /** info xml element used for message from servlet to client */
    public static String ELEMENT_INFO = "info";
    
    /** link xml element used for message from servlet to client */
    public static String ELEMENT_LINK = "link";
    
    /** id xml attribute used for message from servlet to client */
    public static String ATTRIBUT_ID = "id";
    
    /** hasSub xml attribute used for message from servlet to client */
    public static String ATTRIBUT_HAS_SUB = "hasSub";
    
    /** running xml attribute used for message from servlet to client */
    public static String ATTRIBUT_RUNNING = "running";
    
    /** code xml attribute used for message from servlet to client */
    public static String ATTRIBUT_CODE = "code";
    
    /** link xml attribute used for message from servlet to client */
    public static String ATTRIBUT_LINK = "link";
    
    /** string representation of boolean value true */
    public static String VALUE_TRUE = "true";
    
    /** string representation of boolean value false */
    public static String VALUE_FALSE = "false";
    
    /** name used for conjobs */
    public static String MAIN_CREATION_CRONJOB_NAME = "cronjob";

    /** name used for single fired jobs */
    public static String SIMPLE_JOB_NAME = "simplejob";
    
    /** time a job will wait to create a new shift in ms */
    private static int SLEEP_TIME = 0;
    
    /** interval between jobs to start shift creation in ms */
    private static int SLEEP_TIME_INTERVAL = 100;
    
    /** max sleep time a job can wait for shift creation in ms */
    private static int SLEEP_TIME_MAX = 3000;
    
    /** command used for com. with client to stop a job */
    public static String COMMAND_STOP_JOB = "stopJob";
    
    /** command used for com. with client to start a job */
    public static String COMMAND_START_JOB = "startJob";
    
    /** command used for com. with client to save a conf file entry */
    public static String COMMAND_EDIT = "saveConf";
    
    /** command used for com. with client to reload the treeData.xml file */
    public static String COMMAND_RELOAD_TREE = "reloadTree";
    
    /** command used for com. with client to recreate the treeData.xml file */
    public static String COMMAND_CREATE_TREE_DATABASE = "createTreeDatabase";
    
    /** command used for com. with client to reload a logbook and its managed components (reload conf) */
    public static String COMMAND_RELOAD_LOGBOOK = "reloadLogbook";

    /** command used for com. with client to recreate the index files */
    public static String COMMAND_CREATE_INDEX = "createSearchIndex";
    
    /** unique id to mark a new conf file entry */
    public static String NEW_CONF_FILE_ELEMENT_ID = "sehr_neuer_eintrag";
    
    /** this tag must be included in the conf.xml to manage a logbook */
    public static String REQUIRED_CONF_TAG = "tree_servlet";
    
    /** Max length when converting a loaded tree to a string */
    public static int MAX_TREE_TO_STRING_LENGTH = 2000;

    /** Password for the manager (not very secure) */
    public static String MANAGER_PASSWORD = "***********";

    /** Optimization times for all indexes */
    public static String [] OPTIMIZE_TIMES = {"06:00", "14:00", "22:00"};
    
    /** commands used to set search server debug level: */
    public static String  COMMAND_NO_DEBUG = "NoDebug";
    public static String  COMMAND_NORMAL_DEBUG = "Normal";
    public static String  COMMAND_HIGH_DEBUG = "High";
    public static String  COMMAND_HIGHEST_DEBUG = "Highest";

    /**
     * calculates the sleeptime for a job before it can
     * create a new shift
     */
    public static int getSleepTime()
    {
        int result = SLEEP_TIME;
        SLEEP_TIME += SLEEP_TIME_INTERVAL;
        if (SLEEP_TIME > SLEEP_TIME_MAX)
        {
            SLEEP_TIME = 0;
        }
        return result;
    }
    
    /** Creates a new instance of Settings */
    public Settings() {
    }
    
}
