/**
 * Represents a cell's coordinates in a spreadsheet (e.g., "A0", "B1", etc.)
 */
public class CellEntry implements Index2D {
    private final int x;
    private final int y;

    /**
     * Creates a cell entry with given coordinates
     * @param x Column index (0-25 for A-Z)
     * @param y Row index (0-99)
     */
    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets column index
     * @return Column index (0-25)
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * Gets row index
     * @return Row index (0-99)
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * Checks if coordinates are within valid range
     * @return true if coordinates are valid (A-Z for columns, 0-99 for rows)
     */
    @Override
    public boolean isValid() {
        return x >= 0 && x < 26 && y >= 0 && y < 100;
    }

    /**
     * Extracts column index from cell reference
     * @param c Cell reference string (e.g., "F13")
     * @return Column index (0-25) or -1 if invalid
     */
    public static int xCell(String c) {
        if (c == null || c.isEmpty()) {
            return -1;
        }
        char firstChar = Character.toUpperCase(c.charAt(0));
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return firstChar - 'A';
        }
        return -1;
    }

    /**
     * Extracts row index from cell reference
     * @param c Cell reference string (e.g., "F13")
     * @return Row index (0-99) or -1 if invalid
     */
    public static int yCell(String c) {
        if (c == null || c.length() < 2) {
            return -1;
        }
        try {
            int row = Integer.parseInt(c.substring(1));
            return (row >= 0 && row < 100) ? row : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Creates a CellEntry from a cell reference string
     * @param cellRef Cell reference (e.g., "A0", "F13")
     * @return New CellEntry object or null if invalid
     */
    public static CellEntry parseEntry(String cellRef) {
        int x = xCell(cellRef);
        int y = yCell(cellRef);
        if (x != -1 && y != -1) {
            return new CellEntry(x, y);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%c%d", (char)('A' + x), y);
    }
}