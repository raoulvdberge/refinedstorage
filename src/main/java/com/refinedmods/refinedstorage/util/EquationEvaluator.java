package com.refinedmods.refinedstorage.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Implements evaluation for simple math expressions using the shunting yard
 * algorithm.
 */
public class EquationEvaluator {

    public static double evaluate(String input) {
        final StringTokenizer tokens = new StringTokenizer(input, "+-*/()", true);
        final Deque<String> operators = new ArrayDeque<>();
        final Deque<Double> operands = new ArrayDeque<>();
        String lastToken = null;
        while (tokens.hasMoreTokens()) {
            String rawToken = tokens.nextToken().trim();
            if (rawToken.isEmpty())
                continue;
            if (isUnaryMinus(rawToken, lastToken)) {
                rawToken = "u";
            } else if (isImplicitMultiply(rawToken, lastToken)) {
                lastToken = processToken(input, "*", operators, operands);
            }
            lastToken = processToken(input, rawToken, operators, operands);
        }
        popOperators(input, operators, operands, top -> true);
        return operands.pop();
    }

    private static boolean isUnaryMinus(String rawToken, @Nullable String lastToken) {
        return rawToken.equals("-")
                && (lastToken == null || (isOperator(lastToken) && !lastToken.equals("u")) || lastToken.equals("("));
    }

    private static boolean isImplicitMultiply(String rawToken, @Nullable String lastToken) {
        return rawToken.equals("(")
                && (lastToken != null && !isOperator(lastToken) && !lastToken.equals("(") && !lastToken.equals(")"));
    }

    private static String processToken(String input, String token, Deque<String> operators, Deque<Double> operands) {
        if (isOperator(token)) {
            popOperators(input, operators, operands,
                    top -> (!top.equals("(") && getPrecedence(token) <= getPrecedence(top)));
            operators.push(token);
        } else if (token.equals("(")) {
            operators.push(token);
        } else if (token.equals(")")) {
            popOperators(input, operators, operands, top -> !top.equals("("));
            if (!operators.pop().equals("(")) {
                throw new IllegalArgumentException("Unbalanced right parentheses in expression: " + input);
            }
        } else {
            try {
                operands.push(Double.parseDouble(token));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Could not parse double from:" + token);
            }
        }
        return token;
    }

    private static void popOperators(String input, Deque<String> operators, Deque<Double> operands,
            Predicate<String> predicate) {
        try {
            while (!operators.isEmpty() && predicate.test(operators.peek())) {
                final String op = operators.pop();
                if (op.equals("u")) {
                    operands.push(-operands.pop());
                    continue;
                }
                operands.push(evaluateExpression(op, operands.pop(), operands.pop()));
            }
        } catch (NoSuchElementException exception) {
            throw new IllegalArgumentException("Missing operand in expression: " + input);
        }
    }

    private static double evaluateExpression(String op, double b, double a) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                return a / b;
        }
        throw new IllegalArgumentException("Unexpected arithmetic operation " + op);
    }

    private static boolean isOperator(String token) {
        switch (token) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "u":
                return true;
        }
        return false;
    }

    private static int getPrecedence(String op) {
        switch (op) {
            case "+":
            case "-":
                return 0;
            case "*":
            case "/":
                return 1;
            case "u":
                return 2;
        }
        return -1;
    }
}
