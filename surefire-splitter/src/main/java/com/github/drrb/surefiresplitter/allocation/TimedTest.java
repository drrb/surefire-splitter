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
package com.github.drrb.surefiresplitter.allocation;

import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;

import java.util.List;

public class TimedTest implements Comparable<TimedTest> {
    private final Class<?> testClass;
    private final Duration duration;

    public TimedTest(Class<?> testClass, Duration duration) {
        this.testClass = testClass;
        this.duration = duration;
    }

    @Override
    public int compareTo(TimedTest that) {
        int timeComparison = that.duration.compareTo(this.duration);
        if (timeComparison == 0) {
            return this.testClass.getName().compareTo(that.testClass.getName());
        } else {
            return timeComparison;
        }
    }

    public static TimedTest from(Class<?> testClass, List<JunitTestSuite> oldRuns) {
        for (JunitTestSuite oldRun : oldRuns) {
            if (testClass.getName().equals(oldRun.getName())) {
                return new TimedTest(testClass, Duration.of(oldRun.getTime()));
            }
        }
        return new TimedTest(testClass, Duration.UNKNOWN);
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Duration getDuration() {
        return duration;
    }
}
