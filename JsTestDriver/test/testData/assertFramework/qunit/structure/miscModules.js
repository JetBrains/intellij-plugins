module("botva", 1); // not a module

module(1, function() {}); // not a module

a_lc = {};
/*module name:a, id:1*/module('a', a_lc)/*moduleEnd id:1*/;

module('unknown', undefined_lc); // not a module

var b_lc = {};
/*module name:b, id:2*/module('b', b_lc)/*moduleEnd id:2*/;


var c_lc = 1;
module('c', c_lc); // not a module
