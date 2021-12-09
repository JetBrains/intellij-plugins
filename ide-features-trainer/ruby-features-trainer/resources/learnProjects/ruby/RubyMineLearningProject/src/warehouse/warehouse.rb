class Warehouse
  # Fruit name to amount of it in warehouse
  @entry = {}  # Apple, banana, etc...

  def initialize
    FRUITS.each do |fruit|
      @entry[fruit] = 0
    end
  end

  # fruit name from util.FRUITS (mango, apple...)
  def add_fruits(fruit_name, quantity)
    cur_quantity = @entry[fruit_name]
    if !cur_quantity.nil?
      @entry[fruit_name] = cur_quantity + quantity
    else
      raise 'Not found fruit with name: ' + fruit_name
    end
  end

  def take_fruit(fruit_name)
    cur_quantity = @entry[fruit_name]
    if cur_quantity.nil?
      raise 'Not found fruit with name: ' + fruit_name
    elsif cur_quantity.positive?
      @entry[fruit_name] = cur_quantity - 1
      return true
    end

    false
  end

  def print_all_fruits
    @entry.each do |fruit, quantity|
      puts fruit + ': ' + quantity
    end
  end
end
