abstract class Injector {}

class TalkToMeApp {}

startTalkToMeApp() {
  Injector inj = ngBootstrap(module: new TalkToMeApp());
}}
