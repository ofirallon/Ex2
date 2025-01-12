import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 * Comprehensive Test Suite for Advanced Spreadsheet Implementation
 * Tests all core functionalities and edge cases of the spreadsheet system.
 */
public class Ex2Test {
    private Ex2Sheet testSheet;

    @Before
    public void setUp() {
        testSheet = new Ex2Sheet();
    }

    /**
     * Test 1: Basic text cell handling
     */
    @Test
    public void testBasicTextCell() {
        testSheet.set(0, 0, "Hello");
        assertEquals("Hello", testSheet.value(0, 0));
    }

    /**
     * Test 2: Basic numeric cell handling
     */
    @Test
    public void testBasicNumericCell() {
        testSheet.set(0, 0, "42");
        assertEquals("42.0", testSheet.value(0, 0));
    }

    /**
     * Test 3: Empty cell handling
     */
    @Test
    public void testEmptyCell() {
        assertEquals("", testSheet.value(0, 0));
    }

    /**
     * Test 4: Cell type identification
     */
    @Test
    public void testCellTypeIdentification() {
        testSheet.set(0, 0, "123");
        testSheet.set(0, 1, "abc");
        testSheet.set(0, 2, "=1+1");

        assertTrue(((SCell)testSheet.get(0, 0)).isNumber());
        assertTrue(((SCell)testSheet.get(0, 1)).isText());
        assertTrue(((SCell)testSheet.get(0, 2)).isForm());
    }

    /**
     * Test 5: Cell boundary validation
     */
    @Test
    public void testCellBoundaries() {
        assertFalse(testSheet.isIn(-1, 0));
        assertFalse(testSheet.isIn(0, -1));
        assertTrue(testSheet.isIn(0, 0));
    }

    /**
     * Test 6: Basic arithmetic operations
     */
    @Test
    public void testBasicArithmetic() {
        testSheet.set(0, 0, "=5+3");
        testSheet.set(0, 1, "=10-4");
        testSheet.set(0, 2, "=3*6");
        testSheet.set(0, 3, "=15/3");

        assertEquals("8.0", testSheet.value(0, 0));
        assertEquals("6.0", testSheet.value(0, 1));
        assertEquals("18.0", testSheet.value(0, 2));
        assertEquals("5.0", testSheet.value(0, 3));
    }

    /**
     * Test 7: Complex formula evaluation
     */
    @Test
    public void testComplexFormulas() {
        testSheet.set(0, 0, "=(2+3)*(4-1)");
        assertEquals("15.0", testSheet.value(0, 0));
    }

    /**
     * Test 8: Cell reference handling
     */
    @Test
    public void testCellReferences() {
        testSheet.set(0, 0, "10");
        testSheet.set(0, 1, "=A0*2");
        assertEquals("20.0", testSheet.value(0, 1));
    }

    /**
     * Test 9: Nested parentheses
     */
    @Test
    public void testNestedParentheses() {
        testSheet.set(0, 0, "=((2+3)*(4+5))");
        assertEquals("45.0", testSheet.value(0, 0));
    }

    /**
     * Test 10: Formula error handling
     */
    @Test
    public void testFormulaErrors() {
        testSheet.set(0, 0, "=1++2");
        assertEquals("ERR_FORM!", testSheet.value(0, 0));
    }


    /**
     * Test 11: Scientific notation
     */
    @Test
    public void testScientificNotation() {
        testSheet.set(0, 0, "1e5");
        assertEquals("100000.0", testSheet.value(0, 0));
    }

    /**
     * Test 12: Division by zero
     */
    @Test
    public void testDivisionByZero() {
        testSheet.set(0, 0, "=1/0");
        assertEquals("Infinity", testSheet.value(0, 0));
    }

    /**
     * Test 13: Circular references
     */
    @Test
    public void testCircularReferences() {
        testSheet.set(0, 0, "=A2");
        testSheet.set(0, 1, "=A1");
        assertEquals("ERR_CYCLE!", testSheet.value(0, 0));
    }

