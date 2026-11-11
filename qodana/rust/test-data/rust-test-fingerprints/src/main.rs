// Every `3.1416` triggers RsApproxConstant, one site per scope rule. It matches PI within 0.001.

pub struct Circle;

pub struct Wrapper<T>(T);

pub trait Shape {
    const RATIO: f64 = 3.1416;
}

impl Circle {
    const SCALE: f64 = 3.1416;
}

impl Circle {
    const FACTOR: f64 = 3.1416;

    pub fn area(&self) -> f64 {
        1.0
    }
}

impl<T> Wrapper<T> {
    const LIMIT: f64 = 3.1416;
}

impl<U> Wrapper<U> {
    const BOUND: f64 = 3.1416;
}

pub mod inner {
    pub const NESTED: f64 = 3.1416;
}

pub fn ratios() -> (Vec<f64>, [f64; 1]) {
    (vec![3.1416], [3.1416])
}

pub fn mapped(xs: Vec<f64>) -> Vec<f64> {
    xs.into_iter().map(|x| x * 3.1416).collect()
}

pub fn bound(xs: Vec<f64>) -> Vec<f64> {
    let scale = |x: f64| x * 3.1416;
    xs.into_iter().map(scale).collect()
}

fn main() {
    let _ = ratios();
    let _ = mapped(vec![1.0]);
}

// `scale` names a function here and a closure on line 44. The shape of the body must match.
pub fn scale(xs: Vec<f64>) -> Vec<f64> {
    xs.into_iter().map(|x| x * 3.1416).collect()
}

pub fn assigned(flag: bool) -> f64 {
    let mut f: fn(f64) -> f64 = |x: f64| x;
    if flag {
        f = |x: f64| x * 3.1416;
    }
    f(1.0)
}

pub const SCALED: fn(f64) -> f64 = |x: f64| x * 3.1416;

pub struct Holder {
    pub factor: fn(f64) -> f64,
}

impl Holder {
    pub fn install(&mut self) {
        self.factor = |x: f64| x * 3.1416;
    }
}
