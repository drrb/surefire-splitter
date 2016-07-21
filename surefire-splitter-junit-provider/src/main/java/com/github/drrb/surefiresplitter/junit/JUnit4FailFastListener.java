/**
 * Surefire Splitter JUnit provider
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
 * along with Surefire Splitter JUnit provider. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.junit;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.common.junit4.Notifier;
import org.apache.maven.surefire.common.junit4.Stoppable;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Calling {@link Notifier#fireStopEvent()} if failure happens.
 *
 * @author <a href="mailto:tibordigana@apache.org">Tibor Digana (tibor17)</a>
 * @since 2.19
 */
final class JUnit4FailFastListener
    extends RunListener
{
    private final Stoppable stoppable;

    JUnit4FailFastListener( Notifier stoppable )
    {
        this.stoppable = stoppable;
    }

    @Override
    public void testFailure( Failure failure )
        throws Exception
    {
        stoppable.fireStopEvent();
    }
}
