docker run -d --name fileupload --user "$(id -u):$(id -g)" \
  -p 8080:8080 \
  -e GCP_CREDENTIALS_LOCATION=file:/tmp/keys/application_default_credentials.json \
  -v "$HOME/.config/gcloud/gcs-signer.json:/tmp/keys/application_default_credentials.json:ro" \
    fileupload:latest