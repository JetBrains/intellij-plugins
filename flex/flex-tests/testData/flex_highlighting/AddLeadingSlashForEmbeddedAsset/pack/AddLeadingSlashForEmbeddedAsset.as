package pack {

public class AddLeadingSlashForEmbeddedAsset {
    [Embed(source="<error>assets</error>/<error>non_existent.png</error>")]
    private var asset2:Class;

    [Embed(source="<error>assets</error>/<error>foo.txt</error>")]
    private var asset1:Class;
}
}