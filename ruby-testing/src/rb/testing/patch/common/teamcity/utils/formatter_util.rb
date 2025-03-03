# Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

require 'teamcity/rakerunner_consts'

module Rake
  module TeamCity
    module FormatterUtil
      # to_s clashes with :default format, see https://guides.rubyonrails.org/v7.0/7_0_release_notes.html#active-support-deprecations
      def stringify_time(t)
        unless Object.const_defined?("Time::DATE_FORMATS") and Time::DATE_FORMATS.has_key? :default
          return t.to_s
        end
        default_format = Time::DATE_FORMATS[:default]
        Time::DATE_FORMATS.delete(:default)
        time_string = t.to_s
        Time::DATE_FORMATS[:default] = default_format
        time_string
      end
    end
  end
end
