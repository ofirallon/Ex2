import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class representing a spreadsheet implementation.
 */
public class Ex2Sheet implements Sheet {
    /** The 2D array storing all cells in the spreadsheet */
    private SCell[][] spreadsheetData;

    /**
     * Creates a new spreadsheet with specified dimensions.
     *
     * @param rows number of rows in the spreadsheet
     * @param cols number of columns in the spreadsheet
     */
    public Ex2Sheet(int rows, int cols) {
        spreadsheetData = new SCell[rows][cols];
        createEmptySheet();
        refreshAllCells();
    }

    /**
     * Default constructor using Ex2Utils dimensions.
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    /**
     * Initializes empty cells in the spreadsheet.
     */
    private void createEmptySheet() {
        for (int r = 0; r < spreadsheetData.length; r++) {
            for (int c = 0; c < spreadsheetData[0].length; c++) {
                spreadsheetData[r][c] = new SCell(Ex2Utils.EMPTY_CELL, this);
            }
        }
    }

    /**
     * Validates coordinates within spreadsheet bounds.
     *
     * @param row row index
     * @param col column index
     * @return true if coordinates are valid
     */
    @Override
    public boolean isIn(int row, int col) {
        return row >= 0 && col >= 0 && row < width() && col < height();
    }

    /**
     * Retrieves calculated cell value.
     *
     * @param row row index
     * @param col column index
     * @return calculated cell value or EMPTY_CELL if invalid
     */
    @Override
    public String value(int row, int col) {
        if (!checkBoundaries(row, col)) return Ex2Utils.EMPTY_CELL;

        SCell currentCell = spreadsheetData[row][col];
        String calculatedValue = currentCell.getProcessedValue();

        if (calculatedValue != null) return calculatedValue;

        refreshCell(row, col);
        return currentCell.getProcessedValue();
    }

    /**
     * Validates coordinate boundaries.
     */
    private boolean checkBoundaries(int row, int col) {
        return row >= 0 && col >= 0 && row < width() && col < height();
    }

    /**
     * Gets cell at specified position.
     */
    @Override
    public Cell get(int row, int col) {
        return checkBoundaries(row, col) ? spreadsheetData[row][col] : null;
    }

    /**
     * Gets cell by reference (e.g., "A1").
     */
    @Override
    public Cell get(String cellRef) {
        CellEntry cellPos = CellEntry.parseEntry(cellRef);
        return (cellPos != null && cellPos.isValid()) ?
                get(cellPos.getX(), cellPos.getY()) : null;
    }

    @Override
    public int width() {
        return spreadsheetData.length;
    }

    @Override
    public int height() {
        return spreadsheetData[0].length;
    }

    /**
     * Sets cell content.
     */
    @Override
    public void set(int row, int col, String content) {
        if (!checkBoundaries(row, col)) return;
        String finalContent = (content == null || content.trim().isEmpty()) ?
                Ex2Utils.EMPTY_CELL : content;
        spreadsheetData[row][col] = new SCell(finalContent, this);
    }

    /**
     * Evaluates entire spreadsheet.
     * Analyzes dependencies and updates all cells.
     */
    @Override
    public void eval() {
        int[][] dependencyLevels = analyzeSheetDependencies();
        clearCalculatedValues();
        calculateByDependencyLevel(dependencyLevels);
    }

    /**
     * Clears all calculated values for fresh evaluation.
     */
    private void clearCalculatedValues() {
        for (SCell[] row : spreadsheetData) {
            for (SCell cell : row) {
                cell.setProcessedValue(null);
            }
        }
    }

    /**
     * Calculates cells based on dependency levels.
     */
    private void calculateByDependencyLevel(int[][] levels) {
        int maxLevel = findHighestLevel(levels);
        // Process each level in order
        for (int currentLevel = 0; currentLevel <= maxLevel; currentLevel++) {
            processCurrentLevel(levels, currentLevel);
        }
    }

    /**
     * Processes cells at specified dependency level.
     */
    private void processCurrentLevel(int[][] levels, int targetLevel) {
        for (int r = 0; r < width(); r++) {
            for (int c = 0; c < height(); c++) {
                if (levels[r][c] == targetLevel) {
                    refreshCell(r, c);
                }
            }
        }
    }

