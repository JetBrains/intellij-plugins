#!/bin/sh
#
# ------------------------------------------------------
#  Flex IDE Startup Script for Unix
# ------------------------------------------------------
#

# ---------------------------------------------------------------------
# Before you run Flex IDE specify the location of the
# JDK 1.6 installation directory which will be used for running Flex IDE
# ---------------------------------------------------------------------
[ `uname -s` = "Darwin" ] && OS_TYPE="MAC" || OS_TYPE="NOT_MAC"

if [ -z "$FLEXIDE_JDK" ]; then
  FLEXIDE_JDK=$JDK_HOME
  # if jdk still isn't defined and JAVA_HOME looks correct. "tools.jar" isn't included in Mac OS Java bundle
  if [ -z "$FLEXIDE_JDK" ] && ([ "$OS_TYPE" = "MAC" -a -x "$JAVA_HOME/bin/java" ] || [ -f "$JAVA_HOME/lib/tools.jar" ]); then
    FLEXIDE_JDK=$JAVA_HOME
  fi

  if [ -z "$FLEXIDE_JDK" ]; then
    # Try to get the jdk path from java binary path
    JAVA_BIN_PATH=`which java`

    if [ -n "$JAVA_BIN_PATH" ]; then
      # Mac readlink doesn't support -f option.
      [ "$OS_TYPE" = "MAC" ] && CANONICALIZE_OPTION="" || CANONICALIZE_OPTION="-f"

      JAVA_LOCATION=`readlink $CANONICALIZE_OPTION $JAVA_BIN_PATH`
      case "$JAVA_LOCATION" in
        */jre/bin/java)
          JAVA_LOCATION=`echo "$JAVA_LOCATION" | xargs dirname | xargs dirname | xargs dirname` ;;
        *)
          JAVA_LOCATION=`echo "$JAVA_LOCATION" | xargs dirname | xargs dirname` ;;
      esac

      if [ "$OS_TYPE" = "MAC" ]; then
        if [ -x "$JAVA_LOCATION/CurrentJDK/Home/bin/java" ]; then
          FLEXIDE_JDK="$JAVA_LOCATION/CurrentJDK/Home"
        fi
      else
        if [ -x "$JAVA_LOCATION/bin/java" ]; then
          FLEXIDE_JDK="$JAVA_LOCATION"
        fi
      fi
    fi
  fi

  if [ -z "$FLEXIDE_JDK" ]; then
    echo ERROR: cannot start Flex IDE.
    echo No JDK found to run Flex IDE. Please validate either FLEXIDE_JDK, JDK_HOME or JAVA_HOME environment variable points to valid JDK installation.
    echo
    echo Press Enter to continue.
    read IGNORE
    exit 1
  fi
fi

VERSION_LOG='/tmp/java.version.log'
$FLEXIDE_JDK/bin/java -version 2> $VERSION_LOG
grep 'OpenJDK' $VERSION_LOG
OPEN_JDK=$?
grep '64-Bit' $VERSION_LOG
BITS=$?
rm $VERSION_LOG
if [ $OPEN_JDK -eq 0 ]; then
  echo WARNING: You are launching IDE using OpenJDK Java runtime
  echo
  echo          THIS IS STRICTLY UNSUPPORTED DUE TO KNOWN PERFORMANCE AND GRAPHICS PROBLEMS
  echo
  echo NOTE:    If you have both Sun JDK and OpenJDK installed
  echo          please validate either FLEXIDE_JDK or JDK_HOME environment variable points to valid Sun JDK installation
  echo
  echo Press Enter to continue.
  read IGNORE
fi
if [ $BITS -eq 0 ]; then
  BITS="64"
else
  BITS=""
fi

#--------------------------------------------------------------------------
#   Ensure the FLEXIDE_HOME var for this script points to the
#   home directory where Flex IDE is installed on your system.

SCRIPT_LOCATION=$0
# Step through symlinks to find where the script really is
while [ -L "$SCRIPT_LOCATION" ]; do
  SCRIPT_LOCATION=`readlink -e "$SCRIPT_LOCATION"`
done

FLEXIDE_HOME=`dirname "$SCRIPT_LOCATION"`/..
FLEXIDE_BIN_HOME=`dirname "$SCRIPT_LOCATION"`

export JAVA_HOME
export FLEXIDE_HOME

if [ -n "$FLEXIDE_PROPERTIES" ]; then
  FLEXIDE_PROPERTIES_PROPERTY=-Didea.properties.file=$FLEXIDE_PROPERTIES
fi

if [ -z "$FLEXIDE_MAIN_CLASS_NAME" ]; then
  FLEXIDE_MAIN_CLASS_NAME="com.intellij.idea.Main"
fi

if [ -z "$FLEXIDE_VM_OPTIONS" ]; then
  FLEXIDE_VM_OPTIONS="$FLEXIDE_HOME/bin/flexide.vmoptions"
fi

# isEap
#if ["@@isEap@@" -eq "true" ]
# $AGENT="-agentlib:yjpagent$BITS=disablej2ee,sessionname=flexide"
#fi

[ -f "$FLEXIDE_HOME/Contents/Info.plist" ] && BUNDLE_TYPE="MAC" || BUNDLE_TYPE="NOT_MAC"

# If vmoptions file exists - use it
if [ -r "$FLEXIDE_VM_OPTIONS" ]; then
  JVM_ARGS=`tr '\n' ' ' < "$FLEXIDE_VM_OPTIONS"`

  # don't extract vm options from Info.plist in mac bundle
  INFO_PLIST_PARSER_OPTIONS=""
else
  [ "$BUNDLE_TYPE" = "MAC" ] && [ "$BITS" == "64" ] && INFO_PLIST_PARSER_OPTIONS=" 64" || INFO_PLIST_PARSER_OPTIONS=" 32"
fi

# In MacOS ./Contents/Info.plist describes all vm options & system properties
[ "$OS_TYPE" = "MAC" ] && [ "$BUNDLE_TYPE" = "MAC" ] && [ -z "$FLEXIDE_PROPERTIES_PROPERTY" ] && MAC_IDEA_PROPERTIES="`osascript \"$FLEXIDE_BIN_HOME/info_plist_parser.scpt\"$INFO_PLIST_PARSER_OPTIONS`" || MAC_IDEA_PROPERTIES=""
REQUIRED_JVM_ARGS="-Xbootclasspath/a:../lib/boot.jar -Didea.platform.prefix=Flex -Didea.no.jre.check=true -Didea.paths.selector=@@system_selector@@ $MAC_IDEA_PROPERTIES $AGENT $FLEXIDE_PROPERTIES_PROPERTY $REQUIRED_JVM_ARGS"

JVM_ARGS="$JVM_ARGS $REQUIRED_JVM_ARGS"

CLASSPATH=../lib/bootstrap.jar
CLASSPATH=$CLASSPATH:../lib/util.jar
CLASSPATH=$CLASSPATH:../lib/jdom.jar
CLASSPATH=$CLASSPATH:../lib/log4j.jar
CLASSPATH=$CLASSPATH:../lib/extensions.jar
CLASSPATH=$CLASSPATH:../lib/trove4j.jar
CLASSPATH=$CLASSPATH:$FLEXIDE_JDK/lib/tools.jar
CLASSPATH=$CLASSPATH:$FLEXIDE_CLASSPATH

export CLASSPATH

LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH

cd "$FLEXIDE_BIN_HOME"
while true ; do
  $FLEXIDE_JDK/bin/java $JVM_ARGS -Djb.restart.code=88 $FLEXIDE_MAIN_CLASS_NAME $*
  test $? -ne 88 && break
done
