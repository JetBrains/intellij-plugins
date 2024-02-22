resource "google_cloud_run_service" "oathkeeper" {
  name       = "oathkeeper"
  location   = local.region
  <caret>template
  {
    spec {
      # Use locked down Service Account
      service_account_name = google_service_account.oathkeeper.email
      containers {
        image = null_resource.oathkeeper_image.triggers.image
        args  = ["--config", "/config.yaml"]
        env {
          name  = "nonce"
          value = filesha256("${path.module}/rules.template.yml") # Force refresh on rule change
        }
        env {
          name  = "ACCESS_RULES_REPOSITORIES"
          # storage.cloud.google.com domain serves content via redirects which is does not work ATM https://github.com/ory/oathkeeper/issues/425
          value = "https://storage.googleapis.com/${google_storage_bucket.config.name}/${google_storage_bucket_object.rules.name}"
        }
        env {
          name  = "LOG_LEVEL"
          value = "debug"
        }
      }
    }
  }
  depends_on = [google_storage_bucket_object.rules]

  traffic {
    percent         = 100
    latest_revision = true
  }
}