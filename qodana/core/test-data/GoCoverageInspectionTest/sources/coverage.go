package sources

func Foo(x, y int) int {
	return x + y
}

func Bar(x, y int) int {
	if x != 10 {
		return x - y
	}
	ret := Gar(x, y)
	ret *= 10
	return ret + 2
}

func Gar(x, y int) int {
	x += 1
	y += 1
	return x * y
}

func Baz() func() int {
	_ = Bar(4, 2)
	ret := func() int {
		return 22
	}
	ret2 := func() int {
		return 24
	}
	ret2()
	return ret
}

func Gaz(x int) func() int {
	f := func() int {
		ret := 12
		ret += 1
		return 23 + x + ret
	}
	g := func() int {
		ret := 11
		return 23 + x + ret
	}
	if g == nil {
		g()
	}
	return f
}

type FooInt interface {
	f1() int
}

type MyInt int
type MyFloat float64

func (a MyInt) f1() int {
	return 42
}

func (a MyFloat) f1() int {
	ret := int(a)
	ret += 10
	return ret + 43
}

func B() int {
	var a FooInt
	i := MyInt(10)
	a = i
	return a.f1()
}

func C() int {
	var a FooInt
	var b FooInt
	i := MyFloat(10)
	a = i
	j := MyInt(10)
	b = j
	b.f1()
	return a.f1()
}
