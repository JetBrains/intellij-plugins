#!/bin/sh

####
# Run this script to regenerate _HbLexer.java from handlebars.flex
####

if [ ! -z $1 ]; then
    IDEA_SRC_ROOT=$1
fi


if [ -z ${IDEA_SRC_ROOT} ]; then
    echo 'ERROR: IDEA source root not specified.'
    echo '  Pass the path to the root of the Intellij IDEA Community Edition\n  source code as a argument or set environment variable $IDEA_SRC_ROOT'
    exit 1
fi

# ensure we are in this scripts directory
pushd "$( dirname $0 )"

# thanks to http://code.google.com/p/google-closure-soy/source/browse/trunk/src/net/intellij/plugins/soy/lexer/build-lexer.cmd
# for help with the command line switches
${IDEA_SRC_ROOT}/tools/lexer/jflex-1.4/bin/jflex --charat --nobak --skel \
  ${IDEA_SRC_ROOT}/tools/lexer/idea-flex.skeleton -d . --verbose handlebars.flex \
  || echo '\nERROR: Lexer generation failed.  Check errors above\n  and ensure you provided the correct IDEA source root'

popd