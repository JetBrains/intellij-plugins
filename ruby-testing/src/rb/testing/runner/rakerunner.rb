# Copyright 2000-2012 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @date: 07.06.2007

# TeamCity build server uses this file for running rake tasks
######################################################################
STDOUT.sync=true
STDERR.sync=true

######################################################################
# ENV["idea.rake.debug.log.path"] = File.expand_path(File.dirname(__FILE__) + "/../..")
######################################################################

require 'rubygems'
version = "> 0"
if ARGV.first =~ /^_(.*)_$/ and Gem::Version.correct? $1 then
  version = $1
  ARGV.shift
end

gem 'rake', version
load  File.expand_path(File.dirname(__FILE__) + '/rake_ext.rb')
#################################################################
#################################################################
Rake.application.run