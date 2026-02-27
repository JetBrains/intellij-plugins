fn main() {
    let base_unused = 1;  // Line 2: always analyzed
}

#[cfg(feature = "enabled_feature")]
fn enabled_feature_fn() {
    let enabled_unused = 2;  // Line 7: analyzed (feature enabled by default)
}

#[cfg(feature = "disabled_feature")]
fn disabled_feature_fn() {
    let disabled_unused = 3;  // Line 12: NOT analyzed (feature not enabled)
}

#[cfg(feature = "should_not_compile")]
compile_error!("This feature should never be enabled - it verifies Qodana doesn't enable all features");
