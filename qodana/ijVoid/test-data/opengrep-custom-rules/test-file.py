# Test file with various issues

def connect_to_db():
    # This should trigger hardcoded-password rule
    password = "admin123"
    return connect(password)

def get_user(user_id):
    # This should trigger sql-string-concat rule
    query = execute("SELECT * FROM users WHERE id = " + user_id)
    return query

def debug_function(value):
    # This should trigger print-statement rule
    print(value)
    return value * 2

def valid_function():
    result = calculate_something()
    return result
