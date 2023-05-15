resource "aws_glue_catalog_table" "aaa" {
  storage_descriptor {
    ser_de_info {
      parameters = {
        serialization.format = "1"
      }
    }
  }
}