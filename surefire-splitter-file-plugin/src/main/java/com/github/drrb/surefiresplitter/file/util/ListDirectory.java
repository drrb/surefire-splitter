/**
 * Surefire Splitter File Plugin
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
 * along with Surefire Splitter File Plugin. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.file.util;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

public class ListDirectory {
    public static List<Path> listDirectory(Path directory, final FilenameFilter filenameFilter) throws IOException {
        final List<Path> children = new LinkedList<>();
        Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (filenameFilter.accept(file.toFile().getParentFile(), file.toFile().getName())) {
                    children.add(file);
                }
                return CONTINUE;
            }
        });
        return children;
    }
}
