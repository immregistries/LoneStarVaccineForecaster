package org.immregistries.lonestar.fits;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class FitsFixtureTest {
    @TestFactory
    List<DynamicTest> runsFrozenFitsFixtures() throws Exception {
        FitsPassBaseline baseline = FitsPassBaseline.load();
        List<FitsTestCase> fixtures = FitsFixtures.loadAll();
        List<FitsTestCase> selected = Boolean.getBoolean("fits.full") ? fixtures
                : fixtures.stream()
                        .filter(baseline::contains).collect(Collectors.toList());
        if (!Boolean.getBoolean("fits.full") && selected.size() != baseline.getRequiredPassingCases().size()) {
            throw new IllegalStateException("FITS pass baseline references one or more missing fixtures");
        }
        return selected.stream().map(fixture -> DynamicTest.dynamicTest(fixture.displayName(), () -> {
            FitsComparison.assertMatches(fixture, FitsEngineRunner.run(fixture));
        })).collect(Collectors.toList());
    }
}