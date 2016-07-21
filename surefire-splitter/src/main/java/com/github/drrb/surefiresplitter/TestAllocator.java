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

import com.github.drrb.surefiresplitter.allocation.Chunk;
import com.github.drrb.surefiresplitter.allocation.Chunks;
import com.github.drrb.surefiresplitter.allocation.TimedTest;
import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import com.github.drrb.surefiresplitter.spi.ReportRepo;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.github.drrb.surefiresplitter.util.Pluralizer.pluralize;

public class TestAllocator {

    private static final int MAX_SUGGESTED_CHUNKS = 500;

    public static TestAllocator get(ClassLoader classLoader) {
        return new TestAllocator(AllocationConfig.get(classLoader));
    }

    private final AllocationConfig config;
    private final ReportRepo existingReports;

    public TestAllocator(AllocationConfig config) {
        this.config = config;
        this.existingReports = config.getExistingReports();
    }

    public Iterable<Class<?>> allocate(Iterable<Class<?>> testsToRun) {
        if (!config.isSplitTests()) {
            System.out.println("No chunk number specified: running all tests");
            return testsToRun;
        }

        int chunkNumber = config.getChunkNumber();
        int totalChunks = config.getTotalChunks();

        System.out.println(getRunningMessage(testsToRun));

        SortedSet<TimedTest> orderedTests = order(testsToRun);
        Chunks chunks = chunk(orderedTests, totalChunks);
        return chunks.get(chunkNumber).getTests();
    }

    private String getRunningMessage(Iterable<Class<?>> testsToRun) {
        final String mainMessage;
        if (config.getTotalChunks() == 1) {
            mainMessage = "Running all tests";
        } else {
            mainMessage = String.format("Running tests from chunk %d of %d", config.getChunkNumber(), config.getTotalChunks());
        }

        try {
            final int suggestedNumberOfChunks = suggestedNumberOfChunks(testsToRun);

            if (config.getExistingReports().getTestSuites().isEmpty()) {
                return String.format("%s (not sure how many chunks you'd ideally have: I couldn't find any previous reports!)", mainMessage);
            } else if (suggestedNumberOfChunks == config.getTotalChunks()) {
                return mainMessage;
            } else if (suggestedNumberOfChunks > config.getTotalChunks()) {
                return String.format("%s (though you might want to try running with %s for a better build time)", mainMessage, pluralize("chunk", suggestedNumberOfChunks));
            } else {
                return String.format("%s (though you might only need %s for an optimal build time)", mainMessage, pluralize("chunk", suggestedNumberOfChunks));
            }
        } catch (UnableToSuggestTotalChunks excuse) {
            return String.format("%s (not sure how many chunks you'd ideally have: %s)", mainMessage, excuse.getMessage());
        }
    }

    private SortedSet<TimedTest> order(Iterable<Class<?>> testsToRun) {
        SortedSet<TimedTest> ordered = new TreeSet<>();
        List<JunitTestSuite> oldRuns = existingReports.getTestSuites();
        for (Class<?> testClass : testsToRun) {
            ordered.add(TimedTest.from(testClass, oldRuns));
        }
        return ordered;
    }

    private Chunks chunk(SortedSet<TimedTest> orderedTests, int totalChunks) {
        Chunks chunks = new Chunks(totalChunks);
        for (TimedTest suite : orderedTests) {
            chunks.getShortest().addTestSuite(suite);
        }
        return chunks;
    }

    public int suggestedNumberOfChunks(Iterable<Class<?>> tests) throws UnableToSuggestTotalChunks {
        SortedSet<TimedTest> orderedTests = order(tests);
        for (TimedTest test : orderedTests) {
            if (test.getDuration().isUnknown()) {
                throw UnableToSuggestTotalChunks.someTestsHaveNotBeenRunBefore();
            }
        }
        for (int totalChunks = 1; totalChunks < MAX_SUGGESTED_CHUNKS; totalChunks++) {
            Chunks chunks = chunk(orderedTests, totalChunks);
            Chunk longestChunk = chunks.getLongest();
            if (longestChunk.size() == 1) { // Total runtime is the time taken by the longest test
                return totalChunks;
            }
        }
        throw UnableToSuggestTotalChunks.requiresMoreChunksThan(MAX_SUGGESTED_CHUNKS);
    }

    public static class UnableToSuggestTotalChunks extends Exception {
        public static UnableToSuggestTotalChunks requiresMoreChunksThan(int maxCheckedChunkNumber) {
            return new UnableToSuggestTotalChunks("an optimal build would probably require more than %d", pluralize("chunk", maxCheckedChunkNumber));
        }

        public static UnableToSuggestTotalChunks someTestsHaveNotBeenRunBefore() {
            return new UnableToSuggestTotalChunks("reports couldn't be found for some tests");
        }

        public UnableToSuggestTotalChunks(String messageTemplate, Object... args) {
            super(String.format(messageTemplate, args));
        }
    }
}

