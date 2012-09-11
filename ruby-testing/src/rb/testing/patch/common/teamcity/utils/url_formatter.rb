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

# @author: Roman.Chernyatchik

module Rake
  module TeamCity
    module Utils
      module UrlFormatter
        # @Nullable
        def location_from_link(path, line_str)
          return nil if (path.nil? || line_str.nil?)
          "file://#{path}:#{line_str}"
        end

        # @Nullable
        def location_from_ruby_qualified_name(qualified_name)
          return nil if (qualified_name.nil?)
          "ruby_qn://#{qualified_name}"
        end
      end
    end
  end
end