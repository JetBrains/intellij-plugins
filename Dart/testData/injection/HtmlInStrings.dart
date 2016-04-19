var a1 = '< html>no injection </ html>';
var a2 = '</html>';
var a3 = '<!-- comment>';
var a4 = '<h' "t" '''m''' """l""" r'>' r"<" r'''/''' r"""html>""";
var a5= r"""
<html>

</html>
        """;
var a6 = '<$foo>$foo</$foo>';
var a7 = '<${foo}>${foo}</${foo}>';