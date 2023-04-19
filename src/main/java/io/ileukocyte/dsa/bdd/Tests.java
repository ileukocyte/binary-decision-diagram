package io.ileukocyte.dsa.bdd;

import java.util.stream.Collectors;

public class Tests {
    public static boolean parseFunction(String function, String values, String order) {
        if (function.isEmpty() || values.isEmpty() || order.isEmpty()) {
            throw new IllegalArgumentException("No argument must be empty!");
        }

        if (!function.matches("[!A-Z+\\s]+")) {
            throw new IllegalArgumentException("The provided format is not correct!");
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
}
