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

import com.github.drrb.surefiresplitter.faketests.*;
import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import com.github.drrb.surefiresplitter.spi.ReportRepo;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.github.drrb.surefiresplitter.AllocationConfig.allocationConfig;
import static com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestCase.testCase;
import static com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite.testSuite;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestAllocatorTest {

    private final MemoryReportRepo NO_EXISTING_REPORTS = new MemoryReportRepo();

    @Test
    public void shouldReturnAllTestsWhenNoChunkSelected() {
        TestAllocator allocator = new TestAllocator(allocationConfig().withoutSplitTests().withNoExistingReports().build());
        List<Class<?>> tests = asList(A.class, B.class, C.class, D.class);
        assertThat(allocator.allocate(tests), contains(tests));
    }

    @Test
    public void shouldSelectAChunkOfTests() {
        AllocationConfig.Builder allocationConfig = allocationConfig().withExistingReports(NO_EXISTING_REPORTS);

        List<Class<?>> tests = asList(A.class, B.class, C.class, D.class, E.class, F.class, G.class);
        assertThat(new TestAllocator(allocationConfig.chunk(1, 4)).allocate(tests), contains(B.class, F.class));
        assertThat(new TestAllocator(allocationConfig.chunk(2, 4)).allocate(tests), contains(C.class, G.class));
        assertThat(new TestAllocator(allocationConfig.chunk(3, 4)).allocate(tests), contains(D.class));
        assertThat(new TestAllocator(allocationConfig.chunk(4, 4)).allocate(tests), contains(A.class, E.class));
    }

    @Test
    public void shouldAllocateTestsByTime() {
        ReportRepo existingReports = new MemoryReportRepo()
                .addTestSuite(fakeTest(A.class, 1))
                .addTestSuite(fakeTest(B.class, 1))
                .addTestSuite(fakeTest(C.class, 2))
                .addTestSuite(fakeTest(D.class, 2))
                .addTestSuite(fakeTest(E.class, 2))
                .addTestSuite(fakeTest(F.class, 3))
                .addTestSuite(fakeTest(G.class, 6));
        AllocationConfig.Builder allocationConfig = allocationConfig().withExistingReports(existingReports);

        List<Class<?>> tests = asList(A.class, B.class, C.class, D.class, E.class, F.class, G.class);

        assertThat(new TestAllocator(allocationConfig.chunk(1, 4)).allocate(tests), contains(F.class, B.class));
        assertThat(new TestAllocator(allocationConfig.chunk(2, 4)).allocate(tests), contains(C.class, E.class));
        assertThat(new TestAllocator(allocationConfig.chunk(3, 4)).allocate(tests), contains(D.class, A.class));
        assertThat(new TestAllocator(allocationConfig.chunk(4, 4)).allocate(tests), contains(G.class));
    }

    @Test
    public void shouldShuffleChunksSoThatTheSlowestChunkIsntAlwaysTheFirst() {
        ReportRepo existingReports = new MemoryReportRepo()
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.A.class, 0.006))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.B.class, 8.402))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.C.class, 0.999))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.D.class, 0.004))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.E.class, 2.003))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.F.class, 5.004))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.faketests.G.class, 2.008))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.A.class, 0.006))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.B.class, 8.402))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.C.class, 0.999))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.D.class, 0.004))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.E.class, 2.003))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.F.class, 5.004))
                .addTestSuite(fakeTest(com.github.drrb.surefiresplitter.otherfaketests.G.class, 2.008));
        AllocationConfig.Builder config = allocationConfig().withExistingReports(existingReports);

        List<Class<?>> tests1 = asList(
                com.github.drrb.surefiresplitter.faketests.A.class,
                com.github.drrb.surefiresplitter.faketests.B.class,
                com.github.drrb.surefiresplitter.faketests.C.class,
                com.github.drrb.surefiresplitter.faketests.D.class,
                com.github.drrb.surefiresplitter.faketests.E.class,
                com.github.drrb.surefiresplitter.faketests.F.class,
                com.github.drrb.surefiresplitter.faketests.G.class
        );
        List<Class<?>> tests2 = asList(
                com.github.drrb.surefiresplitter.otherfaketests.A.class,
                com.github.drrb.surefiresplitter.otherfaketests.B.class,
                com.github.drrb.surefiresplitter.otherfaketests.C.class,
                com.github.drrb.surefiresplitter.otherfaketests.D.class,
                com.github.drrb.surefiresplitter.otherfaketests.E.class,
                com.github.drrb.surefiresplitter.otherfaketests.F.class,
                com.github.drrb.surefiresplitter.otherfaketests.G.class
        );

        assertThat(new TestAllocator(config.chunk(1, 3)).allocate(tests1), contains(com.github.drrb.surefiresplitter.faketests.B.class));
        assertThat(new TestAllocator(config.chunk(2, 3)).allocate(tests1), contains(com.github.drrb.surefiresplitter.faketests.F.class, com.github.drrb.surefiresplitter.faketests.A.class, com.github.drrb.surefiresplitter.faketests.D.class));
        assertThat(new TestAllocator(config.chunk(3, 3)).allocate(tests1), contains(com.github.drrb.surefiresplitter.faketests.G.class, com.github.drrb.surefiresplitter.faketests.E.class, com.github.drrb.surefiresplitter.faketests.C.class));

        assertThat(new TestAllocator(config.chunk(1, 3)).allocate(tests2), contains(com.github.drrb.surefiresplitter.otherfaketests.F.class, com.github.drrb.surefiresplitter.otherfaketests.A.class, com.github.drrb.surefiresplitter.otherfaketests.D.class));
        assertThat(new TestAllocator(config.chunk(2, 3)).allocate(tests2), contains(com.github.drrb.surefiresplitter.otherfaketests.G.class, com.github.drrb.surefiresplitter.otherfaketests.E.class, com.github.drrb.surefiresplitter.otherfaketests.C.class));
        assertThat(new TestAllocator(config.chunk(3, 3)).allocate(tests2), contains(com.github.drrb.surefiresplitter.otherfaketests.B.class));
    }

    @Test
    public void shouldSuggestOptimalNumberOfChunks() throws Exception {
        ReportRepo existingReports = new MemoryReportRepo()
                .addTestSuite(fakeTest(A.class, 1))
                .addTestSuite(fakeTest(B.class, 1))
                .addTestSuite(fakeTest(C.class, 2))
                .addTestSuite(fakeTest(D.class, 2))
                .addTestSuite(fakeTest(E.class, 2))
                .addTestSuite(fakeTest(F.class, 3))
                .addTestSuite(fakeTest(G.class, 6));
        AllocationConfig allocationConfig = allocationConfig().withExistingReports(existingReports).build();

        List<Class<?>> tests = asList(A.class, B.class, C.class, D.class, E.class, F.class, G.class);

        assertThat(new TestAllocator(allocationConfig).suggestedNumberOfChunks(tests), is(4));
    }

    @Test(expected = TestAllocator.UnableToSuggestTotalChunks.class)
    public void shouldNotSuggestChunkNumberIfSomeTestsDontHaveReports() throws Exception {
        ReportRepo existingReports = new MemoryReportRepo()
                .addTestSuite(fakeTest(A.class, 1))
                .addTestSuite(fakeTest(B.class, 1))
                .addTestSuite(fakeTest(C.class, 2))
                .addTestSuite(fakeTest(D.class, 2))
                .addTestSuite(fakeTest(E.class, 2))
                //.addTestSuite(fakeTest(F.class, 3))
                .addTestSuite(fakeTest(G.class, 6));
        AllocationConfig allocationConfig = allocationConfig().withExistingReports(existingReports).build();

        List<Class<?>> tests = asList(A.class, B.class, C.class, D.class, E.class, F.class, G.class);

        assertThat(new TestAllocator(allocationConfig).suggestedNumberOfChunks(tests), is(4));
    }

    @Test
    public void shouldIgnoreReportsFromNonScannedTests() {
        ReportRepo existingReports = new MemoryReportRepo()
                .addTestSuite(fakeTest(A.class, 1))
                .addTestSuite(testSuite()
                                .withName("com.example.MissingTest")
                                .withTime(0)
                                .build()
                );
        AllocationConfig.Builder allocationConfig = allocationConfig().withExistingReports(existingReports);

        List<Class<?>> tests = asList(A.class);

        assertThat(new TestAllocator(allocationConfig.chunk(1, 4)).allocate(tests), isEmpty());
        assertThat(new TestAllocator(allocationConfig.chunk(2, 4)).allocate(tests), isEmpty());
        assertThat(new TestAllocator(allocationConfig.chunk(3, 4)).allocate(tests), contains(A.class));
        assertThat(new TestAllocator(allocationConfig.chunk(4, 4)).allocate(tests), isEmpty());
    }

    private JunitTestSuite fakeTest(Class<?> testClass, double time) {
        return testSuite()
                .withName(testClass.getName())
                .withTime(time)
                .withTestCase(
                        testCase()
                                .withName(testClass.getSimpleName().toLowerCase())
                                .withClassName(testClass.getName())
                                .build()
                )
                .build();
    }

    private List<Class<?>> asList(Class<?>... classes) {
        return Arrays.asList(classes);
    }

    private Matcher<Iterable<Class<?>>> isEmpty() {
        return contains();
    }

    private Matcher<Iterable<Class<?>>> contains(Class<?>... expected) {
        return contains(asList(expected));
    }

    private <T> Matcher<Iterable<T>> contains(final Iterable<T> expected) {
        return new TypeSafeMatcher<Iterable<T>>() {
            @Override
            protected boolean matchesSafely(Iterable<T> actual) {
                Iterator<T> actualIt = actual.iterator();
                Iterator<T> expectedIt = expected.iterator();
                while (actualIt.hasNext()) {
                    if (!(expectedIt.hasNext() && actualIt.next().equals(expectedIt.next()))) {
                        return false;
                    }
                }
                return !expectedIt.hasNext();
            }

            public void describeTo(Description description) {
                description.appendText("Iterable containing (in order) ").appendValue(expected);
            }
        };
    }
}
