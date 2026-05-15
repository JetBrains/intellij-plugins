/*global jQuery */
jQuery.extend = function (x) {};
// Example taken from jQuery 1.4.2 source
jQuery.extend({ /* ... */ isEmptyObject: function( obj ) { <error descr="JSHint: The body of a for in should be wrapped in an if statement to filter unwanted properties from the prototype. (W089)">for</error> ( var name in obj ) { return false; } return true; } /* ... */ });

// foo and bar are configured in predef option
foo();
bar();
<error descr="JSHint: eval can be harmful. (W061)">eval</error>("it");
function bar1() {
  var f;
  var f2;
}
