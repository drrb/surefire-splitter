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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Chunk implements Comparable<Chunk> {

    private final List<TimedTest> testSuites = new LinkedList<>();
    private final int number;

    public Chunk(int number) {
        this.number = number;
    }

    public void addTestSuite(TimedTest testSuite) {
        testSuites.add(testSuite);
    }

    private Duration duration() {
        Duration totalDuration = Duration.of(0.0);
        for (TimedTest test : testSuites) {
            totalDuration = totalDuration.plus(test.getDuration());
        }
        return totalDuration;
    }

    public int size() {
        return testSuites.size();
    }

    public Iterable<Class<?>> getTests() {
        Set<Class<?>> result = new LinkedHashSet<>();
        for (TimedTest test : testSuites) {
            result.add(test.getTestClass());
        }
        return result;
    }

    @Override
    public int compareTo(Chunk that) {
        return this.duration().compareTo(that.duration());
    }

    @Override
    public String toString() {
        return "Chunk {" +
                " number=" + number +
                ", testSuites=" + testSuites +
                " }";
    }
}
