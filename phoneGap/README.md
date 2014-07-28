###About PhoneGap/Cordova Plugin for IntelliJ

PhoneGap/Cordova plugin for IntelliJ IDEA

* PhoneGap/Cordova Project wizard
* PhoneGap/Cordova Project importer
* Run PhoneGap/Cordova Application from IDEA toolbar
* codecomplation
* Ripple Emulator support

###About PhoneGap/Cordova

See

http://phonegap.com/

###How to use
####(0)Install PhoneGap/Cordova plugin for IntelliJ
Preference -> Plugins -> Search "PhoneGap/Cordova Plugin"

Restart IntelliJ IDEA

####(1)Install NodeJS and PhoneGap/Cordova
Please install from [Node.js Official site](http://nodejs.org/)

$npm install -g phonegap
$npm install -g cordova

####(2)Install SDKs
If you want to create Android app. You must install Android SDK and set PATH.

If you want to create iOS app. You must install Xcode and commandline tools and ios-sim.

#####(2)-1 Install Xcode and commandline tools

See

https://developer.apple.com/xcode/

#####(2)-2 Install ios-sim

$npm install -g ios-sim

####(3)Create PhoneGap Project

PhoneGap
$phonegap create hellophonegap

Cordova
$cordova create hellocordova
$cd hellocordova
$cordova add platform android
$cordova add platform ios

####(4)Import PhoneGap Project
File -> Import Project -> PhoneGap Project root dir -> Import form external model -> PhoneGap -> Finish

####(5)Run PhoneGap app
Run -> Edit Configuration -> Create new Config -> run

#####(6)Debugging

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

#####(6)-1 Rippleエミュレータによるデバッグ

Configuration作成時にRippleを選択した場合，Rippleエミュレータを利用出来ます．
Rippleエミュレータに関しては
[Apache Ripple公式サイト](http://ripple.incubator.apache.org/)
を参照してください．

######(6)-1-1 Rippleエミュレータのインストール

RippleエミュレータはChromeAppです．Chrome Web Storeからインストールしてください．

######(6)-1-2 Rippleエミュレータ上での実行

http://localhost:1337
にアクセスし，Rippleエミュレータを有効にしてください．

実行時に生成されるserver.jsはNodeJS上で実行されるhttpサーバです．
本スクリプトが実行され，Rippleエミュレータにwwwフォルダ以下のファイルが配信されます．
本スクリプトは自由に編集可能です．独自のカスタマイズを推奨します．

スクリプトに関しては

https://github.com/masahirosuzuka/simpleserver

を参照してください．

####(6)-2 weinreによるリモートデバッグ

Configuration作成時にweinreを選択した場合，weinreによるリモートデバッグを利用出来ます．
weinreに関しては

を参照してください．

#####(6)-2-1 weinreのインストール

$npm install -g weinre

#####(6)-2-2

Configuration作成時にweinreを選択した場合，weinreスクリプトの埋め込みは自動で行われます．
ブラウザで

http://localhost:8080

にアクセスしてください．

###ToDo

* Code completion for PhoneGap function, event (onDeviceReady(), onSuccess(), ..etc)
* Replace server.js to Grunt
* WindowsPhone support
* New ConfigurationEditor
* weinre support
* Windows Phone support
* nvm support
* PhoneGap Build support
* Management PhoneGapPlugin (auto download and auto conf config.xml)
