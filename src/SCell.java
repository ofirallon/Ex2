/**
 * A class representing a cell in a spreadsheet implementation.
 */
public class SCell implements Cell {
    private String cellData;
    private int dataType;
    private String calculatedValue;
    private final Ex2Sheet parentGrid;

    /**
     * Creates a new cell with initial content and parent reference.
     *
     * @param input initial cell content
     * @param parent reference to parent spreadsheet for formula evaluation
     */
    public SCell(String input, Ex2Sheet parent) {
        this.parentGrid = parent;
        setData(input);
        dataType = Ex2Utils.TEXT;  // Default type is text
    }

    /**
     * Determines if cell content represents a valid number.
     * Attempts to parse content as double after trimming whitespace.
     *
     * @return true if content is a valid number, false otherwise
     */
    public boolean isNumber() {
        String content = getCellData();
        if (content == null || content.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(content.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Determines if cell contains text content.
     * Content is considered text if it's neither a number nor a formula.
     * @return true if content is text, false otherwise
     */
    public boolean isText() {
        return !isNumber() && !isForm();
    }

    /**
     * Checks if cell contains a formula.
     * Formulas must start with '=' character.
     * @return true if content is a formula, false otherwise
     */
    public boolean isForm() {
        String content = getCellData();
        return content != null && content.startsWith("=");
    }

    /**
     * Validates if a character is a supported arithmetic operator.
     * Supported operators are defined in Ex2Utils.M_OPS.
     * @param symbol character to check
     * @return true if character is valid arithmetic operator, false otherwise
     */
    private boolean isArithmeticSymbol(char symbol) {
        for (String validOp : Ex2Utils.M_OPS) {
            if (validOp.charAt(0) == symbol) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates proper parentheses matching in an expression.
     * Ensures that:
     * - Opening and closing parentheses match
     * - No closing parenthesis appears before matching opening
     * @param expression formula expression to validate
     * @return true if parentheses are properly matched, false otherwise
     */
    private boolean validateParentheses(String expression) {
        int nestLevel = 0;
        for (char symbol : expression.toCharArray()) {
            if (symbol == '(') nestLevel++;
            if (symbol == ')') nestLevel--;
            if (nestLevel < 0) return false;  // More closing than opening
        }
        return nestLevel == 0;  // All parentheses matched
    }

    /**
     * Executes basic arithmetic operation between two numbers.
     * @param firstNum first operand
     * @param secondNum second operand
     * @param operation arithmetic operator (+, -, *, /)
     * @return result of operation, or null if operation invalid
     * @throws ArithmeticException implicitly on division by zero
     */
    private Double executeOperation(Double firstNum, Double secondNum, char operation) {
        return switch (operation) {
            case '+' -> firstNum + secondNum;
            case '-' -> firstNum - secondNum;
            case '*' -> firstNum * secondNum;
            case '/' -> firstNum / secondNum;
            default -> null;
        };
    }

    /**
     * Computes result of formula expression.
     * Handles:
     * - Basic arithmetic (+, -, *, /)
     * - Parentheses nesting
     * - Cell references
     * - Order of operations
     * - Error conditions
     * @param input formula expression to evaluate
     * @return computed result, or null if invalid formula
     */

    public Double computeForm(String input) {
        return new FormulaEvaluator(input).process();
    }

    private class FormulaEvaluator {
        private abstract class EvaluationState {
            public EvaluationState nextState;

            abstract Double evaluate(String expr);
        }

        private class InitialState extends EvaluationState {
            @Override
            Double evaluate(String expr) {
                if (expr == null || expr.isEmpty()) return null;
                String processed = expr.startsWith("=") ?
                        expr.substring(1).trim().replaceAll("\\s+", "") :
                        expr.replaceAll("\\s+", "");
                return nextState.evaluate(processed);
            }
        }

        private class DirectNumberState extends EvaluationState {
            @Override
            Double evaluate(String expr) {
                try {
                    return Double.parseDouble(expr);
                } catch (NumberFormatException ignored) {
                    return nextState.evaluate(expr);
                }
            }
        }

        private class OperatorValidationState extends EvaluationState {
            @Override
            Double evaluate(String expr) {
                for (int i = 0; i < expr.length() - 1; i++) {
                    if (isArithmeticSymbol(expr.charAt(i)) &&
                            isArithmeticSymbol(expr.charAt(i + 1))) {
                        return null;
                    }
                }
                return nextState.evaluate(expr);
            }
        }

        private class ParenthesesState extends EvaluationState {
            @Override
            Double evaluate(String expr) {
                while (expr.startsWith("(") && expr.endsWith(")")) {
                    String inner = expr.substring(1, expr.length() - 1);
                    if (!validateParentheses(inner)) break;
                    expr = inner;
                    try {
                        return Double.parseDouble(expr);
                    } catch (NumberFormatException ignored) {}
                }
                return nextState.evaluate(expr);
            }
        }

        private class CellReferenceState extends EvaluationState {
            @Override
            Double evaluate(String expr) {
                if (expr.matches("^[A-Za-z][0-9]+$")) {
                    CellEntry ref = CellEntry.parseEntry(expr);
                    if (ref != null && ref.isValid()) {
                        try {
                            return Double.parseDouble(parentGrid.value(ref.getX(), ref.getY()));
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                    }
                    return null;
                }
                return nextState.evaluate(expr);
            }
        }

        private class OperatorState extends EvaluationState {
            @Override
            Double evaluate(String expr) {
                int[] opInfo = findOperator(expr);
                int opIdx = opInfo[0];

                if (opIdx == -1) {
                    try {
                        return Double.parseDouble(expr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }

                char operator = expr.charAt(opIdx);
                String leftPart = expr.substring(0, opIdx).trim();
                String rightPart = expr.substring(opIdx + 1).trim();

                return evaluateOperation(leftPart, rightPart, operator);
            }

            private int[] findOperator(String expr) {
                int opIdx = -1;
                int depth = 0;
                boolean foundMulDiv = false;

                for (int i = expr.length() - 1; i >= 0; i--) {
                    char c = expr.charAt(i);
                    if (c == ')') depth++;
                    else if (c == '(') depth--;
                    else if (depth == 0) {
                        if ((c == '+' || c == '-') &&
                                (i == 0 || !isArithmeticSymbol(expr.charAt(i - 1)))) {
                            return new int[]{i};
                        } else if (!foundMulDiv && (c == '*' || c == '/')) {
                            opIdx = i;
                            foundMulDiv = true;
                        }
                    }
                }
                return new int[]{opIdx};
            }

            private Double evaluateOperation(String left, String right, char op) {
                if (left.isEmpty()) {
                    if (op == '-') {
                        try {
                            return -Double.parseDouble(right);
                        } catch (NumberFormatException e) {
                            Double rightResult = computeForm(right);
                            return rightResult != null ? -rightResult : null;
                        }
                    }
                    return computeForm(right);
                }

                Double leftVal = computeForm(left);
                Double rightVal = computeForm(right);

                if (leftVal == null || rightVal == null) return null;

                return executeOperation(leftVal, rightVal, op);
            }
        }

        private final EvaluationState initialState;
        private EvaluationState nextState;

        public FormulaEvaluator(String input) {
            // Initialize states
            initialState = new InitialState();
            EvaluationState directNumber = new DirectNumberState();
            EvaluationState operatorValidation = new OperatorValidationState();
            EvaluationState parentheses = new ParenthesesState();
            EvaluationState cellReference = new CellReferenceState();
            EvaluationState operator = new OperatorState();

            // Set up chain
            initialState.nextState = directNumber;
            directNumber.nextState = operatorValidation;
            operatorValidation.nextState = parentheses;
            parentheses.nextState = cellReference;
            cellReference.nextState = operator;
            operator.nextState = null;

            this.expression = input;
        }

        public Double process() {
            return initialState.evaluate(expression);
        }

        private final String expression;
    }

    @Override
    public String toString() {
        if (calculatedValue != null) {
            try {
                double value = Double.parseDouble(calculatedValue);
                return formatNumericOutput(value, calculatedValue);
            } catch (NumberFormatException e) {
                return calculatedValue;
            }
        }

        String data = getCellData();
        if (data == null || data.isEmpty()) {
            return "";
        }

        if (isForm()) {
            Double computedResult = computeForm(data);
            if (computedResult != null) {
                return formatNumericOutput(computedResult, computedResult.toString());
            }
            return "ERR_FORM!";
        }

        if (isNumber()) {
            try {
                double parsedValue = Double.parseDouble(data);
                return formatNumericOutput(parsedValue, data);
            } catch (NumberFormatException e) {
                return data;
            }
        }

        return data;
    }

    /**
     * Formats numeric value according to magnitude.
     * Uses scientific notation for:
     * - Values >= 1e6
     * - Values < 1e-6 (except 0)
     * @param val numeric value to format
     * @param defaultReturn fallback string if formatting fails
     * @return formatted string representation
     */
    private String formatNumericOutput(double val, String defaultReturn) {
        if (Double.isInfinite(val)) {
            return defaultReturn;
        }

        boolean isExtremeValue = Math.abs(val) >= 1e6 ||
                (Math.abs(val) < 1e-6 && val != 0);

        return isExtremeValue ? String.format("%.1e", val)
                : String.format("%.1f", val);
    }

    /**
     * Sets cell data and determines its type.
     * @param data new cell content
     */
    @Override
    public void setData(String data) {
        cellData = data;

        if (data == null || data.trim().isEmpty()) {
            setType(Ex2Utils.TEXT);
        }
        else if (data.startsWith("=")) {
            setType(Ex2Utils.FORM);
        }
        else if (isNumber()) {
            setType(Ex2Utils.NUMBER);
        }
        else if (isText()) {
            setType(Ex2Utils.TEXT);
        }
    }

    /**
     * Gets raw cell data.
     * @return cell's raw content
     */
    @Override
    public String getData() {
        return cellData;
    }

    /**
     * Internal accessor for cell data.
     * @return cell's raw content
     */
    private String getCellData() {
        return cellData;
    }

    /**
     * Gets cell's data type.
     * @return cell type (TEXT, NUMBER, or FORMULA)
     */
    @Override
    public int getType() {
        return dataType;
    }

    /**
     * Sets cell's data type.
     * @param newType new type to set
     */
    @Override
    public void setType(int newType) {
        dataType = newType;
    }

    /**
     * Gets cell order (unused).
     *
     * @return always 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Sets cell order (unused).
     *
     * @param order ignored
     */
    @Override
    public void setOrder(int order) {
    }

    /**
     * Sets processed/calculated value.
     * @param value processed value to set
     */
    public void setProcessedValue(String value) {
        this.calculatedValue = value;
    }

    /**
     * Gets processed/calculated value.
     * @return processed value or null if not calculated
     */
    public String getProcessedValue() {
        return calculatedValue;
    }
}