object MetadataCrawlerMain {
  @Throws(Exception::class)
  @JvmStatic fun main(args: Array<String>) {
    OfficialExamplesSaver.save()
    ChangeLogSaver.saveChangeLog()
    ResourceTypesSaver.saveResourceTypes()
  }
}
