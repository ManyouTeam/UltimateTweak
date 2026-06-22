package cn.superiormc.ultimatetweak.hooks.protection;

public record ProtectionRegionResult(boolean success, boolean alreadyExists, String message) {

    public static ProtectionRegionResult created() {
        return new ProtectionRegionResult(true, false, "created");
    }

    public static ProtectionRegionResult alreadyExistsResult() {
        return new ProtectionRegionResult(true, true, "already exists");
    }

    public static ProtectionRegionResult unsupported(String pluginName) {
        return new ProtectionRegionResult(false, false,
                "Creating protection regions is not supported for " + pluginName + " yet.");
    }

    public static ProtectionRegionResult failed(String message) {
        return new ProtectionRegionResult(false, false, message);
    }

    public static ProtectionRegionResult failed(Throwable throwable) {
        return failed(throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage());
    }
}
