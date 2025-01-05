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
