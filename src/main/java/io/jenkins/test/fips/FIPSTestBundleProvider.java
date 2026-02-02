package io.jenkins.test.fips;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class will some default (validated) FIPS configuration for RealJenkinsRule or anything else
 * FIPS BouncyCastle jars (bc-fips.jar, bctls-fips.jar and bcpkix-fips.jar) are in the provided directory.
 * This will set some System properties such :
 * <ul>
 *   <li>-Xbootclasspath with FIPS BouncyCastle jars</li>
 *   <li>java.security.properties to a new temp file configuring the security provider to FIPS BouncyCastle</li>
 *   <li>-Dsecurity.overridePropertiesFile=true</li>
 *   <li>-Djavax.net.ssl.trustStoreType=PKCS12</li>
 *   <li>-Djenkins.security.FIPS140.COMPLIANCE=true</li>
 *   <li>-Dcom.redhat.fips=false</li>
 * </ul>
 */
public interface FIPSTestBundleProvider {

    Logger LOGGER = Logger.getLogger(FIPSTestBundleProvider.class.getName());

    String DEFAULT_VERSION = FIPS1402BC2x.VERSION;

    String SYS_PROP_KEY = "fips.test.bundle.version";

    String ENV_VAR_KEY = "FIPS_TEST_BUNDLE_VERSION";

    /**
     *
     * @return the FIPS specification version managed by this bundle provider
     *          the format will contain BouncyCastle series version as well such {@code fips-140_2-1.x}
     */
    String getVersion();

    /**
     * @return the {@link List} of system properties to add to RealJenkinsRule start
     */
    List<String> getJavaOptions() throws IOException;

    /**
     * @return the {@link List} of libraries to add to {@code -Xbootclasspath/a} option
     */
    List<File> getBootClasspathFiles() throws IOException;

    /**
     * this method will return an instance of {@link FIPSTestBundleProvider} according to the mechanism defined {@link #get(String)}
     * or the default version provided by {@link #DEFAULT_VERSION}
     */
    static FIPSTestBundleProvider get() {
        return get(DEFAULT_VERSION);
    }
    /**
     * Order of precedence to obtain version to use
     * <ul>
     *     <li>env var {@code FIPS_TEST_BUNDLE_VERSION}</li>
     *     <li>system property {@code fips.test.bundle.version}</li>
     *     <li>parameter</li>
     * </ul>
     * @param version the version to use <b>BUT</b> this can be overridden with above mechanism using env var or system property
     * @return instance on {@link FIPSTestBundleProvider} according ot
     */
    static FIPSTestBundleProvider get(String version) {
        String overrideVersion = System.getenv(ENV_VAR_KEY);
        Stream<ServiceLoader.Provider<FIPSTestBundleProvider>> stream =
                ServiceLoader.load(FIPSTestBundleProvider.class).stream();
        if (overrideVersion != null) {
            LOGGER.fine(() -> "use env var to init fips bundle provider: " + overrideVersion);
            return stream.filter(
                            provider -> overrideVersion.equals(provider.get().getVersion()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find FIPS Bundle provider from env var '"
                            + ENV_VAR_KEY + "' with value " + overrideVersion))
                    .get();
        }
        String sysPropVersion = System.getProperty(SYS_PROP_KEY);
        if (sysPropVersion != null) {
            LOGGER.fine(() -> "use sys prop to init fips bundle provider: " + sysPropVersion);
            return stream.filter(
                            provider -> sysPropVersion.equals(provider.get().getVersion()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find FIPS Bundle provider from env var '"
                            + SYS_PROP_KEY + "' with value " + sysPropVersion))
                    .get();
        }
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Cannot find FIPS Bundle provider with version null or empty");
        }
        return stream.filter(provider -> version.equals(provider.get().getVersion()))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Cannot find FIPS Bundle provider from version " + version))
                .get();
    }
}
