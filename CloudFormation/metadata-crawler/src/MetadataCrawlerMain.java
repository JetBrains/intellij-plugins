public class MetadataCrawlerMain {
  public static void main(String[] args) throws Exception {
    OfficialExamplesSaver.save();
    ChangeLogSaver.saveChangeLog();
    ResourceTypesSaver.saveResourceTypes();
  }
}
