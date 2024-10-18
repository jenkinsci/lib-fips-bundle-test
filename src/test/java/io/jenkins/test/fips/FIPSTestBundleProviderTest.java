package io.jenkins.test.fips;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class FIPSTestBundleProviderTest {

    @Test
    public void serviceLoaderEntries() {

        List<FIPSTestBundleProvider> bundles = ServiceLoader.load(FIPSTestBundleProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableList());
        assertThat(bundles, not(empty()));
        assertThat(bundles, hasSize(2));
    }

    @Test
    public void get_test() {
        assertThrows(IllegalArgumentException.class, () -> FIPSTestBundleProvider.get("foobar"));
        assertThrows(IllegalArgumentException.class, () -> FIPSTestBundleProvider.get(null));
        assertThat(FIPSTestBundleProvider.get(), notNullValue());
    }

    @Test
    public void get_fips1401x() throws Exception {
        FIPSTestBundleProvider provider = FIPSTestBundleProvider.get(FIPS1402BC1x.VERSION);
        assertThat(provider, notNullValue());
        assertThat(provider.getVersion(), is(FIPS1402BC1x.VERSION));
        assertThat(provider.getBootClasspathFiles(), hasSize(3));
    }

    @Test
    public void get_fips1402x() throws Exception {
        FIPSTestBundleProvider provider = FIPSTestBundleProvider.get(FIPS1403BC2x.VERSION);
        assertThat(provider, notNullValue());
        assertThat(provider.getVersion(), is(FIPS1403BC2x.VERSION));
        assertThat(provider.getBootClasspathFiles(), hasSize(3));
    }
}
