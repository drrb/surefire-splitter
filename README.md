# Surefire Splitter

An add-on for the Maven Surefire Plugin to make it split up your tests when run
on GoCD, so that you can just add more Go agents to reduce your build duration.

[![Build Status](https://travis-ci.org/drrb/surefire-splitter.svg?branch=master)](https://travis-ci.org/drrb/surefire-splitter)

# Details

Surefire Splitter is an add-on for the Maven Surefire Plugin that runs only a
portion of your tests, splitting them into even chunks by duration. It is
designed to be used to run many builds in parallel to decrease total build
time. Surefire Splitter comes with a couple of plugins to help it decide how to
split tests. The main plugin is for use with GoCD, to work with its [parallel
job execution](http://www.go.cd/2015/10/09/Distrubuted-Test-Execution.html).
Another plugin exists to split the tests using system properties instead.

## Requirements

* Maven Surefire Plugin >= 2.19
* Java 7+
* JUnit tests

## Splitting Builds on GoCD

The main use of Surefire Splitter is to split jobs on Go, so that you can
decrease the duration of your build by adding more agents.

Add the following to your Surefire Plugin configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.19</version>
    <dependencies>
        <dependency>
            <groupId>com.github.drrb</groupId>
            <artifactId>surefire-splitter-junit-provider</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.github.drrb</groupId>
            <artifactId>surefire-splitter-go-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</plugin>
```

Then split your build over as many agents as you would like (details
[here](http://www.go.cd/2015/10/09/Distrubuted-Test-Execution.html)). Make sure
that your job uploads its Surefire reports as test artifacts.

When your build is run on a Go agent, Surefire Splitter will determine how many
agents are running the job (using Go's environment variables), and divide up
the tests according to their duration during previous runs.

## Authentication on Go

Surefire Splitter uses Go's APIs to download previous Surefire reports, so if
your Go server uses authentication, you will need to create a user that is able
to download artifacts for the current job. Then, configure the following as
*secure* environment variables for the job in Go:

```
GO_USERNAME: The username
GO_PASSWORD: The password
```

## Splitting Test Runs Without Go

To allow Surefire Splitter to be used without a Go server, a filesystem-based
plugin is also provided. Add something like this to your Surefire Plugin
configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.19</version>
    <dependencies>
        <dependency>
            <groupId>com.github.drrb</groupId>
            <artifactId>surefire-splitter-junit-provider</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.github.drrb</groupId>
            <artifactId>surefire-splitter-file-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</plugin>
```

Then run your tests like this to just run a small portion of the tests, reading
past test durations from Surefire reports in a specified directory:

```
mvn test -Dsurefire.totalChunks=5 -Dsurefire.chunkNumber=1 -Dsurefire.existingReportsDir=/path/to/old/surefire-reports
```

## Acknowledgement

This project was inspired by Test Load Balancer. It used TLB's example project to generate some test data.

## License

Surefire Splitter

Copyright (C) 2016 drrb

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
