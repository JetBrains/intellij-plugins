variable<cursor> "certificate_sans" {
  type = list(string)
  description = "List of subject alternative names"
  default = ["test1", "test2"]
}