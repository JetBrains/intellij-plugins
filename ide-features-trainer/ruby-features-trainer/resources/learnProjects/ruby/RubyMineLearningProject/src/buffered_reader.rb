
class BufferedReader
  def initialize(input_stream, max_buffer_size = 1024)
    @input_stream = input_stream
    @max_buffer_size = max_buffer_size
  end

  @buffer = []

  def read(n = 1)
    # returns n bytes from buffer or try read max_buffer_size bytes
    # from input stream and return n bytes
  end

  def read_line
    # returns one line from input stream as string
  end

  def lines
    # returns all lines from input stream as list of strings
  end
end