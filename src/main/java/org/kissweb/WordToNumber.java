package org.kissweb;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Blake McBride
 * Date: 12/5/19
 */
public class WordToNumber {

    public static long parseWordToLong(String input) {
        if (input == null)
            throw new NumberFormatException();
        input = input.trim();
        input = input.replaceAll(",", "");
        input = input.replaceAll("$", "");
        input = input.replaceAll("%", "");
        if (input.isEmpty())
            throw new NumberFormatException();
        if (input.matches("-?\\d+"))
            return Long.parseLong(input);

        long result = 0;
        long finalResult = 0;
        boolean isNegative = false;
        final List<String> allowedStrings = Arrays.asList
                (
                        "negative", "minus", "zero", "oh", "one", "two", "to", "too", "three", "four", "for", "five", "six", "seven",
                        "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen",
                        "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty",
                        "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety",
                        "hundred", "thousand", "million", "billion", "trillion"
                );

        input = input.replaceAll("-", " ");
        input = input.toLowerCase().replaceAll(" and ", " ");
        String[] splittedParts = input.trim().split("\\s+");

        for (String str : splittedParts)
            if (!allowedStrings.contains(str))
                throw new NumberFormatException();
        for (String str : splittedParts) {
            switch (str) {
                case "negative":
                case "minus":
                    isNegative = true;
                    break;
                case "zero":
                case "oh":
                    result += 0;
                    break;
                case "one":
                    result += 1;
                    break;
                case "two":
                case "to":
                case "too":
                    result += 2;
                    break;
                case "three":
                    result += 3;
                    break;
                case "four":
                case "for":
                    result += 4;
                    break;
                case "five":
                    result += 5;
                    break;
                case "six":
                    result += 6;
                    break;
                case "seven":
                    result += 7;
                    break;
                case "eight":
                    result += 8;
                    break;
                case "nine":
                    result += 9;
                    break;
                case "ten":
                    result += 10;
                    break;
                case "eleven":
                    result += 11;
                    break;
                case "twelve":
                    result += 12;
                    break;
                case "thirteen":
                    result += 13;
                    break;
                case "fourteen":
                    result += 14;
                    break;
                case "fifteen":
                    result += 15;
                    break;
                case "sixteen":
                    result += 16;
                    break;
                case "seventeen":
                    result += 17;
                    break;
                case "eighteen":
                    result += 18;
                    break;
                case "nineteen":
                    result += 19;
                    break;
                case "twenty":
                    result += 20;
                    break;
                case "thirty":
                    result += 30;
                    break;
                case "forty":
                    result += 40;
                    break;
                case "fifty":
                    result += 50;
                    break;
                case "sixty":
                    result += 60;
                    break;
                case "seventy":
                    result += 70;
                    break;
                case "eighty":
                    result += 80;
                    break;
                case "ninety":
                    result += 90;
                    break;
                case "hundred":
                    result *= 100;
                    break;
                case "thousand":
                    result *= 1_000;
                    finalResult += result;
                    result = 0;
                    break;
                case "million":
                    result *= 1_000_000;
                    finalResult += result;
                    result = 0;
                    break;
                case "billion":
                    result *= 1_000_000_000;
                    finalResult += result;
                    result = 0;
                    break;
                case "trillion":
                    result *= 1_000_000_000_000L;
                    finalResult += result;
                    result = 0;
                    break;
            }
        }
        finalResult += result;

        return isNegative ? -finalResult : finalResult;
    }

    static void test(String inp) {
        try {
            long val = parseWordToLong(inp);
            System.out.println(inp + " = " + val);
        } catch (Exception e) {
            System.out.println(inp + " = invalid");
        }
    }

    public static void main(String [] argv) {
        test("four");
        test("thirty eight");
        test("38");
        test("negative one hundred thirty eight");
        test("One hundred two thousand and thirty four");
    }
}

