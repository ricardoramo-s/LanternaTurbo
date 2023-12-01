/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal.virtual;

import com.googlecode.lanterna.TextCharacter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is used to store lines of text inside of a terminal emulator. As used by {@link DefaultVirtualTerminal}, it keeps
 * two {@link TextBuffer}s, one for private mode and one for normal mode and it can switch between them as needed.
 */
class TextBuffer {
    private static final TextCharacter DOUBLE_WIDTH_CHAR_PADDING = new TextCharacter(' ');

    private final List<List<TextCharacter>> lines;

    TextBuffer() {
        this.lines = new ArrayList<>(256);
        newLine();
    }

    synchronized void newLine() {
        lines.add(new ArrayList<>(240));
    }

    synchronized void removeTopLines(int numberOfLinesToRemove) {
        if (numberOfLinesToRemove > 0) {
            lines.subList(0, numberOfLinesToRemove).clear();
        }
    }

    synchronized void clear() {
        lines.clear();
        newLine();
    }

    ListIterator<List<TextCharacter>> getLinesFrom(int rowNumber) {
        return lines.listIterator(rowNumber);
    }

    synchronized int getLineCount() {
        return lines.size();
    }

    synchronized int setCharacter(int lineNumber, int columnIndex, TextCharacter textCharacter) {
        if(lineNumber < 0 || columnIndex < 0) {
            throw new IllegalArgumentException("Illegal argument to TextBuffer.setCharacter(..), lineNumber = " +
                    lineNumber + ", columnIndex = " + columnIndex);
        }
        if(textCharacter == null) {
            textCharacter = TextCharacter.DEFAULT_CHARACTER;
        }
        while(lineNumber >= lines.size()) {
            newLine();
        }
        List<TextCharacter> line = lines.get(lineNumber);
        while(line.size() <= columnIndex) {
            line.add(TextCharacter.DEFAULT_CHARACTER);
        }

        // Default
        int returnStyle = 0;
        line.set(columnIndex, textCharacter);
        return returnStyle;
    }

    synchronized TextCharacter getCharacter(int lineNumber, int columnIndex) {
        if(lineNumber < 0 || columnIndex < 0) {
            throw new IllegalArgumentException("Illegal argument to TextBuffer.getCharacter(..), lineNumber = " +
                    lineNumber + ", columnIndex = " + columnIndex);
        }
        if(lineNumber >= lines.size()) {
            return TextCharacter.DEFAULT_CHARACTER;
        }
        List<TextCharacter> line = lines.get(lineNumber);
        if(line.size() <= columnIndex) {
            return TextCharacter.DEFAULT_CHARACTER;
        }
        TextCharacter textCharacter = line.get(columnIndex);
        if(textCharacter == DOUBLE_WIDTH_CHAR_PADDING) {
            return line.get(columnIndex - 1);
        }
        return textCharacter;
    }

    @Override
    public String toString() {
        StringBuilder bo = new StringBuilder();
        for (List<TextCharacter> line : lines) {
            StringBuilder b = new StringBuilder();
            for (TextCharacter c : line) {
                b.append(c.getCharacterString());
            }
            bo.append(b.toString().replaceFirst("\\s+$", ""));
            bo.append('\n');
        }
        return bo.toString();
    }
}
