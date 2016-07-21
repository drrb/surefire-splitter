/**
 * Surefire Splitter
 * Copyright (C) 2016 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Surefire Splitter. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter;

import com.github.drrb.surefiresplitter.spi.AllocationConfigProvider;
import com.github.drrb.surefiresplitter.spi.JunitReport;
import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestCase;
import com.github.drrb.surefiresplitter.spi.ReportRepo;
import com.github.drrb.surefiresplitter.spi.ReportRepoProvider;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AllocationConfigTest {

    @Test
    public void shouldBeDisabledByDefault() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryAllocationConfigProvider.unavailable());
        AllocationConfig config = AllocationConfig.get(serviceLookup);
        assertThat(config.isSplitTests(), is(false));
    }

    @Test
    public void shouldBeEnabledIfAConfigProviderIsAvailable() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryAllocationConfigProvider.unavailable());
        serviceLookup.addProvider(MemoryAllocationConfigProvider.chunk(1, 3));
        AllocationConfig config = AllocationConfig.get(serviceLookup);
        assertThat(config.isSplitTests(), is(true));
    }

    @Test
    public void shouldUseTheFirstAvailableConfigProvider() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryAllocationConfigProvider.unavailable());
        serviceLookup.addProvider(MemoryAllocationConfigProvider.chunk(1, 3));
        serviceLookup.addProvider(MemoryAllocationConfigProvider.chunk(2, 4));
        AllocationConfig config = AllocationConfig.get(serviceLookup);
        assertThat(config.isSplitTests(), is(true));
        assertThat(config.getChunkNumber(), is(1));
        assertThat(config.getTotalChunks(), is(3));
    }

    @Test
    public void shouldHaveNoReportsByDefault() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryReportRepoProvider.unavailable());
        AllocationConfig config = AllocationConfig.get(serviceLookup);
        assertThat(config.getExistingReports().getTestSuites().size(), is(0));
    }

    @Test
    public void shouldCombineReportsFromReportProviders() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryReportRepoProvider.available(new MemoryReportRepo().addTestSuite(new JunitReport.JunitTestSuite("x", 1.0, new ArrayList<JunitTestCase>()))));
        serviceLookup.addProvider(MemoryReportRepoProvider.unavailable());
        serviceLookup.addProvider(MemoryReportRepoProvider.available(new MemoryReportRepo().addTestSuite(new JunitReport.JunitTestSuite("y", 2.0, new ArrayList<JunitTestCase>()))));

        AllocationConfig config = AllocationConfig.get(serviceLookup);
        assertThat(config.getExistingReports().getTestSuites().size(), is(2));
        assertThat(config.getExistingReports().getTestSuites().get(0).getName(), is("x"));
        assertThat(config.getExistingReports().getTestSuites().get(1).getName(), is("y"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenChunkIsZero() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryAllocationConfigProvider.chunk(0, 3));
        AllocationConfig.get(serviceLookup);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenTotalChunksIsNegative() {
        TestServiceLookup serviceLookup = new TestServiceLookup();
        serviceLookup.addProvider(MemoryAllocationConfigProvider.chunk(1, 0));
        AllocationConfig.get(serviceLookup);
    }

    private static class MemoryReportRepoProvider implements ReportRepoProvider {

        private final ReportRepo repo;
        private final boolean available;
        private final String description;

        public static MemoryReportRepoProvider available(ReportRepo repo) {
            return new MemoryReportRepoProvider(repo, true, "Available report repo plugin");
        }

        public static MemoryReportRepoProvider unavailable() {
            return new MemoryReportRepoProvider(null, false, "Unavailable report repo plugin");
        }

        public MemoryReportRepoProvider(ReportRepo repo, boolean available, String description) {
            this.repo = repo;
            this.available = available;
            this.description = description;
        }

        public ReportRepo getExistingReports() {
            return repo;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getDescription() {
            return description;
        }
    }

    private static class MemoryAllocationConfigProvider implements AllocationConfigProvider {

        private final Integer chunkNumber;
        private final Integer totalChunks;
        private final boolean available;
        private final String description;

        public static MemoryAllocationConfigProvider unavailable() {
            return new MemoryAllocationConfigProvider(null, null, false, "Unavailable provider");
        }


        public static MemoryAllocationConfigProvider chunk(int chunkNumber, int totalChunks) {
            return new MemoryAllocationConfigProvider(chunkNumber, totalChunks, true, "Chunking provider");
        }

        public MemoryAllocationConfigProvider(Integer chunkNumber, Integer totalChunks, boolean available, String description) {
            this.chunkNumber = chunkNumber;
            this.totalChunks = totalChunks;
            this.available = available;
            this.description = description;
        }

        public Integer getChunkNumber() {
            return chunkNumber;
        }

        public Integer getTotalChunks() {
            return totalChunks;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getDescription() {
            return description;
        }
    }

    private static class TestServiceLookup extends ServiceLookup {

        private final Map<Class<?>, List<Object>> providers = new HashMap<>();

        public TestServiceLookup() {
            super(null);
        }

        public void addProvider(Object provider) {
            for (Class<?> providerType : provider.getClass().getInterfaces()) {
                List<Object> providersOfType = providers.get(providerType);
                if (providersOfType == null) {
                    providersOfType = new LinkedList<>();
                    providers.put(providerType, providersOfType);
                }
                providersOfType.add(provider);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> lookUp(Class<T> providerType) {
            if (providers.containsKey(providerType)) {
                return (List<T>) providers.get(providerType);
            } else {
                return Collections.emptyList();
            }
        }
    }
}
