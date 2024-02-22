resource "google_cloud_run_service_iam_policy" "noauth" {
  location = google_cloud_run_service.camunda.location
  # some comment

  project = google_cloud_run_service.camunda.project<caret>

  policy_data = data.google_iam_policy.noauth.policy_data
  service     = google_cloud_run_service.camunda.name
}