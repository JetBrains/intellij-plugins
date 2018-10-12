package training.ui

object MessageUtils {

  fun List<Message>.extractTag(tagName: String, messageType: Message.MessageType): List<Message> {
    val groupedRegex = Regex("<$tagName>(.*?)</$tagName>")
    val regex = Regex("<$tagName>.*?</$tagName>")
    return this.map { message ->
      if (message.isText && groupedRegex.find(message.text) != null) {
        val splitByTag = regex.split(message.text)
        val groups =  regex.findAll(message.text).map { it.groups }.flatten().toList()
        val result = ArrayList<Message>()
        groups.forEachIndexed { i, _ -> result.add(Message(splitByTag[i], message.type))
          val textInTag = groupedRegex.find(groups[i]!!.value)!!.groups[1]!!.value
          result.add(Message(textInTag, messageType))
        }
        result.add(Message(splitByTag.last(), message.type))
        result
      } else listOf(message)
    }.flatten().toList()
  }

}