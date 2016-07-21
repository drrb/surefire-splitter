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

import com.google.gson.Gson;

import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

public class GoFiles extends LinkedList<GoFile> {
    public List<GoFile> filter(FilenameFilter filter) {
        List<GoFile> result = new LinkedList<>();
        for (GoFile file : this) {
            if (file.isFolder()) {
                result.addAll(file.getFiles().filter(filter));
            } else if (filter.accept(null, file.getName())) {
                result.add(file);
            }
        }
        return result;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static GoFiles fromJson(String json) {
        return new Gson().fromJson(json, GoFiles.class);
    }
}
