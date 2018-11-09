#!/bin/sh

archive_file=/tmp/remote_sdk_files.tar.gz
host_volume_path=/tmp/ruby_sdk_gems/remote_sdk_files.tar.gz
{
for gempath in "$@"
do
    find $gempath -name '*.rb' -o -name '*.gemspec' -o ! -name '*.*' -type f;
done
} | tar -cvzf $archive_file --files-from -
cp $archive_file $host_volume_path
                                           