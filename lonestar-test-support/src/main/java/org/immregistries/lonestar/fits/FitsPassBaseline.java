package org.immregistries.lonestar.fits;

import com.google.gson.Gson;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The reviewed FITS cases that must continue to pass in ordinary builds. */
public final class FitsPassBaseline {
    private List<CaseIdentity> requiredPassingCases = new ArrayList<CaseIdentity>();

    public static FitsPassBaseline load() throws Exception {
        InputStream input = FitsPassBaseline.class.getClassLoader().getResourceAsStream("fits/pass-baseline.json");
        if (input == null) {
            throw new IllegalStateException("Missing fits/pass-baseline.json");
        }
        try {
            byte[] bytes = new byte[0];
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            bytes = output.toByteArray();
            return new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), FitsPassBaseline.class);
        } finally {
            input.close();
        }
    }

    public List<CaseIdentity> getRequiredPassingCases() {
        return requiredPassingCases;
    }

    public boolean contains(FitsTestCase fixture) {
        return identities().contains(CaseIdentity.from(fixture));
    }

    public void setRequiredPassingCases(List<CaseIdentity> requiredPassingCases) {
        this.requiredPassingCases = requiredPassingCases;
    }

    public Set<CaseIdentity> identities() {
        return new HashSet<CaseIdentity>(requiredPassingCases);
    }

    public static final class CaseIdentity implements Comparable<CaseIdentity> {
        private String testPlanId;
        private String groupName;
        private String uid;

        public static CaseIdentity from(FitsTestCase fixture) {
            CaseIdentity identity = new CaseIdentity();
            identity.testPlanId = fixture.getTestPlanId();
            identity.groupName = fixture.getGroupName();
            identity.uid = fixture.getUid();
            return identity;
        }

        public String getTestPlanId() {
            return testPlanId;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getUid() {
            return uid;
        }

        public int compareTo(CaseIdentity other) {
            return key().compareTo(other.key());
        }

        @Override
        public boolean equals(Object value) {
            return value instanceof CaseIdentity && key().equals(((CaseIdentity) value).key());
        }

        @Override
        public int hashCode() {
            return key().hashCode();
        }

        private String key() {
            return value(testPlanId) + "\n" + value(groupName) + "\n" + value(uid);
        }

        private static String value(String value) {
            return value == null ? "" : value;
        }
    }
}