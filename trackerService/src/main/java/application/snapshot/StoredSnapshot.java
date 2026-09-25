package application.snapshot;

public record StoredSnapshot(String versionToken, ResourceSnapshot snapshot) {
}
