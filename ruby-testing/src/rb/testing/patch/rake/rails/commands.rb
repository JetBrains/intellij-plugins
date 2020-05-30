# This monkey patch is necessary to initialize RubyMine output formatter - to build tree of tests
require 'rubymine_test_framework_initializer'
include RubyMineTestFrameworkInitializer

# Standard content of commands.rb
require "rails/command"


aliases = {
  "g"  => "generate",
  "d"  => "destroy",
  "c"  => "console",
  "s"  => "server",
  "db" => "dbconsole",
  "r"  => "runner",
  "t"  => "test"
}

command = ARGV.shift
command = aliases[command] || command

Rails::Command.invoke command, ARGV