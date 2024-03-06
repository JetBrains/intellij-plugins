resource "google_storage_bucket" "test" {
  name     = "test"
  location = "ASIA-NORTHEAST3"
  autoclass {
    enabled = true
  }
}