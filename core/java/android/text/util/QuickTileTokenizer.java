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

import java.util.ArrayList;
import java.util.List;

/**
 * This class works as a Tokenizer for the QuickTiles settings
 */
public class QuickTileTokenizer {
    /**
     * This constructor will try to take a string like
     * "<QuickAirplane,1,1>"
     * and convert it into one or more QuickTileTokens, output into the supplied
     * collection.
     *
     * It will try to be tolerant of broken syntax instead of
     * returning an error.
     *
     */
    public static void tokenize(String text, List<QuickTileToken> out) {
        String name = "";
        int columns = 0;
        int rows = 0;

        if(text==null) return;

        for(String setting : text.split("\\|")) {
            String[] parts = setting.split(",");
            // do we have the correct number of parts? 
            if(parts.length == 3){

                name = parts[0].replace("<","");
                rows = Integer.parseInt(parts[1]);
                columns = Integer.parseInt(parts[2].replace(">", ""));

        		// check to make sure the data looks good
                if(!name.equals("") && columns > 0 && rows > 0){
                    out.add(new QuickTileToken(name,
                            rows, columns));
                }
            }
            name = "";
            columns = 0;
            rows = 0;
        }
    }

    /**	
     * This method will try to take a string like
     * "<QuickAirplane,1,1>"
     * and convert it into one or more QuickTileTokens.	
     * It will try to be tolerant of broken syntax instead of
     * returning an error.
     */	
    public static QuickTileToken[] tokenize(String text) {
        ArrayList<QuickTileToken> out = new ArrayList<QuickTileToken>();
        tokenize(text, out);
        return out.toArray(new QuickTileToken[out.size()]);
    }

    /**	
     * {@inheritDoc}
     */	
    public int findTokenStart(CharSequence text, int cursor) {
        /*
         * It's hard to search backward, so search forward until
         * we reach the cursor.
         */

        int best = 0;
        int i = 0;
        while (i < cursor) {
            i = findTokenEnd(text, i);
            if (i < cursor) {
                i++; // Skip terminating punctuation 

                while (i < cursor && text.charAt(i) != '<') {
                    i++;
                }
                if (i < cursor) {
                    best = i;
                }
            }
        }

        return best;
    }

    /**	
     * {@inheritDoc}
     */	
    public int findTokenEnd(CharSequence text, int cursor) {
        int len = text.length();
        int i = cursor;
        while (i < len) {
            char c = text.charAt(i);
            if (c == '>') {
                return i;
            } else if (c == ',') {
                int level = 1;
                i++;
                while (i < len && level > 0) {
                    c = text.charAt(i);
                    if (c == ',' || c == '>') {
                        level--;
                        i++;	
                    } else {
                        i++;	
                    }
                }
            } else if (c == '<' || c == '|') {
                i++;	
                while (i < len) {
                    c = text.charAt(i);
                    if (c == '>') {
                        i++;
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                i++;
            }
        }
        return i;
    }
    /**	
     * Terminates the specified address with a pipe.
     * This assumes that the specified text already has valid syntax.
     * The Adapter subclass's convertToString() method must make that
     * guarantee.
     */
    public CharSequence terminateToken(CharSequence text) {
        return text + "|";
    }
}