    /**
     * Test 14: Whitespace handling
     */
    @Test
    public void testWhitespaceHandling() {
        testSheet.set(0, 0, "  42  ");
        assertEquals("42.0", testSheet.value(0, 0));
    }

    /**
     * Test 15: Null input handling
     */
    @Test
    public void testNullInput() {
        testSheet.set(0, 0, null);
        assertEquals("", testSheet.value(0, 0));
    }

    /**
     * Test Group 4: File Operations (Tests 16-20)
     */

    /**
     * Test 16: Basic save and load
     */
    @Test
    public void testBasicSaveLoad() throws IOException {
        testSheet.set(0, 0, "Test");
        testSheet.save("test.csv");

        Ex2Sheet loaded = new Ex2Sheet();
        loaded.load("test.csv");
        assertEquals("Test", loaded.value(0, 0));

        new File("test.csv").delete();
    }

    /**
     * Test 17: Special character handling in files
     */
    @Test
    public void testSpecialCharacters() throws IOException {
        testSheet.set(0, 0, "Test,With,Commas");
        testSheet.save("test.csv");

        Ex2Sheet loaded = new Ex2Sheet();
        loaded.load("test.csv");
        assertEquals("Test,With,Commas", loaded.value(0, 0));

        new File("test.csv").delete();
    }

    /**
     * Test 18: Formula persistence
     */
    @Test
    public void testFormulaPersistence() throws IOException {
        testSheet.set(0, 0, "=1+1");
        testSheet.save("test.csv");

        Ex2Sheet loaded = new Ex2Sheet();
        loaded.load("test.csv");
        assertEquals("2.0", loaded.value(0, 0));

        new File("test.csv").delete();
    }

    /**
     * Test 19: Large spreadsheet save/load
     */
    @Test
    public void testLargeSpreadsheet() throws IOException {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                testSheet.set(i, j, String.valueOf(i*j));
            }
        }
        testSheet.save("test.csv");

        Ex2Sheet loaded = new Ex2Sheet();
        loaded.load("test.csv");

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(testSheet.value(i, j), loaded.value(i, j));
            }
        }

        new File("test.csv").delete();
    }

    /**
     * Test 20: Empty file handling
     */
    @Test
    public void testEmptyFile() throws IOException {
        testSheet.save("test.csv");

        Ex2Sheet loaded = new Ex2Sheet();
        loaded.load("test.csv");
        assertEquals("", loaded.value(0, 0));

        new File("test.csv").delete();
    }

    /**
     * Test Group 5: Advanced Features (Tests 21-25)
     */

    /**
     * Test 21: Complex cell dependencies
     */
    @Test
    public void testComplexDependencies() {
        testSheet.set(0, 0, "1");
        testSheet.set(0, 1, "=A0*2");
        testSheet.set(0, 2, "=A1*2");
        assertEquals("4.0", testSheet.value(0, 2));
    }

    /**
     * Test 22: Mixed operation types
     */
    @Test
    public void testMixedOperations() {
        testSheet.set(0, 0, "=2.5*3+4/2");
        assertEquals("9.5", testSheet.value(0, 0));
    }

    /**
     * Test 23: Error propagation
     */
    @Test
    public void testErrorPropagation() {
        testSheet.set(0, 0, "=1/0");
        testSheet.set(0, 1, "=A0*2");
        assertEquals("Infinity", testSheet.value(0, 1));
    }

    /**
     * Test 24: Multiple cell references
     */
    @Test
    public void testMultipleCellRefs() {
        testSheet.set(0, 0, "1");
        testSheet.set(0, 1, "2");
        testSheet.set(0, 2, "=A0+A1");
        assertEquals("3.0", testSheet.value(0, 2));
    }

    /**
     * Test 25: Edge case formatting
     */
    @Test
    public void testEdgeCaseFormatting() {
        testSheet.set(0, 0, "1e5");
        assertEquals("100000.0", testSheet.value(0, 0));
    }
}