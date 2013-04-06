# [Handlebars](http://handlebarsjs.com/)/[Mustache](http://mustache.github.com/) template plugin for Jetbrains IDEs

IDEA-Handlebars adds support for [Handlebars](http://handlebarsjs.com/) and [Mustache](http://mustache.github.com/) templates to IDEs based on the Intellij IDEA platform (IntelliJ IDEA, IDEA Community Edition, RubyMine, PhpStorm, WebStorm, PyCharm, AppCode).

![Handlebars/Mustache editing screenshot](https://raw.github.com/dmarcotte/idea-handlebars/master/markdown_images/editor.png "Handlebars/Mustache editing")

[Jetbrains plugin repository page](http://plugins.intellij.net/plugin/?idea&pluginId=6884)

## _NEW:_ Updated parsing, improved comments, Darcula support
The new release includes the following:
* handles the [latest Handlebars features](https://github.com/dmarcotte/idea-handlebars/pull/46)
* adds [comment folding](https://github.com/dmarcotte/idea-handlebars/pull/49)
* enables a [consistent commenter when editing](https://github.com/dmarcotte/idea-handlebars/pull/47) Handlebars files
* updates look and feel to integrate properly with alternate themes, including Darcula
    * *NOTE:* requires your IDE's build number to be greater than `128.18` (for IDEA, this means version at least `12.1`.  Other IDEs should catch up soon)

## Installing
* To install the latest release (and get automatic updates), install this plugin using your IDE's plugin manager:
  * In Settings->Plugins, choose "Browse repositories".  Find "Handlebars/Mustache" on the list, right-click, and select "Download and Install"

## Features
* [Syntax error inspections](https://raw.github.com/dmarcotte/idea-handlebars/master/markdown_images/editor.png)
* [Configurable syntax highlighting](https://raw.github.com/dmarcotte/idea-handlebars/master/markdown_images/highlight_config.png)
* [Template formatter](https://raw.github.com/dmarcotte/idea-handlebars/master/markdown_images/formatter.png)
* Code folding for mustache blocks and comments
* Auto-insert of closing tags
* Matched mustache pair highlighting
* Full highlighting, code completion, inspections, formatting and commenting for the content in your templates (by default HTML, configurable in ["Settings->Template Data Languages"](https://raw.github.com/dmarcotte/idea-handlebars/master/markdown_images/custom_langs_by_project.png) and ["Settings->File Types"](https://raw.github.com/dmarcotte/idea-handlebars/master/markdown_images/custom_langs_by_filename_pattern.png))
* By default, files matching `*.handlebars`, `*.hbs` or `*.mustache` are handled by this plugin.  Configure other file patterns in `Settings->File Types`

## Future directions
* See [the pulls](https://github.com/dmarcotte/idea-handlebars/pulls) and [issues](https://github.com/dmarcotte/idea-handlebars/issues) for a preview of in-progress and planned features

## Contributing
Contributions welcome!

There's a variety of ways you can help this project out:

* Contributing without coding
    * [File issues](https://github.com/dmarcotte/idea-handlebars/issues/new) for bugs, suggestions and requests.  This is a great and easy way to help out
    * Fluent in multiple languages?  [Provide a translation!](https://github.com/dmarcotte/idea-handlebars/issues/21)
* Contributing code
    * Have a gander at the [contributor guidelines](https://github.com/dmarcotte/idea-handlebars/blob/master/contributing.md)
    * The [developer setup instructions](https://github.com/dmarcotte/idea-handlebars/blob/master/developer_environment.md) should get you up and running in no time
    * Look at [contrib-welcome issues](https://github.com/dmarcotte/idea-handlebars/issues?direction=desc&labels=contrib-welcome&page=1&sort=created&state=open) for ideas, or [submit an idea of your own](https://github.com/dmarcotte/idea-handlebars/issues/new)
