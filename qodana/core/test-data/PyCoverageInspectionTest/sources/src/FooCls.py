class FooCls:
    def foo(self):
        if 1 == 1:
            return 42
        ret = 10
        ret += 12
        ret -= 11

        return ret ** 2

    def bar(self, i):
        if i % 2 == 1:
            fun2()
            return 42
        fun1()
        self.baz(i)

        return -1

    def baz(self, i):
        if i % 2 == 1:
            return 42
        elif i == 3:
            return -2
        elif i == 5:
            return -2
        return -1

    def foobar(self):
        return 42


def fun1():
    ret = "A"
    ret += "b"
    return ret

def fun2():
    return 42

class BaseClass:
    def __init__(self):
        print("In base class constructor")
        print("In base class constructor 2")
