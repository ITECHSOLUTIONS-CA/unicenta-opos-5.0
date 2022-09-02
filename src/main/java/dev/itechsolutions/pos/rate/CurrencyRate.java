/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.rate;

import java.util.Date;

/**
 *
 * @author Argenis Rodríguez
 */
public class CurrencyRate {
    
    public CurrencyRate(String id, double rate, Date dateFrom, String currencyId) {
        this.id = id;
        this.rate = rate;
        this.dateFrom = dateFrom;
        this.currencyId = currencyId;
    }
    
    public CurrencyRate(Object[] rate) {
        id = (String) rate[0];
        this.rate = (Double) rate[1];
        dateFrom = (Date) rate[2];
        currencyId = (String) rate[3];
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setRate(double rate) {
        this.rate = rate;
    }
    
    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }
    
    public String getId() {
        return id;
    }
    
    public double getRate() {
        return rate;
    }
    
    public Date getDateFrom() {
        return dateFrom;
    }
    
    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
    
    public String getCurrencyId() {
        return currencyId;
    }
    
    private String id, currencyId;
    private double rate;
    private Date dateFrom;
}
