# Ex2 - Foundation of Object-Oriented and Recursion

## Overview

This project implements a **basic yet advanced spreadsheet system** as part of the Introduction to Computer Science course at Ariel University. The system is a 2D array of "Cells," where each cell may store one of the following types of data:

![image](https://github.com/user-attachments/assets/1b97ddba-ccf2-4a17-b050-30e010e65e53)


- **Text** (String values like "Hello World")
- **Numbers** (Double values like `42.5` or `3.14`)
- **Formulas** (Expressions like `=A1+B2*2`)

The spreadsheet is designed to handle:
- Arithmetic operations within formulas.
- Dependencies between cells.
- Error detection (e.g., invalid formulas, circular references).
- File operations such as saving and loading spreadsheets.

This README provides a comprehensive guide to the project's structure, functionality, and usage.

---

## Features

### Supported Cell Types

1. **Text**: Any non-numeric, non-formula input.
   - Example: `Hello`, `ABC123`

2. **Numbers**: Numeric inputs that can be parsed as doubles.
   - Example: `42`, `-3.14`

3. **Formulas**: Expressions that start with `=` and include:
   - Numbers (e.g., `=42`, `=3.14`)
   - Arithmetic operators (`+`, `-`, `*`, `/`)
   - Cell references (e.g., `=A1+B2`)
   - Parentheses for order of operations (e.g., `=(1+2)*(3-4)`)

### Validation and Error Handling
- **Valid formulas**:
  - `=1`, `=1+2`, `=(3-1)*4`
  - `=A1+B2`, `=B1/2`

- **Invalid formulas**:
  - `a`, `1++2`, `=()`
  - Circular references (e.g., `A1 -> A2 -> A1`)

- **Errors handled**:
  - `ERR_FORM`: Invalid formula syntax.
  - `ERR_CYCLE`: Circular references between cells.

### File Operations

1. **Saving the spreadsheet**:
   - Saves the spreadsheet to a `.csv` file.
   - Escapes special characters like commas and newlines.

2. **Loading a spreadsheet**:
   - Reads data from a `.csv` file.
   - Restores cell values and formulas.

### Dependency Management
- Tracks cell dependencies for formulas.
- Ensures cells are evaluated in the correct order based on dependency depth.
- Handles complex dependency graphs and updates.

---

## Class Design

### Ex2Sheet (Spreadsheet)
The main class representing the spreadsheet.

#### Attributes
- `SCell[][] spreadsheetData`: A 2D array storing the cells.

#### Key Methods
- **Basic Operations**:
  - `set(int row, int col, String content)`: Sets the content of a cell.
  - `value(int row, int col)`: Returns the evaluated value of a cell.
- **File Operations**:
  - `save(String filename)`: Saves the spreadsheet to a file.
  - `load(String filename)`: Loads the spreadsheet from a file.
- **Dependency Analysis**:
  - `depth()`: Computes dependency depths for all cells.

### SCell (Spreadsheet Cell)
Represents an individual cell in the spreadsheet.

#### Attributes
- `String cellData`: Raw content of the cell.
- `String calculatedValue`: Processed value after evaluation.
- `int dataType`: Type of the cell (text, number, or formula).

#### Key Methods
- `isNumber()`, `isText()`, `isForm()`: Determine the type of the cell.
- `computeForm(String input)`: Evaluates a formula and returns the result.
- `setProcessedValue(String value)`: Sets the processed value.
- `getProcessedValue()`: Gets the processed value.

### CellEntry (Coordinates Parsing)
Utility class for parsing cell references (e.g., `A1`, `B12`).

#### Key Methods
- `parseEntry(String cellRef)`: Converts a cell reference string to coordinates.
- `xCell(String c)`, `yCell(String c)`: Extracts the column and row indices from a reference.

---

## Testing

### JUnit Test Suite

The project includes a comprehensive JUnit test suite to validate all functionalities and edge cases. Key test cases include:

1. **Basic Operations**
   - Text cell: `set(0, 0, "Hello")` -> `value(0, 0)` returns `"Hello"`.
   - Numeric cell: `set(0, 1, "42")` -> `value(0, 1)` returns `"42.0"`.

2. **Formula Evaluation**
   - Simple formula: `set(0, 2, "=1+1")` -> `value(0, 2)` returns `"2.0"`.
   - Nested formula: `set(0, 3, "=(2+3)*4")` -> `value(0, 3)` returns `"20.0"`.

3. **Error Handling**
   - Invalid formula: `set(1, 0, "=1++2")` -> `value(1, 0)` returns `"ERR_FORM!"`.
   - Circular reference: `set(2, 0, "=A1")`, `set(1, 0, "=A2")` -> `value(1, 0)` returns `"ERR_CYCLE!"`.

4. **File Operations**
   - Save and load: Save a spreadsheet, modify it, reload, and verify consistency.

---

### Example Usage

#### Basic Operations
```java
Ex2Sheet sheet = new Ex2Sheet();
sheet.set(0, 0, "=1+2");
System.out.println(sheet.value(0, 0)); // Output: 3.0
```

#### File Operations
```java
sheet.save("spreadsheet.csv");
Ex2Sheet loadedSheet = new Ex2Sheet();
loadedSheet.load("spreadsheet.csv");
```

---

## Advanced Details

### Formula Parsing
The formula parser supports:
- Operator precedence (`*` and `/` before `+` and `-`).
- Nested parentheses.
- Cell references within formulas (e.g., `=A1+B2`).

#### Parsing Algorithm
1. **Validation:**
   - Check for invalid characters and mismatched parentheses.
2. **Dependency Analysis:**
   - Identify cell references and track dependencies.
3. **Evaluation:**
   - Evaluate in the correct order of operations.

### Dependency Resolution
- **Depth Analysis:**
  - Calculates the depth of each cell based on its dependencies.
- **Circular Reference Detection:**
  - Detects and flags circular references during evaluation.

---
