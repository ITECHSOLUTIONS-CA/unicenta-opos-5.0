/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author Argenis Rodríguez
 */
public class NumberUtil {
    
    public static double round(double num, int precision) {
        BigDecimal bdNum = BigDecimal.valueOf(num)
                .setScale(precision, RoundingMode.HALF_UP);
        
        return bdNum.doubleValue();
    }
}
