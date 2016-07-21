/**
 * Surefire Splitter Go Plugin
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
 * along with Surefire Splitter Go Plugin. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.go.model;

public class GoFile {

    public enum FileType {
        folder,
        file
    }

    private final String name;
    private final String url;
    private final FileType type;
    private final GoFiles files = new GoFiles();

    public GoFile(FileType type, String name, String url) {
        this.type = type;
        this.name = name;
        this.url = url;
    }

    public boolean isFolder() {
        return type == FileType.folder;
    }

    public String getUrl() {
        return url;
    }

    public GoFiles getFiles() {
        return files;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "GoFile{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                ", files=" + files +
                '}';
    }
}
