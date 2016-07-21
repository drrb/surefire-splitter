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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.sort;

public class Chunks {
    private final List<Chunk> chunks;

    public Chunks(int size) {
        this.chunks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            chunks.add(new Chunk(i + 1));
        }
    }

    public Chunk getShortest() {
        return orderedChunks().getFirst();
    }

    public Chunk getLongest() {
        return orderedChunks().getLast();
    }

    public Chunk get(int chunkNumber) {
        int chunkIndex = getChunkIndex(chunkNumber);
        return chunks.get(chunkIndex);
    }

    private Deque<Chunk> orderedChunks() {
        LinkedList<Chunk> sortedChunks = new LinkedList<>(chunks);
        sort(sortedChunks);
        return sortedChunks;
    }

    private int getChunkIndex(int chunkNumber) {
        int base = Math.abs(hashCode()) % chunks.size();
        return (base + chunkNumber) % chunks.size();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Chunk chunk : chunks) {
            for (Class<?> testClass : chunk.getTests()) {
                hash += testClass.getName().hashCode();
            }
        }
        return hash;
    }
}
