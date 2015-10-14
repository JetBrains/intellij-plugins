package {

/**
 * asdoc for A
 */

// just a comment


class A<caret expected="A"> {

}

/**
 * asdoc for B
 */

/**
 * asdoc for B2
 */

// comment1
// comment2
class B<caret expected="B"> {

    /**
     * asdoc for f1
     */
    // comment
    function f1<caret expected="f1">() {

    }
}

}
