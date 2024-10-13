<?php

namespace qodana;

class FooCls
{
    public function foo()
    {
        return 42;
    }

    public function bar($i)
    {
        if ($i % 2) {
            fun2();
            return 42;
        }
        return -1;
    }

    public function baz($i)
    {
        if ($i % 2) {
            return 42;
        }
        $i++;
        $i++;
        return -1;
    }

    public function foobar()
    {
        return 42;
    }
}

function fun1()
{
    phpinfo();
}

function fun2()
{
    return 42;
}

new class {
    public function foo()
    {
        function ($number) {
            return $number * 2;
        };

        function ($string) {
            return strtoupper($string);
        };

        function ($array) {
            return array_reverse($array);
        };
    }
};

$fn = fn($x = 42) =>
            /*foo*/
            $x > 15
            ? 1
            : 42;

class BaseClass {
    function __construct() {
        print "In BaseClass constructor\n";
    }
}
