object MetadataCrawlerMain {
  @JvmStatic fun main(args: Array<String>) {
    OfficialExamplesSaver.save()
    OfficialExamplesSaver.saveServerless()
    ChangeLogSaver.saveChangeLog()
    ResourceTypesSaver.saveResourceTypes()
  }
}
