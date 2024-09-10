# fips-bundle-test

## Introduction

This library will bundle known validated BouncyCastle FIPS provider, will provide java options and bootclasspath files.

## Getting started

Just add this to your usage of [RealJenkinsRule](https://javadoc.jenkins.io/component/jenkins-test-harness/org/jvnet/hudson/test/RealJenkinsRule.html)

```java
    @Rule public RealJenkinsRule rr = new RealJenkinsRule()
            .withFIPSEnabled();

```

To use a specific version of the bundle

```java
    @Rule public RealJenkinsRule rr = new RealJenkinsRule()
            .withFIPSEnabled(FIPSTestBundleProvider.get("some version"));

```

If you need (such PCT context which need to use a fixed version) to override the version defined in the test code, the version can be overriden using:

- Env var: `ENV_VAR_KEY`
- System property: `fips.test.bundle.version`


## Issues

Report issues and enhancements in the [Jenkins issue tracker](https://issues.jenkins.io/).

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

