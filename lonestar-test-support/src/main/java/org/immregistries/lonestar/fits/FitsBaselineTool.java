package org.immregistries.lonestar.fits;

import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deliberately regenerates the candidate FITS pass baseline after a full-suite
 * review.
 */
public final class FitsBaselineTool {
    private FitsBaselineTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"--update".equals(args[0])) {
            throw new IllegalArgumentException("Usage: FitsBaselineTool --update");
        }
        List<FitsPassBaseline.CaseIdentity> passing = new ArrayList<FitsPassBaseline.CaseIdentity>();
        Path root = repositoryRoot();
        List<FitsTestCase> fixtures = FitsFixtures.loadAll(
                root.resolve("lonestar-test-support/src/test/resources/fits"));
        if (fixtures.isEmpty()) {
            throw new IllegalStateException("No frozen FITS fixtures found");
        }
        for (FitsTestCase fixture : fixtures) {
            try {
                FitsComparison.assertMatches(fixture, FitsEngineRunner.run(fixture));
                passing.add(FitsPassBaseline.CaseIdentity.from(fixture));
            } catch (Exception exception) {
                // A candidate baseline intentionally includes only cases that currently pass.
            } catch (AssertionError error) {
                // Outcome mismatches remain outside the required regression gate until
                // reviewed.
            }
        }
        Collections.sort(passing);
        FitsPassBaseline baseline = new FitsPassBaseline();
        baseline.setRequiredPassingCases(passing);
        Path output = root.resolve("lonestar-test-support/src/test/resources/fits/pass-baseline.json");
        Files.write(output, new GsonBuilder().setPrettyPrinting().create().toJson(baseline)
                .getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + passing.size() + " required passing FITS cases to " + output);
    }

    private static Path repositoryRoot() {
        Path root = Paths.get("").toAbsolutePath();
        if (!Files.isDirectory(root.resolve("lonestar-core"))) {
            root = root.getParent();
        }
        if (root == null || !Files.isDirectory(root.resolve("lonestar-core"))) {
            throw new IllegalArgumentException("Run from the repository root or a direct module directory");
        }
        return root;
    }
}