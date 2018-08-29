# Script for setting the enviroment needed for all elogbook
# purposes. This is added to the enviroment the JAVA VM is
# running.
#
# Author: R. Kammering	Date: 2003-09-29
#

PATH=ELOGBINPATH:${PATH}:/bin:/sbin:/usr/bin:/usr/local/bin
LD_LIBRARY_PATH=ELOGBINPATH:/usr/lib:/usr/local/lib:/local/lib
JAVA_HOME=JAVA_DIR
CATALINA_HOME=TOMCAT_HOME
CATALINA_BASE=TOMCAT_DIR
HTDOCS=HTDOCS_DIR

export PATH LD_LIBRARY_PATH JAVA_HOME CATALINA_HOME CATALINA_BASE HTDOCS
