# Running `fileupload` in Docker: gcloud Credentials Fix

Date: 2026-07-06

## Summary

`docker run fileupload` failed on startup because the app was configured to read
Google Cloud Application Default Credentials (ADC) from a path that only exists
on the host machine. Once that was fixed, a second, related failure appeared:
a file-permission mismatch between the host-owned credentials file and the
non-root user the container runs as. Both are fixed below, and the fix was
verified by actually starting the container end-to-end.

## Background

`spring-cloud-gcp-starter-storage` is on the classpath (`pom.xml`), so Spring
Boot auto-configures a `GoogleCredentials` bean on startup
(`GcpContextAutoConfiguration`). It reads the credentials file location from:

```
spring.cloud.gcp.credentials.location
```

This was hardcoded in `src/main/resources/application.properties` to:

```
spring.cloud.gcp.credentials.location=file:/home/admini/.config/gcloud/application_default_credentials.json
```

The Docker image is built with the Spring Boot Maven plugin's Cloud Native
Buildpacks integration (`spring-boot:build-image`, configured in `pom.xml`
under `<image><name>fileupload</name></image>`), producing local images
`fileupload:latest` and `fileupload:0.0.1-SNAPSHOT`.

## Issue 1 — hardcoded host path doesn't exist in the container

### Reproduction

```
docker run --rm -p 8080:8080 fileupload:latest
```

Result: application context fails to start.

```
Failed to instantiate [com.google.api.gax.core.CredentialsProvider]:
Factory method 'googleCredentials' threw exception with message:
/home/admini/.config/gcloud/application_default_credentials.json (No such file or directory)
```

The path `/home/admini/.config/gcloud/...` is a path on the **host**. Nothing
copies or mounts that file into the container image, so inside the container
it simply doesn't exist.

### Fix

Made the credentials location overridable via an environment variable, with
the previous hardcoded path kept only as the default fallback (so existing
local/non-Docker runs are unaffected):

`src/main/resources/application.properties`:

```properties
spring.application.name=fileupload

spring.cloud.gcp.credentials.location=${GCP_CREDENTIALS_LOCATION:file:/home/admini/.config/gcloud/application_default_credentials.json}
```

This lets a container-friendly path be supplied at `docker run` time via
`-e GCP_CREDENTIALS_LOCATION=...`, combined with a volume mount, instead of
requiring a code change or an image with the host's username baked in.

Rebuilt the image after the change:

```
./mvnw spring-boot:build-image -DskipTests
```

## Issue 2 — permission denied reading the mounted credentials file

### Reproduction

After fixing the path and mounting the real ADC file:

```
docker run -d --name fileupload-real -p 8080:8080 \
  -e GCP_CREDENTIALS_LOCATION=file:/tmp/keys/application_default_credentials.json \
  -v "$HOME/.config/gcloud/application_default_credentials.json:/tmp/keys/application_default_credentials.json:ro" \
  fileupload:latest
```

Result: a *different* error appeared:

```
Factory method 'googleCredentials' threw exception with message:
/tmp/keys/application_default_credentials.json (Permission denied)
```

Cause: the ADC file on disk is `-rw-------` (mode 600), owned by the host
user. Buildpack-built images run as a fixed non-root UID/GID
(`1002:1001` for this image — verified via
`docker inspect fileupload:latest --format '{{.Config.User}}'`). That UID has
no read access to a file owned by a different UID with mode 600.

### Fix

Run the container as the host user's UID/GID via `--user`, so the process
reading the mounted file matches the file's owner permissions. This avoids
loosening the permissions on the real credentials file (e.g. `chmod 644`),
which would make it world-readable on the host — not an acceptable trade-off
for a real secret.

```
docker run -d --name fileupload --user "$(id -u):$(id -g)" \
  -p 8080:8080 \
  -e GCP_CREDENTIALS_LOCATION=file:/tmp/keys/application_default_credentials.json \
  -v "$HOME/.config/gcloud/application_default_credentials.json:/tmp/keys/application_default_credentials.json:ro" \
  fileupload:latest
```

## Verification

1. Rebuilt image, confirmed a new image ID was produced.
2. Ran the container with a dummy (non-real) ADC-shaped JSON file mounted at
   the env-var-driven path — confirmed the original `FileNotFoundException`
   was gone and the app logged
   `Default credentials provider for user dummy-client-id...` and started
   Tomcat successfully. `curl http://localhost:8080/api/upload` returned
   `HTTP 200`.
3. Ran the container with the user's **real** ADC file mounted, without
   `--user`, and reproduced the "Permission denied" failure described above.
4. Re-ran with `--user "$(id -u):$(id -g)"` — startup log showed
   `Default credentials provider for user 764086051850-...apps.googleusercontent.com`
   (i.e., the real Google OAuth client from the host's ADC), Tomcat started,
   and `curl http://localhost:8080/api/upload` returned `HTTP 200`.
5. Stopped and removed all test containers (`fileupload-test2`,
   `fileupload-real`) — no leftover containers from this session
   (`docker ps -a --filter name=fileupload` returned empty).

## How to run it going forward

```
docker run -d --name fileupload --user "$(id -u):$(id -g)" \
  -p 8080:8080 \
  -e GCP_CREDENTIALS_LOCATION=file:/tmp/keys/application_default_credentials.json \
  -v "$HOME/.config/gcloud/application_default_credentials.json:/tmp/keys/application_default_credentials.json:ro" \
  fileupload:latest
```

- Mount is read-only (`:ro`) — the container never needs to write to the ADC
  file.
- `--user "$(id -u):$(id -g)"` only works when the container's runtime user
  needs read access to a host-owned file; it does not require the file to be
  world-readable.
- If the ADC token expires, refresh it on the host with
  `gcloud auth application-default login` — no container or image change is
  needed since the file is bind-mounted, not baked into the image.

## Files changed

- `src/main/resources/application.properties` — credentials location now
  reads from `GCP_CREDENTIALS_LOCATION` env var with the old hardcoded path
  as fallback default.

## Out of scope / not touched

- `pom.xml` build-image config and `FileuploadApplicationTests.java` changes
  that were already present in the working tree before this session were left
  as-is; they're unrelated to the credentials issue.
- No Dockerfile exists in this repo; the image is built entirely through the
  Spring Boot Maven plugin's Cloud Native Buildpacks integration
  (`spring-boot:build-image`), not a hand-written Dockerfile.
