resource "google_cloud_run_service_iam_policy" "noauth" {
  location = google_cloud_run_service.camunda.location
  # some comment

  policy_data = data.google_iam_policy.noauth.policy_data

  project = google_cloud_run_service.camunda.project<caret>
  service     = google_cloud_run_service.camunda.name
}