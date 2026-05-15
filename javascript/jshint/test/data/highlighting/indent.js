function f() {
	const x = <error descr="JSHint: 'xyz' is not defined. (W117)">xyz</error>;
	if (x) {
		let <error descr="JSHint: 'y' is defined but never used. (W098)">y</error> = 1;
	}
}
f();
