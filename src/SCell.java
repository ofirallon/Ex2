// Add your documentation below:

public class SCell implements Cell {
    private String line;
    private int type;
    // Add your code here

    public boolean isNumber(){
        if (getData() == null || getData().isEmpty()) {
            return false;
        }
        int start = 0;
        if (getData().charAt(0) == '-') {
            start = 1; // start from the character after the minus
        }
        boolean hasDecimalPoint = false;
        for (int i = start; i < getData().length(); i++) {
            char c = getData().charAt(i);
            if (c == '.') {
                if (hasDecimalPoint) {
                    return false; // if there is more than one decimal point
                }
                hasDecimalPoint = true;
            }
            else
                if (!Character.isDigit(c)) {
                    return false; // if the character is not a number
            }
        }
        return true; // If all tests pass, this is a valid number
    }

    public boolean isText(){
        // If the text is null or empty, it is not considered as text
        if (getData() == null || getData().isEmpty()) {
            return false;
        }

        // Check if it's a number
        if (isNumber()) {
            return false; // Numbers are not text
        }

        // Check if this is a formula
        if (isForm()) {
            return false; // Formulas are not text
        }

        // If it's neither a number nor a formula, it's text
        return true;
    }

    public boolean isForm() {
        // Check if the string starts with "="
        if (getData() == null || !getData().startsWith("=")) {
            return false;
        }

        // Remove the "=" and trim the expression
        String form = getData().substring(1).trim();

        // Check if the expression is empty
        if (form.isEmpty()) {
            return false;
        }

        // Check if the formula contains only valid characters
        if (!form.matches("[0-9A-Za-z+\\-*/(). ]+")) {
            return false;
        }

        // Check if the parentheses are balanced
        if (!areParenthesesBalanced(form)) {
            return false;
        }

        // Check if mathematical operators are placed correctly (e.g., not at the start or end of the expression)
        if (!CorrectPlacementOfMathematicalOperationSymbols(form)) {
            return false;
        }

        // If all checks pass, the formula is valid
        return true;
    }

    private boolean areParenthesesBalanced(String expression) {
        int balance = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) {
                    return false; // Closing parenthesis without a matching opening parenthesis
                }
            }
        }
        return balance == 0; // Must be balanced at the end
    }

    private boolean CorrectPlacementOfMathematicalOperationSymbols(String expression) {
        // Check if the expression starts or ends with a mathematical operator
        if (expression.matches("^[+\\-*/]|[+\\-*/]$")) {
            return false;
        }

        // Check for double operators like "++" or "**"
        if (expression.contains("++") || expression.contains("--") || expression.contains("**") || expression.contains("//")) {
            return false;
        }

        return true;
    }

    public Double computeForm(String form) {
        // Check if the formula is null or empty
        if (form == null || form.isEmpty() || !form.startsWith("=")) {
            return null; // In case of an error, return null
        }

        // Remove the "=" symbol
        String expression = form.substring(1).trim();

        // Recursive computation
        return evaluateRecursive(expression);
    }

    private Double evaluateRecursive(String expression) {
        expression = expression.trim();

        // If this is a simple number, return its value
        if (isNumber()) {
            return Double.parseDouble(expression);
        }

        // If this is an expression inside parentheses (e.g., "(1+2)"), compute the inner expression
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return evaluateRecursive(expression.substring(1, expression.length() - 1).trim());
        }

        // Find the main operator in the expression (e.g., "+", "-", "*", "/")
        int mainOpIndex = findMainOperator(expression);
        if (mainOpIndex == -1) {
            return null; // Invalid expression
        }

        // Split the expression into two parts based on the main operator
        String left = expression.substring(0, mainOpIndex).trim();
        String right = expression.substring(mainOpIndex + 1).trim();
        char operator = expression.charAt(mainOpIndex);

        // Compute the values of the left and right parts recursively
        Double leftValue = evaluateRecursive(left);
        Double rightValue = evaluateRecursive(right);

        // If one of the parts is invalid, return null
        if (leftValue == null || rightValue == null) {
            return null;
        }

        // Perform computation based on the operator
        if (operator == '+') {
            return leftValue + rightValue;
        } else if (operator == '-') {
            return leftValue - rightValue;
        } else if (operator == '*') {
            return leftValue * rightValue;
        } else if (operator == '/') {
            if (rightValue == 0) {
                return Double.POSITIVE_INFINITY; // Division by zero returns infinity
            }
            return leftValue / rightValue;
        }

        // If the operator is invalid, return null
        return null;
    }

    private static int findMainOperator(String expression) {
        int balance = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
            } else if ((c == '+' || c == '-' || c == '*' || c == '/') && balance == 0) {
                // Found the main operator at the highest level
                return i;
            }
        }
        return -1; // No main operator found
    }


    public SCell(String s) {
        // Add your code here
        setData(s);
    }

    @Override
    public int getOrder() {
        // Add your code here

        return 0;
        // ///////////////////
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
public void setData(String s) {
        // Add your code here
        line = s;
        /////////////////////
    }
    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // Add your code here

    }
}
