#!/bin/sh
#
# Script for creation of a status file to be parsed for status info.
#
# NOTE: After creation of the status.xml file it needs to be checked
# for well formed'ness. Here this is done by use of 'xmllint' (libxml2).
#

cd `dirname $0`
. ../../elogbook/bin/elogenv.sh
LD_LIBRARY_PATH=/usr/lib:../../elogbook/bin:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH

PATH=/usr/bin:/bin:/sbin:.:/usr/local/bin:../../elogbook/bin
export PATH

# Name of the status file to be created/updated
FILE="../jsp/status.xml"
TMPFILE="../jsp/status.xml.tmp"

# Used for error output
DATE=`date`

# Parse values from config file 
#DOCROOT=`../../elogbook/bin/xmlGetFirstElement ../jsp/conf.xml docroot`
#LOGROOT=`../../elogbook/bin/xmlGetFirstElement ../jsp/conf.xml logroot`
DOCROOT=`../../elogbook/bin/GetXMLElement docroot ../jsp/conf.xml`
LOGROOT=`../../elogbook/bin/GetXMLElement logroot ../jsp/conf.xml`

# Read relevant data from your system
# Insert statements to fill values for assignment below:

# Assign status values to shell variables
STATUS_VAL1="Status 1"
STATUS_VAL2="Status 2"
STATUS_VAL3="Status 3"
STATUS_VAL4="Status 4"
STATUS_VAL5="Status 5"
STATUS_VAL6="Status 6"

# Normalize the status strings
STATUS_VAL1=`echo $STATUS_VAL1 | tr -d '[\000-\010][\013-\014][\016-\037][\177-\377]' | sed 's/&/\&amp;/g' | sed 's/>/\&gt;/g' | sed 's/</\&lt;/g' | sed 's/"/\&quot;/g' | sed "s/'/\&apos;/g"`
STATUS_VAL2=`echo $STATUS_VAL2 | tr -d '[\000-\010][\013-\014][\016-\037][\177-\377]' | sed 's/&/\&amp;/g' | sed 's/>/\&gt;/g' | sed 's/</\&lt;/g' | sed 's/"/\&quot;/g' | sed "s/'/\&apos;/g"`
STATUS_VAL3=`echo $STATUS_VAL3 | tr -d '[\000-\010][\013-\014][\016-\037][\177-\377]' | sed 's/&/\&amp;/g' | sed 's/>/\&gt;/g' | sed 's/</\&lt;/g' | sed 's/"/\&quot;/g' | sed "s/'/\&apos;/g"`
STATUS_VAL4=`echo $STATUS_VAL4 | tr -d '[\000-\010][\013-\014][\016-\037][\177-\377]' | sed 's/&/\&amp;/g' | sed 's/>/\&gt;/g' | sed 's/</\&lt;/g' | sed 's/"/\&quot;/g' | sed "s/'/\&apos;/g"`
STATUS_VAL5=`echo $STATUS_VAL5 | tr -d '[\000-\010][\013-\014][\016-\037][\177-\377]' | sed 's/&/\&amp;/g' | sed 's/>/\&gt;/g' | sed 's/</\&lt;/g' | sed 's/"/\&quot;/g' | sed "s/'/\&apos;/g"`
STATUS_VAL6=`echo $STATUS_VAL6 | tr -d '[\000-\010][\013-\014][\016-\037][\177-\377]' | sed 's/&/\&amp;/g' | sed 's/>/\&gt;/g' | sed 's/</\&lt;/g' | sed 's/"/\&quot;/g' | sed "s/'/\&apos;/g"`

# Now print values to file
echo "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" > $TMPFILE
echo "<status_info>" >> $TMPFILE
echo "<status_val1>${STATUS_VAL1}</status_val1>" >> $TMPFILE
echo "<status_val2>${STATUS_VAL2}</status_val2>" >> $TMPFILE
echo "<status_val3>${STATUS_VAL3}</status_val3>" >> $TMPFILE
echo "<status_val4>${STATUS_VAL4}</status_val4>" >> $TMPFILE
echo "<status_val5>${STATUS_VAL5}</status_val5>" >> $TMPFILE
echo "<status_val6>${STATUS_VAL6}</status_val6>" >> $TMPFILE
echo "</status_info>" >> $TMPFILE

# Check file for well formed'ness
`xmllint --noout $TMPFILE 2> /dev/null`
# Leave old file intact if not well formed
FORMEDNESS=`echo $?`
if   [ "$FORMEDNESS" = "0" ]; then `mv $TMPFILE $FILE`;
else echo "$DATE: Error creating new status info. file: $FILE";
fi

exit 0
