package io.jenkins.test.fips;

import org.apache.commons.io.IOUtils;
import org.kohsuke.MetaInfServices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@MetaInfServices(FIPSTestBundleProvider.class)
public class FIPS1403BC2x implements FIPSTestBundleProvider {

    public static final String VERSION = "fips-140_3-2.x";

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public List<String> getJavaOptions() throws IOException {
        return List.of(
                "-Dsecurity.useSystemPropertiesFile=false",
                // please note == is not a typo, but it makes our file completely override the jvm security file
                "-Djava.security.properties==" + writeFIPSJavaSecurityFile().toUri(),
                "-Dorg.bouncycastle.fips.approved_only=true",
                "-Djavax.net.ssl.trustStoreType=PKCS12",
                "-Djenkins.security.FIPS140.COMPLIANCE=true");
    }

    @Override
    public List<File> getBootClasspathFiles() throws IOException {
        return List.of(
                extractJar("bc-fips.jar").toFile(),
                extractJar("bcpkix-fips.jar").toFile(),
                extractJar("bcutil-fips.jar").toFile(),
                extractJar("bctls-fips.jar").toFile());
    }

    private Path extractJar(String jarName) throws IOException {
        // unzip jar files to a temporary directory
        URL url = Thread.currentThread().getContextClassLoader().getResource(VERSION + "/" + jarName);
        Path bcFips = Files.createTempFile(jarName, "jar");
        bcFips.toFile().deleteOnExit();
        try (OutputStream os = Files.newOutputStream(bcFips)) {
            IOUtils.copy(url.openStream(), os);
        }
        return bcFips;
    }

    private Path writeFIPSJavaSecurityFile() throws IOException {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new IllegalArgumentException("Cannot find java.home property");
        }
        Path javaSecurity = Paths.get(javaHome, "conf", "security", "java.security");
        Properties properties = new Properties();
        Path securityFile = Files.createTempFile("java", ".security");
        securityFile.toFile().deleteOnExit();
        try (InputStream inputStream = Files.newInputStream(javaSecurity);
                OutputStream outputStream = Files.newOutputStream(securityFile)) {
            properties.load(inputStream);
            properties.keySet().removeIf(o -> ((String) o).startsWith("security.provider"));
            properties.put(
                    "security.provider.1",
                    "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider C:HYBRID;ENABLE{All};");
            properties.put(
                    "security.provider.2", "org.bouncycastle.jsse.provider.BouncyCastleJsseProvider fips:BCFIPS");
            properties.put("security.provider.3", "sun.security.provider.Sun");
            properties.put(
                    "fips.provider.1",
                    "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider C:HYBRID;ENABLE{All};");
            properties.put("fips.provider.2", "org.bouncycastle.jsse.provider.BouncyCastleJsseProvider fips:BCFIPS");
            properties.put("keystore.type", "BCFKS");
            // properties.put("securerandom.strongAlgorithms", "PKCS11:SunPKCS11-NSS-FIPS");
            properties.put("ssl.KeyManagerFactory.algorithm", "PKIX");
            properties.put("fips.keystore.type", "BCFKS");
            properties.store(outputStream, "");
        }
        return securityFile;
    }
}
