package pack {

public class AddLeadingSlashForEmbeddedAsset {
    [Embed(source="assets/non_existent.png")]
    private var asset2:Class;

    [Embed(source="/assets/foo.txt")]
    private var asset1:Class;
}
}