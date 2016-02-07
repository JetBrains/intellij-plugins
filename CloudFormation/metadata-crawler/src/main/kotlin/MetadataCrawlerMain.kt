object MetadataCrawlerMain {
  @JvmStatic fun main(args: Array<String>) {
    OfficialExamplesSaver.save()
    ChangeLogSaver.saveChangeLog()
    ResourceTypesSaver.saveResourceTypes()
  }
}
