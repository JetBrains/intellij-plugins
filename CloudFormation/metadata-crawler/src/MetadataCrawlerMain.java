public class MetadataCrawlerMain {
  public static void main(String[] args) throws Exception {
    ChangeLogSaver.saveChangeLog();
    ResourceTypesSaver.saveResourceTypes();
  }
}
