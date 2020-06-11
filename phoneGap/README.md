###About Cordova Plugin for IntelliJ

Cordova plugin for IntelliJ IDEA

#####Supported Cordova-based Frameworks: Cordova, PhoneGap, Ionic
* Project wizard
* Run configuration
* Code Completion for event types (only in javascript)
* Plugin Manager
* Auto exclude 'platforms' directory

###How to use
####(0) Install Cordova plugin for IntelliJ
Preference -> Plugins -> Search "Cordova Plugin"

Restart IntelliJ IDEA

####(1) Install NodeJS and Cordova/PhoneGap/Ionic
Please install from [Node.js Official site](http://nodejs.org/)

For PhoneGap

$npm install -g phonegap

For Cordova

$npm install -g cordova

For Ionic

$npm install -g ionic

####(2) Install SDKs
If you want to create Android app. You must install Android SDK and set PATH.

If you want to create iOS app. You must install Xcode and commandline tools and ios-sim.

#####(2)-1 Install Xcode and commandline tools

See https://developer.apple.com/xcode/

#####(2)-2 Install ios-sim

$npm install -g ios-sim

####(3) Create Cordova Project

see https://confluence.jetbrains.com/display/IntelliJIDEA/PhoneGap%2C+Cordova+and+Ionic

####(4) Run Cordova app
Run -> Edit Configuration -> Create new Cordova run Configuration -> run

### ToDo List

* Code completion for Cordova function, event (onDeviceReady(), onSuccess(), ..etc)
* WindowsPhone support
* weinre support
* Windows Phone support
* nvm support