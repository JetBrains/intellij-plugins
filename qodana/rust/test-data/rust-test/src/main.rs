// TODO: refactor this file to remove issues detected by Qodana
use std::fs; // unused import on purpose to trigger inspection

fn main() {
    println!("Hello, world!");
 let a = 1. / 0.;
    // Intentionally left unused to trigger lints/inspections
    let _unused_number = 42;
    let _shadowed = 10;
    let _shadowed = 20; // shadowing warning/inspection

    // Unnecessary allocation/format usage to trigger clippy (Qodana) inspection
    // println!("{}", format!("{_shadowed}"));

    // Unused result (ignoring returned Result) to trigger inspection
    // let _ = "123".parse::<i32>();
}

// Dead code on purpose to trigger inspection
fn helper_never_called(a: i32) -> i32 {
    if a == 0 {
        println!("");
        // panic! usage is often discouraged and can be flagged by inspections
        panic!("a must not be zero");
    }
    a * 2
}

fn fun() ->i32 {
    return 0x0
}


// Intentionally incorrect trait bound separators to trigger Qodana inspection:
// "Reports trait bounds for a single type that are separated with a comma instead of a `+` sign"
// Correct Rust would be `<T: Send + Sync>` and `Box<dyn Send + Sync>`.
// We use commas here on purpose to be flagged by the inspection.
#[allow(dead_code)]
fn requires_bounds_with_comma<T: Send, Sync>(value: T) -> T { // <- should be `Send + Sync`
    value
}
//
// #[allow(dead_code)]
// fn takes_trait_object_with_comma(_: Box<dyn Send, Sync>) { // <- should be `Send + Sync`
// }
