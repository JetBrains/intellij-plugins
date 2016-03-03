### Flutter support for the Dart plugin ###

First, set up Dart plugin development as described in the
ReadMe.txt file in the Dart plugin.
* Open intellij-community project, compile it.
  - Open File | Project Structure | Modules | [+] | Import Module,
  select intellij-plugins/Flutter/Flutter-community.iml.
  - In the same Project Structure dialog open the Dependencies tab
  of the community-main module, click [+] at the bottom (Mac) or
  right (Win/Linux) to add a module dependency on the Flutter-community module.
* Install Flutter from [github](https://github.com/flutter/flutter)
and set it up according to its instructions.
* Verify installation from the command line:
  - Connect an android device with USB debugging.
  - `cd <flutter>/examples/hello_world`
  - `flutter start`
