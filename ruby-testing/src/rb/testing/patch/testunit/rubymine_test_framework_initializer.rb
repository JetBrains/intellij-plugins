# this file should be dropped after we stop supporting minitest < 5.x (2013 year, 1% of users at 2020)
minitest_5_or_newer = false
Gem::Specification::find_all_by_name('minitest').each do |gem|
  if Gem::Version.new(gem.version) >= Gem::Version.new('5.0')
    minitest_5_or_newer = true
    break
  end
end

unless minitest_5_or_newer
  begin
    require "minitest/autorun"
  rescue LoadError
    # it's ok in case of minitest 1.4
  end

  begin
    require "minitest/unit"
    begin
      require 'minitest/reporters'
    rescue LoadError
      # ok, there is no minitest-reporters gem
    end
    require 'minitest/rm_reporter_plugin'
  rescue LoadError
    begin
      require 'test/unit'
    rescue LoadError
      puts "Neither minitest nor test-unit libraries available"
    end
  end
end