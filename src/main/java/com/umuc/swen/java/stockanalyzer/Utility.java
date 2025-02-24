package com.umuc.swen.java.stockanalyzer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for conversion and generic methods
 */
public class Utility {
    
    /**
     * Convert string value to big decimal
     * @param stringCurrency
     * @return 
     */
    public static BigDecimal convertStringCurrency(String stringCurrency){
        BigDecimal doubleCurrency = BigDecimal.valueOf(0);
        BigDecimal val = BigDecimal.valueOf(0);
        if (Character.isLetter(stringCurrency.charAt(stringCurrency.length()-1))){
            char letter = stringCurrency.charAt(stringCurrency.length()-1);
            BigDecimal thousands = getThousands(letter);
            stringCurrency = stringCurrency.substring(0, stringCurrency.length()-1);
            doubleCurrency  = getDoubleCurrency(stringCurrency);
            Logger.getLogger(Utility.class.getName()).log(Level.INFO, "Value of doubleCurrency is: {0}", doubleCurrency);
            Logger.getLogger(Utility.class.getName()).log(Level.INFO, "Value of thousands is: {0}", thousands);
            val = doubleCurrency.multiply(thousands);
            Logger.getLogger(Utility.class.getName()).log(Level.INFO, "Value of val is: {0}", val);
            
        }else{
            doubleCurrency  = getDoubleCurrency(stringCurrency);
            val = doubleCurrency;
        }
        return val;
    }
    
    /**
     * Get thousands by B/M
     * @param letter
     * @return 
     */
    public static BigDecimal getThousands(char letter){
        switch (letter) {
            case 'M':
                return BigDecimal.valueOf(10).pow(6);
            case 'B':
                return BigDecimal.valueOf(10).pow(9);
            case 'T':
                return BigDecimal.valueOf(10).pow(12);
            default:
                break;
        }
        return BigDecimal.valueOf(1);
    }
    
    /**
     * Convert currency from String to Double
     * @param stringCurrency
     * @return 
     */
    public static BigDecimal getDoubleCurrency(String stringCurrency){
        try {
            return BigDecimal.valueOf(NumberFormat.getNumberInstance(java.util.Locale.US).parse(stringCurrency).doubleValue());
        } catch (ParseException ex) {
            Logger.getLogger(StockReporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.valueOf(1);
    }
    public static String removeTrailingZero(double value){
        return new DecimalFormat("0.#").format(value);
    }
    
    /**
     * Get min/max range
     * @param range
     * @return 
     */
    public static String[] getRangeMinAndMax(String range){
        return range.split("-");
    }
    /**
     * Get numerator and denominator for fraction
     * @param fraction
     * @return 
     */
    public static String[] getNumeratorAndDenominator(String fraction){
        return fraction.trim().equals("-")?new String[]{"0","0"}:fraction.split("/");
    }
    
    /**
     * Compute the string values
     * @param str
     * @return 
     */
    public static String computeStringValues(String str){
        String[] operands = str.split("x");
        double operand1 = convertStringCurrency(operands[0]).doubleValue(); 
        double operand2 = Double.parseDouble(operands[1]); 
        double result = operand1 * operand2;
        return String.valueOf(result);
    }
    
    /**
     * Get the number before parantheses for a given string
     * @param str
     * @return 
     */
    public static String getNumberBeforeParantheses(String str){
        return str.trim().equals("-")?"0":(str.split("\\(")[0]).trim();
    }
    
    /**
     * Format date string
     * @param dateString
     * @return 
     */
    public static String formatDateString(String dateString){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(dateString);
            dateString = format.format(date);
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dateString;
    }
    
    /**
     * check if the data is blank
     * @param str
     * @return 
     */
    public static boolean isBlank(String str){
        return str.equals("-") || str.equals("N/A");
    }
}
