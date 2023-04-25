package io.ileukocyte.dsa.bdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public class Tests {
    public static final int MIN_CLAUSES = 15;
    public static final int MAX_CLAUSES = 55;
    public static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static boolean testBdd(BinaryDecisionDiagram bdd, boolean singleTestOutput) {
        var successful = true;

        var total = (int) Math.pow(2, bdd.getOrder().length());
        var wrongValues = 0;

        for (int i = 0; i < total; i++) {
            var zeros = new char[bdd.getOrder().length()];
            var binary = Integer.toBinaryString(i);

            Arrays.fill(zeros, '0');

            var values = new String(zeros).substring(binary.length()) + binary;
            var parseResult = Tests.parseFunction(bdd.getFunction(), values, bdd.getOrder());

            try {
                var bddResult = bdd.use(values);

                if (bddResult != parseResult) {
                    System.out.printf("%s. BDD: %b, actual: %b\n", values, bddResult, parseResult);

                    wrongValues++;

                    successful = false;
                }
            } catch (IllegalStateException e) {
                System.out.printf("IllegalStateException for %s: (%s)\n", values, parseResult);

                e.printStackTrace();

                return false;
            }
        }

        if (singleTestOutput) {
            System.out.printf("Done! Valid values: %d/%d\n", total - wrongValues, total);
        }

        return successful;
    }

    public static String generateDnfExpression(int variableCount) {
        var random = new Random();

        var clausesCount = random.ints(1, MIN_CLAUSES, MAX_CLAUSES + 1)
                .findFirst()
                .orElseThrow();

        var clauses = new ArrayList<String>();

        var letters = new ArrayList<>(CAPITAL_LETTERS.chars().mapToObj(i -> (char) i).toList());

        Collections.shuffle(letters);

        var variables = letters.stream().limit(variableCount).toList();

        for (int i = 0; i < clausesCount; i++) {
            var variablesToTake = random.ints(1, 1, variableCount + 1)
                    .findFirst()
                    .orElseThrow();

            var temp = new ArrayList<>(variables);

            Collections.shuffle(temp);

            // 15% is the probability of whether a variable within a clause will be negated
            clauses.add(letters.stream()
                    .limit(variablesToTake)
                    .map(v -> (Math.random() <= 0.15 && !clauses.contains(String.valueOf(v)) ? "!" : "") + v)
                    .collect(Collectors.joining("")));
        }

        var result = String.join(" + ", clauses.stream().distinct().toList());
        var resultVariableCount = result.chars()
                .mapToObj(i -> (char) i)
                .distinct()
                .filter(c -> c >= 'A' && c <= 'Z')
                .count();

        // in case the result contains fewer variables than expected
        return resultVariableCount < variableCount ? generateDnfExpression(variableCount) : result;
    }

    // used for getting a boolean value directly from a function input
    public static boolean parseFunction(String function, String values, String order) {
        if (function.isEmpty() || values.isEmpty() || order.isEmpty()) {
            throw new IllegalArgumentException("No argument must be empty!");
        }

        if (function.matches("[01]+")) {
            return BinaryDecisionDiagram.Node.parseDigits(function).equals("1");
        }

        if (!function.matches("[!A-Z+\\s]+")) {
            throw new IllegalArgumentException("The provided format is not correct! DNF (e.g., ABC + A!B!C) should be used instead!");
        }

        var functionVariables = function.chars()
                .mapToObj(i -> (char) i)
                .filter(c -> c >= 'A' && c <= 'Z')
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(""));
        var orderVariables = order.chars()
                .mapToObj(i -> (char) i)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(""));

        if (!functionVariables.equals(orderVariables)) {
            throw new IllegalArgumentException("The function and order provided do not correspond to each other!");
        }

        if (!values.matches("[01]+") || values.length() != functionVariables.length()) {
            throw new IllegalArgumentException("The input does not match the required format!");
        }

        function = BinaryDecisionDiagram.Node.format(function);

        var variablesMap = order.chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toMap(c -> c, c -> values.charAt(order.indexOf(c))));

        for (var entry : variablesMap.entrySet()) {
            function = function
                    .replace(entry.getKey(), entry.getValue())
                    .replace(Character.toLowerCase(entry.getKey()), entry.getValue() == '1' ? '0' : '1');
        }

        function = BinaryDecisionDiagram.Node.parseDigits(function);

        return function.equals("1");
    }

    public record TestEntry(double reduction, long creationTime, long memoryUsage) {}
}