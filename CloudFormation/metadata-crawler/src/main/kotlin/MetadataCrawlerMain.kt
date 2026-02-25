object MetadataCrawlerMain {
  private enum class CrawlerMode {
    METADATA,
    EXAMPLES,
    ALL,
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val mode = (args.firstOrNull() ?: System.getenv("CRAWLER_MODE") ?: "metadata").trim().uppercase()
    when (CrawlerMode.valueOf(mode)) {
      CrawlerMode.METADATA -> ResourceTypesSaver.saveResourceTypes()
      CrawlerMode.EXAMPLES -> {
        OfficialExamplesSaver.save()
        OfficialExamplesSaver.saveServerless()
      }

      CrawlerMode.ALL -> {
        OfficialExamplesSaver.save()
        OfficialExamplesSaver.saveServerless()
        ResourceTypesSaver.saveResourceTypes()
      }
    }
  }
}
