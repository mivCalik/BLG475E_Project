/* @Authors
 * Student Names: Mehmet Abdullah Şeşen, Sezai Gökalp, Merve Nur Çalık
 * Student IDs: 150220029, 150220713, 150180096
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BookScanTest {

    private BookScan bookScan;

    @BeforeEach
    public void setUp() {
        bookScan = new BookScan();
    }

    @Nested
    class UnitTests {
        @Test
        public void testSubstringCount_Normal() {
            assertEquals(2, BookScan.substringCount("banana", "an"));
        }

        @Test
        public void testGetStringLength_Normal() {
            assertEquals(5, BookScan.getStringLength("hello"));
        }

        @Test
        public void testToggleCase_Normal() {
            assertEquals("hELLO wORLD", BookScan.toggleCase("Hello World"));
        }
    }

    @Nested
    class IntegrationTests {

        @Test
        public void testScanWordLength_IntegrationWithLength() {
            String text = "The quick brown fox\njumps over the lazy dog";
            // Testing target length 5 ("quick", "brown", "jumps")
            Map<String, Object> result = bookScan.scanWordLength(text, 5);

            assertEquals(3, result.get("totalCount"), "Should accurately find 3 words of length 5.");

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertEquals(2, lines.size());
            assertTrue(lines.contains(1)); // "quick", "brown" on line 1
            assertTrue(lines.contains(2)); // "jumps" on line 2
        }

        @Test
        public void testScanWordLength_And_ToggleCase_Integration() {
            // Scenario: Content has messy casing. We toggle the case first, then scan it.
            String originalText = "vALID tEST\nLINE TWO";
            String toggledText = BookScan.toggleCase(originalText); // "Valid Test\nline two"

            // "Valid" (5), "Test" (4), "line" (4), "two" (3)
            Map<String, Object> result = bookScan.scanWordLength(toggledText, 4);

            // "Test" on line 1 and "line" on line 2 -> total count 2, across lines 1 and 2
            assertEquals(2, result.get("totalCount"));

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertEquals(2, lines.size());
        }

        @Test
        public void testScanWordLength_And_SubstringCount_Integration() {
            // "apple" appears 3 times as isolated whole words, no other 5-letter words present.
            // substringCount is only a valid cross-check under these strict conditions.
            String text = "apple apple apple\norange";
            int targetLength = 5;

            Map<String, Object> result = bookScan.scanWordLength(text, targetLength);
            int detectedCount = (int) result.get("totalCount");

            // Cross-verify only using words that are exactly the target length and fully isolated
            String[] lines = text.split("\\r?\\n");
            int manualCount = 0;
            for (String line : lines) {
                String cleanLine = line.replaceAll("[^a-zA-Z0-9\\s]", "");
                for (String word : cleanLine.trim().split("\\s+")) {
                    if (word.length() == targetLength) manualCount++;
                }
            }

            assertEquals(manualCount, detectedCount,
                    "Scanner count should match a manual whole-word length scan, not a raw substring count.");

            // Also confirm the absolute value explicitly
            assertEquals(3, detectedCount);
        }
    }

    @Nested
    class BoundaryValueAnalysisTests {

        @Test
        public void testScanWordLength_NullAndEmptyInputs() {
            Map<String, Object> nullResult = bookScan.scanWordLength(null, 5);
            assertEquals(0, nullResult.get("totalCount"));
            assertTrue(((List<?>) nullResult.get("lineNumbers")).isEmpty());

            Map<String, Object> emptyResult = bookScan.scanWordLength("   ", 3);
            assertEquals(0, emptyResult.get("totalCount"));
        }

        @Test
        public void testScanWordLength_InvalidTargetLength() {
            String text = "Testing invalid boundaries.";
            Map<String, Object> zeroLengthResult = bookScan.scanWordLength(text, 0);
            Map<String, Object> negativeLengthResult = bookScan.scanWordLength(text, -5);

            assertEquals(0, zeroLengthResult.get("totalCount"));
            assertEquals(0, negativeLengthResult.get("totalCount"));
        }

        @Test
        public void testScanWordLength_PunctuationHandling() {
            // Words are surrounded by heavy punctuation chains
            String text = "Hello, world!!!\n...Ok...";

            // "Hello" (5), "world" (5), "Ok" (2)
            Map<String, Object> result = bookScan.scanWordLength(text, 5);
            assertEquals(2, result.get("totalCount"));

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertEquals(1, lines.size());
            assertEquals(1, lines.get(0));
        }
    }
    @Nested
    class AdditionalEdgeCaseTests {

        @Test
        public void testScanWordLength_SingleWordExactMatch() {
            // Text is just one word, and it exactly matches the target length
            String text = "Hello";
            Map<String, Object> result = bookScan.scanWordLength(text, 5);

            assertEquals(1, result.get("totalCount"));

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertEquals(1, lines.size());
            assertEquals(1, lines.get(0));
        }

        @Test
        public void testScanWordLength_NoMatchingWords() {
            // No word in the text matches the target length — should return 0 cleanly
            String text = "Hi there now";
            Map<String, Object> result = bookScan.scanWordLength(text, 10);

            assertEquals(0, result.get("totalCount"));

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertTrue(lines.isEmpty());
        }

        @Test
        public void testScanWordLength_MultipleMatchesOnSameLine_LineRecordedOnce() {
            // Two matching words on the same line — line number should only appear once
            String text = "cat bat rat";
            Map<String, Object> result = bookScan.scanWordLength(text, 3);

            assertEquals(3, result.get("totalCount"), "All 3 words should be counted.");

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertEquals(1, lines.size(), "Line 1 should appear only once even with multiple matches.");
            assertEquals(1, lines.get(0));
        }

        @Test
        public void testScanWordLength_NumbersAreCountedAsWords() {
            // The cleanup regex keeps digits — "123" has length 3 and should be counted
            String text = "go 123 hi";
            Map<String, Object> result = bookScan.scanWordLength(text, 3);

            assertEquals(1, result.get("totalCount"), "Numeric token '123' should match length 3.");
        }

        @Test
        public void testScanWordLength_ToggleCase_ThenVerifyLineTracking() {
            // After toggle, verify not just count but also which lines were affected
            String original = "HELLO WORLD\nfoo bar baz";
            String toggled = BookScan.toggleCase(original); // "hello world\nFOO BAR BAZ"

            Map<String, Object> result = bookScan.scanWordLength(toggled, 5);

            // "hello"(5), "world"(5) on line 1 — "FOO"(3), "BAR"(3), "BAZ"(3) on line 2
            assertEquals(2, result.get("totalCount"));

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertEquals(1, lines.size());
            assertEquals(1, lines.get(0), "Only line 1 should be recorded.");
        }

        @Test
        public void testScanWordLength_BlankLinesBetweenContent() {
            // Blank lines in the middle should not shift line numbering for real content
            String text = "hello\n\nworld";
            Map<String, Object> result = bookScan.scanWordLength(text, 5);

            assertEquals(2, result.get("totalCount"));

            @SuppressWarnings("unchecked")
            List<Integer> lines = (List<Integer>) result.get("lineNumbers");
            assertTrue(lines.contains(1), "Line 1 should contain 'hello'.");
            assertTrue(lines.contains(3), "Line 3 should contain 'world' (blank line shifts numbering).");
        }

        @Test
        public void testScanWordLength_LargeTargetLength_NoFalsePositives() {
            // Target length larger than any word in text — should never match
            String text = "one two three four five";
            Map<String, Object> result = bookScan.scanWordLength(text, 100);

            assertEquals(0, result.get("totalCount"));
            assertTrue(((List<?>) result.get("lineNumbers")).isEmpty());
        }

        @Test
        public void testSubstringCount_OverlappingSubstrings_DoNotOvercount() {
            // substringCount uses non-overlapping matches (advances by sub.length())
            // "aa" in "aaaa" matches at index 0 and 2 → 2 times, not 3
            assertEquals(2, BookScan.substringCount("aaaa", "aa"));
        }

        @Test
        public void testToggleCase_AllSameCase() {
            assertEquals("HELLO", BookScan.toggleCase("hello"));
            assertEquals("hello", BookScan.toggleCase("HELLO"));
        }

        @Test
        public void testToggleCase_WithDigitsAndSymbols() {
            // Digits and symbols should be unaffected by toggleCase
            assertEquals("hELLO 123 !@#", BookScan.toggleCase("Hello 123 !@#"));
        }
    }
}