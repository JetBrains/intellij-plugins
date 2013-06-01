class ReplaceAll2 {
    main() {
        var tmp = false;
        var foo = tmp ? 239 : -239;
        for (i in [1, 2, 3]) {
            print(foo);
        }
        var a = 239 + (foo);
    }
}