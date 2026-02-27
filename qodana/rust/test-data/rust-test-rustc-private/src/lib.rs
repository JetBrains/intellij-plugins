#![feature(rustc_private)]

use std::fs; // unused import to trigger RsUnusedImport

fn helper_never_called() -> i32 {
    return 0
}
