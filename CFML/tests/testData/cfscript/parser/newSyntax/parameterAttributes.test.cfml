component rest="true" restpath="widget"{

  remote string function getWidget(string widgetid restargsource="path") httpmethod="GET" restpath="{widgetid}" {

  return arguments.widgetid;
}

}