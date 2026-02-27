fn main() {
    let base_unused = 1;  // Always detected
}

// Always true on any real platform (unix OR windows)
#[cfg(any(unix, windows))]
fn always_compiled() {
    let always_unused = 2;  // Should always be detected
}

// Always false (impossible: unix AND windows simultaneously)
#[cfg(all(unix, windows))]
fn never_compiled() {
    let never_unused = 3;  // Should never be detected
}

// Nested cfg to verify complex conditions work
#[cfg(any(unix, windows))]
mod platform_module {
    #[cfg(all(unix, windows))]
    pub fn impossible_fn() {
        let impossible_unused = 4;  // Never detected
    }

    pub fn possible_fn() {
        let possible_unused = 5;  // Always detected (module is compiled)
    }
}
