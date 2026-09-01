package org.immregistries.lonestar.fits;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class FitsFixtureTest {
    @TestFactory
    List<DynamicTest> runsFrozenFitsFixtures() throws Exception {
        return FitsFixtures.loadAll().stream().map(fixture -> DynamicTest.dynamicTest(fixture.displayName(), () -> {
            FitsComparison.assertMatches(fixture, FitsEngineRunner.run(fixture));
        })).collect(Collectors.toList());
    }
}