###About PhoneGapIntelliJPlugin

IntelliJ Platform Plugin for PhoneGap/Cordova Application

* PhoneGap Project wizard (not stable)
* PhoneGap Project importer (recommend)
* Run PhoneGap Application from IntelliJ toolbar
* Ripple Emulator integration (not stable)

###About PhoneGap/Cordova

See

http://phonegap.com/

###How to use
####(0)Install PhoneGapIntelliJPlugin
Preference -> Plugins -> Search "PhoneGap Plugin"

Restart IntelliJ

####(1)Install NodeJS and PhoneGap
Please install from [Node.js Official site](http://nodejs.org/)

nvm is not supported (So sorry fix soon)

$npm install -g PhoneGap

PhoneGap dir must be

 '/usr/local/bin/phonegap'

####(2)Install Android SDK
If you want to develop Android app. You must install Android SDK and set PATH.

####(3)Create PhoneGap Project
$phonegap create hellophonegap

####(4)Import PhoneGap Project
File -> Import Project -> PhoneGap Project root dir -> Import form external model -> PhoneGap -> Finish

####(5)Run PhoneGap app
Run -> Edit Configuration -> Create new Config -> run

#####(6)Debug PhoneGap app

###PhoneGapIntelliJPluginについて

PhoneGap/Cordovaアプリ開発をIntelliJ IDEAで行えるようにするプラグインです．

PhoneGapIntelliJPluginの機能は以下の通りです．

* PhoneGapプロジェクトウィザード（開発中）
* PhoneGapプロジェクトのインポート
* IntelliJからPhoneGapプロジェクトの実行（エミュレータ）

###PhoneGap/Cordovaについて

http://phonegap.com/

http://phonegap-fan.com/

###使い方
#####(0)PhoneGapIntelliJPluginをインストール
Preference -> PluginsからPhoneGapと検索してください．IntelliJの再起動が必要です．

#####(1)PhoneGapをインストール
Node.jsを使ってインストールする必要があります．nvmでの動作は保証しません．
公式サイトからのインストールをお勧めします．

$npm install -g phonegap

PhoneGapのインストールディレクトリは

 '/usr/local/bin/phonegap'

である必要があります．ご不便をおかけして申し訳ありません．近くnvmにも対応したいと思っています．

#####(2)Android SDKをインストール
AndroidSDKをインストールし，PATHを通しておいてください．

#####(3)PhoneGapプロジェクトを作成
phonegap create my-app

#####(4)PhoneGapアプリをIntelliJにインポート

プロジェクトのインポート
File -> Import Project -> PhoneGapプロジェクトのルートディレクトリを指定 -> Import form external model -> PhoneGap -> Finish

#####(5)エミュレータ起動
Run -> Edit Configuration
PhoneGapが追加されているはずです．新規Configurationを作成し，実行してください．

#####(6)デバッグ


###ToDo

* weinre support
* Ripple Emulator integration (by NanoHTTPD or HttpServer)
* LogCat integration(or helper for javaScript debugging)
* Suggest for PhoneGap function (onDeviceReady(), onSuccess(), ..etc)
* Windows Phone support
* nvm support
* PhoneGap Build support
* Management PhoneGapPlugin (auto download and auto conf config.xml)
* Component management with Bower (or other tool)
