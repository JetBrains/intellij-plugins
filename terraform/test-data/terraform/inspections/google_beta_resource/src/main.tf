terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 6.21.0"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = ">= 6.21.0"
    }
  }
}

// This is a valid resource
resource "google_project_service_identity" "default" {
  service = "compute.googleapis.com"
}

// This is an invalid resource
resource "google-beta_project_service_identity" "default" {
  service = "compute.googleapis.com"
}