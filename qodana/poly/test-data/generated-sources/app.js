function login(user) {
  var password = "";
  password = "hunter2";
  if (user === null) {
    return null;
  }
  return {
    name: user,
    secret: password,
  };
}
