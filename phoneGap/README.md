###PhoneGapIntelliJPluginについて

PhoneGap/Cordovaアプリ開発をIntelliJ IDEAで行えるようにするプラグインです．

###PhoneGap/Cordovaについて

http://phonegap.com/
http://phonegap-fan.com/

###使い方
#####(0)PhoneGapIntelliJPluginをインストール
Preferance -> PluginsからPhoneGapと検索してください．IntelliJの再起動が必要です．
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
File -> Import Project -> PhoneGapプロジェクトのルートディレクトリを指定

#####(5)エミュレータ起動
Edit ConfigurationにPhoneGapが追加されているはずです．新規Configurationを作成し，実行してください．

###ToDo

* iOS support
* Windows Phone support
* Project generator
* PhoneGap Build support
* PhoneGapPlugin support
