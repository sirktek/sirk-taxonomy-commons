package no.sirktek.taxonomy.loader;

/** Test-only domain loader over the opt-in fixture. */
class TestDomainLoader extends RdfsTaxonomyLoader {

    @Override
    protected String getNamespace() {
        return "http://taxonomy.sirktek.no/testdomain#";
    }

    @Override
    protected String getResourcePath() {
        return "/taxonomy/test-domain-optin.ttl";
    }
}
