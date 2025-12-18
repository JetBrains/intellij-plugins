# Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

require File.dirname(__FILE__) + '/runner_settings'
require 'teamcity/utils/runner_utils'

files = ENV["INTELLIJ_IDEA_RUN_CONF_TEST_FILE_PATH"] # separated by "||" filenames
if files.nil? || files.length == 0
   raise Exception.new("A test file must be provided")
end

files.split("||").each do |file|
   require file
end

# Minitest 6.0 makes plugin loading opt-in, which means the reporter file isn't `require`-d anymore, so we have to require it manually
if ::Rake::TeamCity::RunnerUtils.use_minitest?
  require 'minitest/rm_reporter_plugin'
end
