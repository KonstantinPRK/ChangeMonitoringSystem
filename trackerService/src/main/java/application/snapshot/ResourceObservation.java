package application.snapshot;

public record ResourceObservation(
        String versionToken,
        boolean notModified,
        ResourceSnapshot snapshot
) {
    public static ResourceObservation unchanged(String versionToken) {
        return new ResourceObservation(versionToken, true, null);
    }


    public static ResourceObservation changed(String versionToken, ResourceSnapshot snapshot) {
        return new ResourceObservation(versionToken, false, snapshot);
    }
}
