package org.immregistries.lonestar.fits;

import com.google.gson.Gson;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FitsFixtures {
    private FitsFixtures() {
    }

    public static List<FitsTestCase> loadAll() throws Exception {
        URL resource = FitsFixtures.class.getClassLoader().getResource("fits");
        if (resource == null || !"file".equals(resource.getProtocol())) {
            return Collections.emptyList();
        }
        List<Path> files = new ArrayList<Path>();
        try (java.util.stream.Stream<Path> paths = Files.walk(Paths.get(resource.toURI()))) {
            paths.filter(path -> path.toString().endsWith(".json")).forEach(files::add);
        }
        Collections.sort(files);
        Gson gson = new Gson();
        List<FitsTestCase> fixtures = new ArrayList<FitsTestCase>();
        for (Path file : files) {
            FitsTestCase fixture = gson.fromJson(new String(Files.readAllBytes(file), "UTF-8"), FitsTestCase.class);
            if (fixture.getUid() == null || fixture.getBirthDate() == null || fixture.getEvalDate() == null) {
                throw new IllegalArgumentException("Incomplete FITS fixture " + file);
            }
            fixtures.add(fixture);
        }
        return fixtures;
    }
}