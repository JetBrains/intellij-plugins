package org.intellij.prisma.ide.schema.types

enum class PrismaIndexAlgorithm(val documentation: String) {
  BTree("Can handle equality and range queries on data that can be sorted into some ordering (default)."),
  Hash("Can handle simple equality queries, but no ordering. Faster than BTree, if ordering is not needed."),
  Gist("Generalized Search Tree. A framework for building specialized indices for custom data types."),
  Gin("Generalized Inverted Index. Useful for indexing composite items, such as arrays or text."),
  SpGist("Space-partitioned Generalized Search Tree. For implenting a wide range of different non-balanced data structures."),
  Brin(
    "Block Range Index. If the data has some natural correlation with their physical location within the table, can compress very large amount of data into a small space.");

  companion object {
    fun fromString(name: String?): PrismaIndexAlgorithm? =
      PrismaIndexAlgorithm.values().find { it.name == name }
  }
}