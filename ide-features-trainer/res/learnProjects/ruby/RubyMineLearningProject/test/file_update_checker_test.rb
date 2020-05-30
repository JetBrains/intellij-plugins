# frozen_string_literal: true

require "abstract_unit"
require "file_update_checker_shared_tests"

class FileUpdateCheckerTest < ActiveSupport::TestCase
  include FileUpdateCheckerSharedTests

  def new_checker(files = [], dirs = {}, &block)
    ActiveSupport::FileUpdateChecker.new(files, dirs, &block)
  end

  def wait
    # noop
  end

  def touch(files)
    sleep 1 # let's wait a bit to ensure there's a new mtime
    super
  end
end
