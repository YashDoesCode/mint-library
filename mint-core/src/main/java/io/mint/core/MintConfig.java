package io.mint.core;

/**
 * Global configuration for the Mint snapshot testing library.
 *
 * <p>In CI never set {@code MINT_UPDATE}. Locally: {@code mvn test -Dmint.update=true}.
 */
public final class MintConfig {

    /**
     * Flag indicating whether snapshots should be updated automatically on test runs.
     */
    public static final boolean UPDATE = readUpdateFlag();

    private MintConfig() {
    }

    private static boolean readUpdateFlag() {
        String sysProp = System.getProperty("mint.update");
        if ("true".equalsIgnoreCase(sysProp)) {
            return true;
        }
        String envVal = System.getenv("MINT_UPDATE");
        return "true".equalsIgnoreCase(envVal);
    }
}
