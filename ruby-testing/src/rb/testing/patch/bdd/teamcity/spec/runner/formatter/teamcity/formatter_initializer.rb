# Copyright 2000-2014 JetBrains s.r.o.
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

# @author Roman.Chernyatchik

module Spec
  module Runner
    module Formatter
      # Let's determine current RSpec gem version
      # Formatter is required from some other RSpec code, so let's check some
      # marker classed in object space

      if defined? ::RSpec::Core::Version::STRING
        if ::RSpec::Core::Version::STRING.split('.')[0] == '2'
          # rspec >= 2.x
          require 'rspec/core/formatters/base_formatter'
          RSPEC_VERSION_2 = true
          RSPEC_VERSION_3 = false
        else
          # rspec >= 3.x
          require 'rspec/core/formatters/base_formatter'
          RSPEC_VERSION_2 = false
          RSPEC_VERSION_3 = true
        end
      elsif defined? ::Spec::VERSION::STRING
        # rspec 1.x
        require 'spec/runner/formatter/base_formatter'
        RSPEC_VERSION_2 = false
        RSPEC_VERSION_3 = false
      else
        # some unsupported version. Let's assume that it is something like rspec 2.x
        # such require may force gem activation
        require 'rspec/core/formatters/base_formatter'
        RSPEC_VERSION_2 = true
        RSPEC_VERSION_3 = false
      end
    end
  end
end