# Serial Monitor plugin for IntelliJ IDEA Platform #

This plugin aims to leverage Arduino development with CLion by providing Serial Monitor Tool Window. It allows you to communicate to your
Arduino device without leaving the IDE. Functionality is pretty similar to those, available in Arduino IDE through "Serial Monitor Tool".
**Note:** Only COM port (both hardware and virtual) is currently supported. In order to use virtual COM port please make sure to install all
necessary drivers.

## Features ##

- Serial Monitor Tool Window with project-based persistent settings
- Multiple serial port profiles
- Duplex Console View (Regular view + HEX)

## Change Notes ##

- **2022.1** 
  - Original [version by Dmitry Cherkas](https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor) is forked and imported into contrib repository 
  - Bunch of fixes and refactorings
  - Multiple serial connections are supported
  - Apple M1 support

- **0.6**
  - publish the plugin under Apache License v.2.0
  - remove the usage of deprecated APIs
  - build with java11
- **0.5.5**
  - update icons: Icons made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [www.flaticon.com](https://www.flaticon.com/)
  - upgrade jssc lib to resolve issue with win10 crash on jdk11
  - add configuration option to allow hiding status
    widget [#21](https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/21/allow-disabling-of-status-bar-widget)
    remove deprecated apis usage as of 2020.1 EAP Dmitry Cherkas 26.02.2020 18:02
- **0.5.4**
  - fix for [#24](https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/24/not-working-in-clion-20201-eap)
  - fix incorrect settings path (use relative path instead of absolute)
- **0.5.3**
  - fix for [#11](https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/11)
    and [#13](https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/13) (thanks to Sven Sübert)
- **0.5.2**
  - migrated plugin to gradle build
- **0.5.1**
  - fixed IDE freezes on Mac OS
- **0.5**
  - possible solution for Mac OS support (https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/4)
- **0.4.7**
  - rebuild with Java7
- **0.4.6**
  - fixed https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/3/plugin-error-in-android-studio
- **0.4.5**
  - improved port name validation in settings dropdown
  - added Troubleshooting section to README
- **0.4.1**
  - added plugin logo
  - SerialService converted to projectComponent in order to allow simultaneous usage in multiple open projects
  - fixed NPE when trying to dispose non-initialized SerialMonitorView
- **0.4**
  - added history support for sent commands + keyboard shortcut to send on Ctrl+Enter
  - implemented exception handling via notification popups
  - fixed https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/1
- **0.3**
  - implemented duplex console to switch between HEX/regular views
  - changed behaviour of status widget
- **0.2**
  - implemented serial monitor status bar
  - added README
- **0.1.1**
  - fixed "EditorImpl hasn't been released" exception on idea shutdown
  - switched to purejavacomm SerialService implementation
- **0.1**
  - Initial version

## Troubleshooting ##

On Linux you may not see you port in available ports dropdown due to lack or permissions.

- To check, that serial port is really there use ```dmesg | grep tty```
- To set read and write permission to port, use ```sudo chmod a+rw <your_port_name>```, where **your_port_name** should
  be something similar to _/dev/ttyACM0_, use the output of the above command to check the exact name.
