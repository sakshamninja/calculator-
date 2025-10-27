/*
 A simple fully-working calculator implemented in Java Swing.
 Enhancements:
  - Basic operations: +, -, ×, ÷
  - Chaining operations and immediate-execute behavior (like a physical calculator)
  - Decimal point support
  - Equals, Clear Entry (CE), All Clear (AC)
  - Backspace
  - Toggle sign (+/-), Percent, Square root
  - Memory: MC, MR, M+, M-
  - ANS button: automatically stores the last computed result so the user can recall it to perform new calculations
  - Improved behavior so pressing an operator after "=" uses the last result automatically
  - Error handling (e.g., division by zero)
  - Uses BigDecimal for accurate decimal arithmetic where appropriate

 To compile:
   javac Calculator.java

 To run:
   java Calculator
*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

public class Calculator extends JFrame {
    private final JTextField display = new JTextField("0");
    private BigDecimal accumulator = BigDecimal.ZERO; // stored value for operations
    private String pendingOp = null; // "+", "-", "*", "/"
    private boolean startNewNumber = true; // whether next digit starts a new number
    private boolean justCalculated = false; // indicates last action was "="
    private BigDecimal memory = BigDecimal.ZERO; // memory register (MC/MR/M+/M-)
    private BigDecimal lastAnswer = BigDecimal.ZERO; // ANS register (automatically set to last computed result)
    private final MathContext mc = new MathContext(20); // precision for BigDecimal operations

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));

        setupDisplay();
        setupButtons();

        setResizable(false);
        setVisible(true);
    }

    private void setupDisplay() {
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font("SansSerif", Font.PLAIN, 32));
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display, BorderLayout.NORTH);
    }

    private void setupButtons() {
        String[][] labels = {
                {"MC", "MR", "M+", "M-"},
                {"√", "%", "←", "AC"},
                {"7", "8", "9", "÷"},
                {"4", "5", "6", "×"},
                {"1", "2", "3", "-"},
                {"+/-", "0", ".", "+"},
                {"CE", "=", "ANS", ""} // added ANS button in the last row
        };

        JPanel panel = new JPanel(new GridLayout(labels.length, labels[0].length, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (int r = 0; r < labels.length; r++) {
            for (int c = 0; c < labels[r].length; c++) {
                String lab = labels[r][c];
                if (lab == null || lab.isEmpty()) {
                    panel.add(new JLabel()); // placeholder
                    continue;
                }
                JButton btn = new JButton(lab);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 20));
                btn.addActionListener(e -> onButtonPress(lab));
                panel.add(btn);
            }
        }

        add(panel, BorderLayout.CENTER);
    }

    private void onButtonPress(String label) {
        try {
            if (label.matches("[0-9]")) {
                typeDigit(label);
            } else if (label.equals(".")) {
                typeDot();
            } else if (label.equals("+") || label.equals("-") || label.equals("×") || label.equals("÷")) {
                applyOperator(label);
            } else if (label.equals("=")) {
                calculateResult();
            } else if (label.equals("AC")) {
                allClear();
            } else if (label.equals("CE")) {
                clearEntry();
            } else if (label.equals("←")) {
                backspace();
            } else if (label.equals("+/-")) {
                toggleSign();
            } else if (label.equals("%")) {
                percent();
            } else if (label.equals("√")) {
                sqrt();
            } else if (label.equals("MC")) {
                memoryClear();
            } else if (label.equals("MR")) {
                memoryRecall();
            } else if (label.equals("M+")) {
                memoryAdd();
            } else if (label.equals("M-")) {
                memorySubtract();
            } else if (label.equals("ANS")) {
                recallAns();
            }
        } catch (Exception ex) {
            // any unexpected exception: display error and reset state
            display.setText("Error");
            startNewNumber = true;
            pendingOp = null;
            accumulator = BigDecimal.ZERO;
            justCalculated = true;
        }
    }

    private void typeDigit(String d) {
        if (startNewNumber || display.getText().equals("0") || justCalculated) {
            display.setText(d);
            startNewNumber = false;
            justCalculated = false;
        } else {
            display.setText(display.getText() + d);
        }
    }

    private void typeDot() {
        if (startNewNumber || justCalculated) {
            display.setText("0.");
            startNewNumber = false;
            justCalculated = false;
            return;
        }
        if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    private BigDecimal currentValue() {
        try {
            return new BigDecimal(display.getText());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private void applyOperator(String opLabel) {
        // Map × and ÷ to * and /
        String op = opLabel;
        if (opLabel.equals("×")) op = "*";
        if (opLabel.equals("÷")) op = "/";

        if (pendingOp != null && !startNewNumber) {
            // compute previous op first to mimic physical calculator chaining
            computePendingOperation();
        } else {
            // Always store the current displayed value as the accumulator when starting a new operation.
            // This ensures the result of "=" can be used immediately for the next operation.
            accumulator = currentValue();
        }
        pendingOp = op;
        startNewNumber = true;
        justCalculated = false;
    }

    private void calculateResult() {
        if (pendingOp != null) {
            computePendingOperation();
            pendingOp = null;
            justCalculated = true;
            startNewNumber = true;
            // lastAnswer is already updated inside computePendingOperation
        } else {
            // No pending op; treat pressing "=" as storing the current displayed value as lastAnswer
            lastAnswer = currentValue();
            justCalculated = true;
            startNewNumber = true;
        }
    }

    private void computePendingOperation() {
        BigDecimal rhs = currentValue();
        BigDecimal result = accumulator;
        try {
            switch (pendingOp) {
                case "+":
                    result = accumulator.add(rhs, mc);
                    break;
                case "-":
                    result = accumulator.subtract(rhs, mc);
                    break;
                case "*":
                    result = accumulator.multiply(rhs, mc);
                    break;
                case "/":
                    if (rhs.compareTo(BigDecimal.ZERO) == 0) {
                        display.setText("Error");
                        accumulator = BigDecimal.ZERO;
                        pendingOp = null;
                        startNewNumber = true;
                        justCalculated = true;
                        return;
                    } else {
                        result = accumulator.divide(rhs, mc);
                    }
                    break;
            }
        } catch (ArithmeticException ae) {
            // In case of non-terminating decimal expansion or other arithmetic issues, fall back to double
            result = new BigDecimal(Double.toString(accumulator.doubleValue()), mc);
            switch (pendingOp) {
                case "+":
                    result = result.add(new BigDecimal(Double.toString(rhs.doubleValue())), mc);
                    break;
                case "-":
                    result = result.subtract(new BigDecimal(Double.toString(rhs.doubleValue())), mc);
                    break;
                case "*":
                    result = result.multiply(new BigDecimal(Double.toString(rhs.doubleValue())), mc);
                    break;
                case "/":
                    if (rhs.compareTo(BigDecimal.ZERO) == 0) {
                        display.setText("Error");
                        accumulator = BigDecimal.ZERO;
                        pendingOp = null;
                        startNewNumber = true;
                        justCalculated = true;
                        return;
                    } else {
                        result = new BigDecimal(Double.toString(accumulator.doubleValue() / rhs.doubleValue()), mc);
                    }
                    break;
            }
        }

        accumulator = result;
        display.setText(formatResult(result));
        startNewNumber = true;

        // Update ANS register whenever we compute a result
        lastAnswer = accumulator;
    }

    private void allClear() {
        display.setText("0");
        accumulator = BigDecimal.ZERO;
        pendingOp = null;
        startNewNumber = true;
        justCalculated = false;
    }

    private void clearEntry() {
        display.setText("0");
        startNewNumber = true;
    }

    private void backspace() {
        if (justCalculated || startNewNumber) {
            display.setText("0");
            startNewNumber = true;
            justCalculated = false;
            return;
        }
        String s = display.getText();
        if (s.length() <= 1 || (s.length() == 2 && s.startsWith("-"))) {
            display.setText("0");
            startNewNumber = true;
        } else {
            display.setText(s.substring(0, s.length() - 1));
        }
    }

    private void toggleSign() {
        if (display.getText().equals("0")) return;
        if (display.getText().startsWith("-")) {
            display.setText(display.getText().substring(1));
        } else {
            display.setText("-" + display.getText());
        }
    }

    private void percent() {
        BigDecimal val = currentValue();
        BigDecimal res = val.divide(BigDecimal.valueOf(100), mc);
        display.setText(formatResult(res));
        startNewNumber = true;
    }

    private void sqrt() {
        BigDecimal val = currentValue();
        if (val.compareTo(BigDecimal.ZERO) < 0) {
            display.setText("Error");
            startNewNumber = true;
            return;
        }
        double sqrtD = Math.sqrt(val.doubleValue());
        BigDecimal res = new BigDecimal(Double.toString(sqrtD), mc);
        display.setText(formatResult(res));
        startNewNumber = true;
    }

    private void memoryClear() {
        memory = BigDecimal.ZERO;
    }

    private void memoryRecall() {
        display.setText(formatResult(memory));
        startNewNumber = true;
    }

    private void memoryAdd() {
        memory = memory.add(currentValue(), mc);
        startNewNumber = true;
    }

    private void memorySubtract() {
        memory = memory.subtract(currentValue(), mc);
        startNewNumber = true;
    }

    private void recallAns() {
        display.setText(formatResult(lastAnswer));
        startNewNumber = true;
        justCalculated = false;
    }

    private String formatResult(BigDecimal value) {
        // Strip trailing zeros and use plain string to avoid scientific notation for typical calculator sizes
        try {
            BigDecimal stripped = value.stripTrailingZeros();
            String s = stripped.toPlainString();

            // Avoid showing "-0"
            if (s.equals("-0") || s.equals("-0.0")) return "0";

            return s;
        } catch (Exception ex) {
            // Fallback formatting
            DecimalFormat fmt = new DecimalFormat("0.###############");
            return fmt.format(value.doubleValue());
        }
    }

    public static void main(String[] args) {
        // Ensure Swing UI uses platform look & feel where possible
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Calculator::new);
    }
}