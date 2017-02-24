var namedGroup:RegExp = /(?P<name>group)/
var namedGroup2:RegExp = /<error descr="This named group syntax is not supported in this regex dialect">(?<name>group)</error>/
var namedGroup3:RegExp = /<error descr="This named group syntax is not supported in this regex dialect">(?'name'group)</error>/