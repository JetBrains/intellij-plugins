class Foo {
    // function fBar (x,y);
    fOne(argA, argB, argC,
argD, argE, argF, argG, argH) {
        Array<string> numbers   = ['one', 'two', 'three', 'four', 'five', 'six'];
        var x = ("" + argA) +
     argB + argC + argD +
                argE + argF + argG + argH;
        try {
            this.fTwo(
            argA, argB, argC, this.fThree(
            "", argE, argF, argG, argH));
        } catch (string ignored) {}
        var z = argA == 'Some string' ?
        'yes' : 'no';
        var colors = ['red', 'green', 'blue', 'black', 'white', 'gray'];
        for (colorIndex in colors) {
            var colorString = numbers[colorIndex];
        }
        do {
            colors.pop();
        } while (colors.length > 0);
    }

    fTwo(strA,
    strB, strC, strD) {
        if (true)
        return strC;
        if (strA == 'one' ||
        strB == 'two') {
            return strA + strB;
        } else if (true) return strD;
        throw strD;
    }

    fThree(strA, strB, strC, strD, strE) {
        return strA + strB + strC + strD + strE;
    }
}