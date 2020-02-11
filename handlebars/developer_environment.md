# Setting up a development environment

## Import the project
* Fork this project, clone it to your machine
* Launch IDEA and `File -> New -> Project from Existing Sources...` the `handlebars` directory as a Gradle project
* At this point you should be ready to go!  Launch the "runIde" Gradle task and start hacking.

## Tips
* **Regenerating the lexer:** the heart of the plugin is the grammar defined in `/com/dmarcotte/handlebars/parsing/handlebars.flex`.  If you modify this grammar, you need to run `/com/dmarcotte/handlebars/parsing/jflex_generate.sh` to regenerate the class (`com.dmarcotte.handlebars.parsing._HbLexer`) which does the actual lexing in the plugin.
* **Test against any JetBrains IDE:** you can run your plugin build inside of any IDEA-platform-based IDE (the Ultimate Edition, Rubymine, PhpStorm, etc.) by setting up a Plugin SDK pointing at the installation location of the IDE you want to test against.  You will not have the full source for most of these, but this comes in handy when back-porting features, testing against an IDEA EAP, or troubleshooting IDE-specific problems.
    * *Note:* you do not need a license for any of the products you want to test against.  The plugin SDK will let you use a trial license, and you can "renew" this any time by deleting the **Sandbox Home** folder defined in `Project Structure -> SDKs`
![ProjectSettings](markdown_images/sdk_setup_2.png)
    * There may be compilation errors in the test suite against non-IDEA SDKs since not all the products ship with the test framework.  To work around this, temporarily "Unmark as Test Source Root" `test/src`.
* **Hack on any open source plugin:** these setup instructions should apply for just about any plugin, so now you should be able to easily explore the code of [all of your favorite open source plugins](http://blogs.jetbrains.com/idea/2012/10/check-out-more-than-200-open-source-plugins/).
  * For plugins which do not have a `.idea` project in their source, you can consult the [legacy version of these instructions](https://github.com/dmarcotte/idea-handlebars/blob/master/developer_environment.md) for pointers on how to set up a project.

## Problems with these instructions?
Please let us know! The easy way to suggest improvements, point out gaps, or even just ask questions is to simply comment on one the commits for this file.
