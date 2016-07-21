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
import com.github.drrb.surefiresplitter.spi.ReportRepo;
import com.github.drrb.surefiresplitter.spi.ReportRepoProvider;

public class AllocationConfig {

    public static class Builder {
        private boolean splitTests;
        private int totalChunks;
        private int chunkNumber;
        private ReportRepo existingReports;

        public Builder withoutSplitTests() {
            this.splitTests = false;
            return this;
        }

        public Builder withSplitTests() {
            this.splitTests = true;
            return this;
        }

        public Builder withTotalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
            return this;
        }

        public Builder withChunkNumber(int chunkNumber) {
            this.chunkNumber = chunkNumber;
            return this;
        }

        public Builder withExistingReports(ReportRepo existingReports) {
            this.existingReports = existingReports;
            return this;
        }

        public Builder withNoExistingReports() {
            return withExistingReports(new MemoryReportRepo());
        }

        public AllocationConfig chunk(int chunkNumber, int totalChunks) {
            return withSplitTests()
                    .withChunkNumber(chunkNumber)
                    .withTotalChunks(totalChunks)
                    .build();
        }

        public AllocationConfig build() {
            return new AllocationConfig(splitTests, chunkNumber, totalChunks, existingReports);
        }
    }

    public static Builder allocationConfig() {
        return new Builder();
    }

    public static AllocationConfig get(ClassLoader classLoader) {
        return get(new ServiceLookup(classLoader));
    }

    public static AllocationConfig get(ServiceLookup serviceLookup) {
        Builder config = allocationConfig()
                .withoutSplitTests()
                .withNoExistingReports();

        //TODO: should we order these by priority? (e.g. with a getPriority() method on the provider interface)
        for (AllocationConfigProvider configProvider : serviceLookup.getAllocationConfigProviders()) {
            if (configProvider.isAvailable()) {
                Integer chunkNumber = configProvider.getChunkNumber();
                Integer totalChunks = configProvider.getTotalChunks();
                config.withSplitTests()
                        .withChunkNumber(chunkNumber)
                        .withTotalChunks(totalChunks);
                if (totalChunks < 1) {
                    throw new RuntimeException("Expected total chunks to be greater than zero, but it was " + totalChunks);
                }
                if (chunkNumber < 1 || chunkNumber > totalChunks) {
                    throw new RuntimeException("Expected chunk number to be between 1 and " + totalChunks + ", but it was " + chunkNumber);
                }
                break;
            }
        }

        ProxyReportRepo existingReports = new ProxyReportRepo();
        for (ReportRepoProvider reportRepoProvider : serviceLookup.getReportRepoProviders()) {
            if (reportRepoProvider.isAvailable()) {
                existingReports.addDelegate(reportRepoProvider.getExistingReports());
            }
        }
        config.withExistingReports(existingReports);

        return config.build();
    }

    private final boolean splitTests;
    private final int chunkNumber;
    private final int totalChunks;
    private final ReportRepo existingReports;

    private AllocationConfig(boolean splitTests, int chunkNumber, int totalChunks, ReportRepo existingReports) {
        this.splitTests = splitTests;
        this.chunkNumber = chunkNumber;
        this.totalChunks = totalChunks;
        this.existingReports = existingReports;
    }

    public boolean isSplitTests() {
        return splitTests;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public ReportRepo getExistingReports() {
        return existingReports;
    }
}
