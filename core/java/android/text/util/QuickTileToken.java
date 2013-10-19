/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.text.util;

/**
 * This class stores the config info for custom quick tiles,
 * and provides methods to convert them to strings.
 */
	public class QuickTileToken {
    private String mName;
    private int mColumns, mRows;

    /**
     * Creates a new QuickTileToken with the specified name, columns,
     * and rows.
     */
    public QuickTileToken(String name, int rows, int columns) {
        mName = name;
        mColumns = columns;
        mRows = rows;
    }

    /**
     * Returns the name part.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the columns part.
     */
    public int getColumns() {
        return mColumns;
    }

    /**
     * Returns the rows part.
     */
    public int getRows() {
        return mRows;
    }

    /**
     * Changes the name to the specified name.
     */
    public void setName(String name) {
        mName = name;
    }

    /**	
     * Changes the columns to the specified columns.
     */	
    public void setColumns(int columns) {
        mColumns = columns;
    }

    /**
     * Changes the rows to the specified rows.
     */
    public void setRows(int rows) {
        mRows = rows;
    }

    /**
     * Returns the name,
     * the columns and rows (in a suitable form for saving).
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        if (mName != null && mName.length() != 0) {
            sb.append(mName);
        }
        sb.append(',');
        sb.append(mRows);
        sb.append(',');
        sb.append(mColumns);
        sb.append('>');

        return sb.toString();
    }

    private int hash(int n) {
        return (int) ((131111L*n)^n^(1973*n)%7919);
    }

    public int hashCode() {
        int result = 17;
        if (mName != null) result = 31 * result + mName.hashCode();
        result = 31 * result + hash(mColumns);
        result = 31 * result + hash(mRows);
        return result;
    }

    private static boolean stringEquals(String a, String b) {
        if (a == null) {
            return (b == null);
        } else {
            return (a.equals(b));
        }
    }

    private static boolean intEquals(int a, int b) {
        return (a == b);
    }

    public boolean equals(Object o) {
        if (!(o instanceof QuickTileToken)) {
            return false;
        }
        QuickTileToken other = (QuickTileToken) o;
        return (stringEquals(mName, other.mName) &&
                intEquals(mColumns, other.mColumns) &&
                intEquals(mRows, other.mRows));
    }	
}