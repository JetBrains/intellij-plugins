require 'yaml'
CLASS_DUMP = File.expand_path(File.dirname(__FILE__)) + "/class-dump"
OTOOL = '/usr/bin/otool'

def calc_inheritance_info(output)
  result = {}
  output.each_line do |line|
    if line =~ /@interface\s*(\w*)\s?:\s?(\w*)(\s?.*)?$/
      result[$1] = $2
    end
  end
  result
end

def calc_inheritance_info_ios(framework)
  output = `"#{CLASS_DUMP}" #{framework}`
  calc_inheritance_info(output)
end

def process_directory(headers, result)
  Dir.entries(headers).each do |header|
    next if header == '.' || header == '..'
    if header =~ /.*\.h/
      full_header = File.join(headers, header)
      begin
        result.merge!(calc_inheritance_info(File.open(full_header).read))
      rescue Exception => e
        puts "#{full_header}: #{e}"
      end
    else
      process_directory(header, result)
    end
  end if File.directory?(headers)
end

def calc_inheritance_info_osx(framework)
  result = {}
  headers = File.join(framework, '/Headers')
  process_directory(headers, result)
  result
end

def calc_framework_dependencies(framework, name, osx)
  output = `#{OTOOL} -L #{framework}`
  result = {}
  pattern = osx ? /\s*\/System\/Library\/Frameworks\/.*framework\/Versions\/A\/(\S*)\s*.*/ :
                  /\s*\/System\/Library\/Frameworks\/.*framework\/(\S*)\s*.*/
  output.each_line do |line|
    if line =~ pattern
      list = result[name]
      list ||= []
      list << $1 unless $1.include? "/"
      list.uniq!
      list.sort!
      result[name] = list
    end
  end
  result
end

sdk_dir = ARGV[0]
frameworks_dir = sdk_dir + "/System/Library/Frameworks/"
inheritance_info = {}
dependency_info = {}
osx = !ENV['OSX'].nil?
Dir.entries(frameworks_dir).each do |subdir|
  if subdir =~ /.*\.framework/
    name = subdir[0..-11]
    framework = File.join(frameworks_dir, subdir, name)
    if File.exists?(framework)
      inheritance_info.merge!(osx ? calc_inheritance_info_osx(File.join(frameworks_dir, subdir)) :
                                    calc_inheritance_info_ios(framework))
      dependency_info.merge!(calc_framework_dependencies(framework, name, osx))
    end
  end
end
File.open(ARGV[1], "w") do |f|
  f.write(inheritance_info.to_yaml)
end

File.open(ARGV[2], "w") do |f|
  f.write(dependency_info.to_yaml)
end
