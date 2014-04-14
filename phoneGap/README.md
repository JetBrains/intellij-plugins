###About PhoneGapIntelliJPlugin

IntelliJ Platform Pligin for PhoneGap/Cordova Applocation

* PhoneGap Project wizard (not stable)
* PhoneGap Project importer
* Run PhoneGap Application from IntelliJ toolbar

###About PhoneGap/Cordova

See

http://phonegap.com/

###How to use
####(0)Install PhoneGapIntelliJPlugin
Preference -> Plugins -> Search "PhoneGap Plugin"
Restart IntelliJ

####(1)Install PhoneGap
$npm install -g PhoneGap

####(2)Install Android SDK
If you want to develop Android app. You must install Android SDK and set PATH.

####(3)Create PhoneGap Project
$phonegap create hellophonegap

####(4)Import PhoneGap Project
File -> Import Project -> PhoneGap Project root dir -> Import form external model -> PhoneGap -> Finish

####(5)Run PhoneGap app
Run -> Edit Configuration -> Create new Config -> run

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
本プラグインを動作させるためにはIntelliJ Platformがjvm1.7以上で動作している必要があります．

#####(1)PhoneGapをインストール
Node.jsを使ってインストールする必要があります．Nodeのインストール方法は特に指定しませんがnvmをお勧めします．
npm install -g phonegap

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

###ToDo

* iOS support
* Windows Phone support
* PhoneGap Build support
* PhoneGapPlugin support