    /**
     * Refreshes individual cell value.
     * Handles formulas, numbers, and text appropriately.
     */
    private void refreshCell(int row, int col) {
        if (!checkBoundaries(row, col)) return;

        SCell cell = spreadsheetData[row][col];
        String rawValue = cell.getData();

        if (isEmptyContent(rawValue)) {
            cell.setProcessedValue("");
            return;
        }

        if (rawValue.startsWith("=")) {
            handleFormulaCell(row, col, cell, rawValue);
            return;
        }

        if (cell.isNumber()) {
            handleNumericCell(cell, rawValue);
            return;
        }

        handleTextCell(cell, rawValue);
    }

    private boolean isEmptyContent(String content) {
        return content == null || content.trim().isEmpty();
    }

    /**
     * Processes formula cells, checking for circular references.
     */
    private void handleFormulaCell(int row, int col, SCell cell, String formula) {
        if (checkForCircularReference(row, col)) {
            markCellAsCircular(cell);
            return;
        }

        calculateFormula(cell, formula);
    }

    private boolean checkForCircularReference(int row, int col) {
        return calculateDependencyDepth(row, col, new boolean[width()][height()])
                == Ex2Utils.ERR_CYCLE_FORM;
    }

    private void markCellAsCircular(SCell cell) {
        cell.setType(Ex2Utils.ERR_CYCLE_FORM);
        cell.setProcessedValue(Ex2Utils.ERR_CYCLE);
    }

    /**
     * Calculates formula result and sets cell value.
     */
    private void calculateFormula(SCell cell, String formula) {
        Double result = cell.computeForm(formula);
        if (result != null) {
            cell.setType(Ex2Utils.FORM);
            cell.setProcessedValue(String.format("%.1f", result));
        } else {
            cell.setType(Ex2Utils.ERR_FORM_FORMAT);
            cell.setProcessedValue(Ex2Utils.ERR_FORM);
        }
    }

    /**
     * Handles numeric cell processing.
     */
    private void handleNumericCell(SCell cell, String content) {
        try {
            double num = Double.parseDouble(content);
            cell.setType(Ex2Utils.NUMBER);
            cell.setProcessedValue(String.format("%.1f", num));
        } catch (NumberFormatException e) {
            handleTextCell(cell, content);
        }
    }

    /**
     * Handles text cell processing.
     */
    private void handleTextCell(SCell cell, String content) {
        cell.setType(Ex2Utils.TEXT);
        cell.setProcessedValue(content);
    }

    /**
     * Returns dependency depths for all cells.
     */
    @Override
    public int[][] depth() {
        return analyzeSheetDependencies();
    }

    /**
     * Analyzes cell dependencies across the spreadsheet.
     * @return 2D array of dependency levels
     */
    private int[][] analyzeSheetDependencies() {
        int[][] dependencies = new int[width()][height()];
        boolean[][] processed = new boolean[width()][height()];

        for (int r = 0; r < width(); r++) {
            for (int c = 0; c < height(); c++) {
                if (!processed[r][c]) {
                    dependencies[r][c] = calculateDependencyDepth(r, c,
                            new boolean[width()][height()]);
                }
            }
        }
        return dependencies;
    }

    /**
     * Calculates dependency depth for a specific cell.
     * Handles circular references and formula parsing.
     */
    private int calculateDependencyDepth(int row, int col, boolean[][] visitMap) {
        if (!checkBoundaries(row, col)) return Ex2Utils.ERR_CYCLE_FORM;

        Cell cell = spreadsheetData[row][col];
        String content = cell.getData();

        if (!isFormulaContent(content)) return 0;
        if (isScientificNotation(content)) return 0;

        Matcher refMatcher = Pattern.compile("[A-Za-z][0-9]+").matcher(content);
        if (!refMatcher.find()) return 0;

        refMatcher.reset();
        return processCellReferences(row, col, refMatcher, visitMap);
    }

    private boolean isFormulaContent(String content) {
        return content != null && !content.trim().isEmpty() && content.startsWith("=");
    }

    private boolean isScientificNotation(String content) {
        return content.substring(1).trim().matches("^-?\\d*\\.?\\d+[eE][-+]?\\d+$");
    }

