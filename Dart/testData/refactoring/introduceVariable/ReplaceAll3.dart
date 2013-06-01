class ReplaceAll3 {
    main() {
        var tmp = false;
        for (i in [1, 2, 3]) {
            print(tmp ? 239:-239);
        }
        var a = 239 + (<selection>tmp ? 239 : -239</selection>);
    }
}