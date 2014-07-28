
<fold text='/.../' expand='false'>// foo

/**
 * bar
 */

/// baz

/*
s
 */</fold>

library foo;

export <fold text='...' expand='true'>"dart:core";
<fold text='/*...*/' expand='true'>/* comment */</fold>
// comment
/// doc
<fold text='/**...*/' expand='true'>/**
 * doc
 */</fold>
import "dart:core";

import "";

export "";</fold>