    /**
     * Processes cell references in formulas.
     * Handles dependency tracking and circular reference detection.
     */
    private int processCellReferences(int row, int col, Matcher matcher,
                                      boolean[][] visitMap) {
        int maxDepth = 0;
        boolean[][] tempVisitMap = new boolean[width()][height()];
        copyVisitMap(visitMap, tempVisitMap);
        tempVisitMap[row][col] = true;

        while (matcher.find()) {
            CellEntry ref = CellEntry.parseEntry(matcher.group());
            if (!isValidReference(ref)) return Ex2Utils.ERR_CYCLE_FORM;

            int refRow = ref.getX(), refCol = ref.getY();
            if (!isValidReferenceCell(refRow, refCol, visitMap)) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }

            int depth = calculateDependencyDepth(refRow, refCol, tempVisitMap);
            if (depth == Ex2Utils.ERR_CYCLE_FORM) return depth;

            maxDepth = Math.max(maxDepth, depth);
        }

        copyVisitMap(tempVisitMap, visitMap);
        return maxDepth + 1;
    }

    private boolean isValidReference(CellEntry ref) {
        return ref != null && ref.isValid();
    }

    private boolean isValidReferenceCell(int row, int col, boolean[][] visitMap) {
        if (!checkBoundaries(row, col)) return false;

        Cell cell = spreadsheetData[row][col];
        if (cell == null || isEmptyContent(cell.getData())) return false;

        return !visitMap[row][col];
    }

    private void copyVisitMap(boolean[][] source, boolean[][] target) {
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, target[i], 0, source[i].length);
        }
    }

    private int findHighestLevel(int[][] levels) {
        int max = 0;
        for (int[] row : levels) {
            for (int level : row) {
                max = Math.max(max, level);
            }
        }
        return max;
    }

    /**
     * Evaluates a specific cell.
     */
    @Override
    public String eval(int row, int col) {
        if (!checkBoundaries(row, col)) return null;

        refreshAllCells();

        SCell cell = spreadsheetData[row][col];
        String calculated = cell.getProcessedValue();
        return calculated != null && !calculated.isEmpty() ? calculated : cell.getData();
    }

    private void refreshAllCells() {
        eval();
    }

    /**
     * Saves spreadsheet to file.
     * Format: dimensions on first line, followed by cell data.
     */
    @Override
    public void save(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writeDimensions(writer);
            writeSheetData(writer);
        }
    }

    /**
     * @param writer the BufferedWriter to write to
     * @throws IOException if an error occurs during writing
     */
    private void writeDimensions(BufferedWriter writer) throws IOException {
        writer.write(width() + "," + height() + "\n");
    }

    /**
     * @param writer the BufferedWriter to write to
     * @throws IOException if an error occurs during writing
     */
    private void writeSheetData(BufferedWriter writer) throws IOException {
        for (int r = 0; r < width(); r++) {
            for (int c = 0; c < height(); c++) {
                writeCell(writer, r, c);
                // Add comma separator between cells (except for last cell in row)
                if (c < height() - 1) writer.write(",");
            }
            writer.write("\n");
        }
    }

    /**
     * @param writer the BufferedWriter to write to
     * @param row the row index of the cell
     * @param col the column index of the cell
     * @throws IOException if an error occurs during writing
     */
    private void writeCell(BufferedWriter writer, int row, int col)
            throws IOException {
        String content = spreadsheetData[row][col].getData();
        // Handle empty cells and escape special characters
        content = isEmptyContent(content) ? "EMPTY" :
                content.replace(",", "\\,").replace("\n", "\\n");
        writer.write(content);
    }

    /**
     * @param filename the path and name of the file to load from
     * @throws IOException if an error occurs during file reading
     * @throws NumberFormatException if the dimensions line is malformed
     */
    @Override
    public void load(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String[] dims = reader.readLine().split(",");
            loadSheetContent(reader,
                    Integer.parseInt(dims[0]),
                    Integer.parseInt(dims[1]));
        }
        refreshAllCells();  // Recalculate all cell values after loading
    }

    /**
     * @param reader the BufferedReader to read from
     * @param w the width (number of rows) of the spreadsheet
     * @param h the height (number of columns) of the spreadsheet
     * @throws IOException if an error occurs during reading
     */
    private void loadSheetContent(BufferedReader reader, int w, int h)
            throws IOException {
        spreadsheetData = new SCell[w][h];

        for (int r = 0; r < w; r++) {
            // Split on commas that aren't escaped
            String[] rowData = reader.readLine().split("(?<!\\\\),");
            for (int c = 0; c < h; c++) {
                String content = rowData[c]
                        .replace("\\,", ",")    // Unescape commas
                        .replace("\\n", "\n");  // Unescape newlines
                content = "EMPTY".equals(content) ? "" : content;
                spreadsheetData[r][c] = new SCell(content, this);
            }
        }
    }
}