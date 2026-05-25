/* @Authors
 * Student Names: Mehmet Abdullah Şeşen, Sezai Gökalp, Merve Nur Çalık
 * Student IDs: 150220029, 150220713, 150180096
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookScan {

    /**
     * Task #18: Substring
     * Counts how many times a specific substring appears in the target string.
     * Splitting logic or direct verification can leverage this.
     */
    public static int substringCount(String text, String sub) {
        if (text == null || sub == null || text.isEmpty() || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * Task #23: String Length
     * Returns the length of the provided string.
     */
    public static int getStringLength(String str) {
        if (str == null) {
            return 0;
        }
        return str.length();
    }

    /**
     * Task #27: Upper-Lower Case
     * Flips the case of a string (lowercase becomes uppercase, uppercase becomes lowercase).
     */
    public static String toggleCase(String str) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                chars[i] = Character.toLowerCase(c);
            } else if (Character.isLowerCase(c)) {
                chars[i] = Character.toUpperCase(c);
            }
        }
        return new String(chars);
    }

    /**
     * Primary Purpose Method: Scan Text
     * Analyzes a text line by line to find how many times words of a specific target length appear,
     * and maps those lengths to the exact line numbers (1-indexed) where they occur.
     * * It relies heavily on the clean application of the required methods above.
     * * @param text The full text content to scan (lines separated by '\n').
     * @param targetLength The exact word length to analyze and filter.
     * @return A Map containing total occurrences and the list of line numbers.
     */
    public Map<String, Object> scanWordLength(String text, int targetLength) {
        Map<String, Object> results = new HashMap<>();
        List<Integer> lineNumbers = new ArrayList<>();
        int totalOccurrences = 0;

        if (text == null || text.trim().isEmpty() || targetLength <= 0) {
            results.put("totalCount", 0);
            results.put("lineNumbers", lineNumbers);
            return results;
        }

        // Split text into individual lines safely
        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i];
            int lineNumber = i + 1; // 1-indexed for reader presentation

            // Clean up punctuation to isolate raw words
            String cleanLine = currentLine.replaceAll("[^a-zA-Z0-9\\s]", "");
            String[] words = cleanLine.split("\\s+");

            boolean lineContainsTargetLength = false;

            for (String word : words) {
                // Utilizing Task #23 logic to inspect string length
                if (getStringLength(word) == targetLength) {
                    totalOccurrences++;
                    lineContainsTargetLength = true;
                }
            }

            // If the word length target was found, record this line index
            if (lineContainsTargetLength) {
                lineNumbers.add(lineNumber);
            }
        }

        results.put("totalCount", totalOccurrences);
        results.put("lineNumbers", lineNumbers);
        return results;
    }
}