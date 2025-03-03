# Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

require 'teamcity/rakerunner_consts'

module Rake
  module TeamCity
    module FormatterUtil
      # to_s clashes with the :default format, see https://guides.rubyonrails.org/v7.0/7_0_release_notes.html#active-support-deprecations
      def format_time(t)
        # copies the `to_s` implementation for `Time`
        t.strftime("%Y-%m-%d %H:%M:%S %z")
      end
    end
  end
end
