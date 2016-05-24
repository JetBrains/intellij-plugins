var a2 = ' </html>';
var a3 = '<!-- comment>';
var a4 = '<h' "t" '''m''' """l""" r'>' r"<" r'''/''' r"""html>""";
var a5= r"""
<html>

</html>
        """;
var a6 = '<$foo>$foo</$foo>';
var a7 = '<${foo}>${foo}</${foo}>';
var a8 = '<foo><bar><baz/>';
var a9 = '<foo><bar></baz>';

var b1 = '< html>no injection </ html>';
var b2 = "<foo>";
var b3 = "<foo><bar><baz>";
var b4 = "List<int>";
var b5 = "x<html/>";
