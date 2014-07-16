
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
/* comment */
// comment
/// doc
/**
 * doc
 */
import "dart:core";

import "";

export "";</fold>