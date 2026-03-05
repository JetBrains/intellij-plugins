require_relative 'rm_diff_patch'

begin
  require 'minitest'
rescue LoadError
  return
end

Minitest.load_plugins