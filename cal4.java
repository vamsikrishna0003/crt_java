import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class cal4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("Enter an expression (or 'exit' to quit): ");
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            if (input.trim().isEmpty()) {
                System.out.println("Error: Expression cannot be empty.");
                continue;
            }
            if (!input.matches("^[0-9+\\-*/(). ]+$")) {
                System.out.println("Invalid characters in expression. Only numbers, operators (+, -, *, /), and parentheses are allowed.");
                continue;
            }
            try {
                double result = eval(input);
                System.out.println("Result: " + result);
            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
        sc.close();
    }

    public static int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    public static double applyOp(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) {
                    throw new UnsupportedOperationException("Cannot divide by zero.");
                }
                return a / b;
        }
        throw new IllegalArgumentException("Invalid operator: " + op);
    }

    public static double eval(String expr) {
        String processedExpr = expr.replaceAll("\\s+", "");
        if (processedExpr.isEmpty()) {
            throw new IllegalArgumentException("Expression is empty after removing whitespace.");
        }
        String[] tokenArray = processedExpr.split("(?<=[-+*/()])|(?=[-+*/()])");
        List<String> tokens = new ArrayList<>(Arrays.asList(tokenArray));
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        boolean expectOperand = true;
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            try {
                double num = Double.parseDouble(token);
                if (!expectOperand) {
                    throw new IllegalArgumentException("Unexpected number: '" + token + "'. An operator may be missing.");
                }
                values.push(num);
                expectOperand = false;
                continue;
            } catch (NumberFormatException e) {
                // Not a number, continue to check for operator/parenthesis
            }
            if (token.length() != 1) {
                throw new IllegalArgumentException("Invalid token: '" + token + "'. Only numbers, single-character operators, or parentheses are allowed.");
            }
            char c = token.charAt(0);
            if (c == '(') {
                if (!expectOperand) {
                    throw new IllegalArgumentException("Unexpected '('. An operator may be missing before '('.");
                }
                ops.push(c);
                expectOperand = true;
            } else if (c == ')') {
                if (expectOperand) {
                    throw new IllegalArgumentException("Invalid expression: Missing operand before ')' or empty/incomplete parentheses.");
                }
                while (!ops.isEmpty() && ops.peek() != '(') {
                    if (values.size() < 2) {
                        throw new IllegalArgumentException("Syntax error: Not enough operands for operator '" + ops.peek() + "' inside parentheses.");
                    }
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                if (ops.isEmpty() || ops.peek() != '(') {
                    throw new IllegalArgumentException("Mismatched parentheses: Extra ')' or missing '('.");
                }
                ops.pop();
                expectOperand = false;
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                if (expectOperand) {
                    if (c == '-' || c == '+') {
                        values.push(0.0);
                    } else {
                        throw new IllegalArgumentException("Invalid use of unary operator: '" + c + "'. Only '+' or '-' can be unary.");
                    }
                }
                while (!ops.isEmpty() && ops.peek() != '(' && precedence(c) <= precedence(ops.peek())) {
                    if (values.size() < 2) {
                        throw new IllegalArgumentException("Syntax error: Not enough operands for operator '" + ops.peek() + "'.");
                    }
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(c);
                expectOperand = true;
            } else {
                throw new IllegalArgumentException("Unknown token or invalid character: '" + token + "'");
            }
        }
        while (!ops.isEmpty()) {
            char op = ops.pop();
            if (op == '(') {
                throw new IllegalArgumentException("Mismatched parentheses: Extra '('.");
            }
            if (values.size() < 2) {
                throw new IllegalArgumentException("Invalid expression: Not enough operands for operator '" + op + "' at the end of expression.");
            }
            values.push(applyOp(op, values.pop(), values.pop()));
        }
        if (values.size() != 1) {
            if (expectOperand && tokens.size() > 0 && !values.isEmpty()) {
                throw new IllegalArgumentException("Invalid expression: Expression ends with an operator needing an operand.");
            }
            throw new IllegalArgumentException("Invalid expression format. Resulting value stack size: " + values.size() + ", expected 1.");
        }
        if (expectOperand && values.isEmpty() && tokens.size() > 0 && !Character.isDigit(tokens.get(tokens.size() - 1).charAt(0))) {
            throw new IllegalArgumentException("Invalid expression: operand expected at the end.");
        }
        return values.pop();
    }
}
