main() {
    var args = new Options().arguments;
    if (args.length == 0) {
        print("Usage: dart embiggen.dart phrase");
        return;
    }
    var
    phrase = args[0];
    print(embiggen(phrase));
}

String embiggen
    (String msg) {
    if (msg == null) {
        throw new ArgumentError("must not be null");
    }
    return msg.toUpperCase();
}