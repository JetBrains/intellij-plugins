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
# @date: 22:36:41

############## STDOUT, STDERR Extension #############
# <<
def STDOUT.<<(text)
  Rake::TeamCityApplication.send_captured_stdout(text)
  self
end

def STDERR.<<(text)
  Rake::TeamCityApplication.send_captured_stderr(text.to_s)
  self
end

# writ_nonmblock
def STDOUT.write_nonblock(text)
  str = text.to_s
  Rake::TeamCityApplication.send_captured_stdout(str)
  str.length
end

def STDERR.write_nonblock(text)
  str = text.to_s
  Rake::TeamCityApplication.send_captured_stderr(str)
  str.length
end

#write
def STDOUT.write(text)
  str = text.to_s
  Rake::TeamCityApplication.send_captured_stdout(str)
  str.length
end

def STDERR.write(text)
  str = text.to_s
  Rake::TeamCityApplication.send_captured_stderr(str)
  str.length
end

#puts
def STDERR.puts(*args)
  teamcity_puts(args)
end

def STDOUT.puts(*args)
  teamcity_puts(args)
end

#############  Object extension #############################
class Object
  def puts(*args)
    STDOUT.puts(args)
  end

  def printf(s, *args)
    puts(sprintf(s, *args))
    nil
  end
end

################  Kernel extension #############################
module Kernel
  def warn(text)
    if $VERBOSE
      Rake::TeamCityApplication.send_captured_warning(text)
    end
    nil
  end
end

###############################################################
###############################################################
private
def teamcity_puts(*args)
  if args.empty?
    self.write("")
  else
    args.each { |arg| self.write(arg) }
  end
  nil
end
