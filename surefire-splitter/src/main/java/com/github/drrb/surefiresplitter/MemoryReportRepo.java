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

import com.github.drrb.surefiresplitter.spi.JunitReport.JunitTestSuite;
import com.github.drrb.surefiresplitter.spi.ReportRepo;

import java.util.LinkedList;
import java.util.List;

public class MemoryReportRepo implements ReportRepo {

    private final List<JunitTestSuite> testSuites = new LinkedList<>();

    public MemoryReportRepo addTestSuite(JunitTestSuite testSuite) {
        testSuites.add(testSuite);
        return this;
    }

    @Override
    public List<JunitTestSuite> getTestSuites() {
        return new LinkedList<>(testSuites);
    }
}
