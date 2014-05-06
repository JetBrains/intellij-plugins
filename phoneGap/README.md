###About PhoneGapIntelliJPlugin

IntelliJ Platform Plugin for PhoneGap/Cordova Application

* PhoneGap Project wizard (not stable)
* PhoneGap Project importer
* Run PhoneGap Application from IntelliJ toolbar
* Ripple Emulator integration

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
* IntelliJからPhoneGapプロジェクトの実行

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

#####(5)PhoneGapアプリを実行
Run -> Edit Configuration
にPhoneGapが追加されているはずです．新規Configurationを作成し，実行してください．

Android : Androidをターゲットにビルドします．

#####(6)デバッグ

Configuration作成時にRipple Emuを選択した場合，Rippleエミュレータを利用出来ます．

######Rippleエミュレータのインストール

RippleエミュレータはChromeAppです．Chrome Web Storeからインストールしてください．
Rippleエミュレータに関しては

[Apache Ripple公式サイト](http://ripple.incubator.apache.org/)

######Rippleエミュレータ上での実行

Chromeブラウザで

http://localhost:1337

にアクセスしてください．



Configuration作成時にweinreを選択した場合，weinreによるリモートデバッグを利用出来ます．

###ToDo

* Windows support
* weinre support
* Ripple Emulator integration (by NanoHTTPD or HttpServer)
* GUI builder(sencha touch, Kendo UI, OnsenUI, etc...)
* LogCat integration(or helper for javaScript debugging)
* Suggest for PhoneGap function, event (onDeviceReady(), onSuccess(), ..etc)
* Windows Phone support
* nvm support
* PhoneGap Build support
* Management PhoneGapPlugin (auto download and auto conf config.xml)
