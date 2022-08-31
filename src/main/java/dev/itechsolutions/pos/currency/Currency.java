/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.currency;

import com.openbravo.data.loader.IKeyed;

/**
 *
 * @author Argenis Rodríguez
 */
public class Currency implements IKeyed {
    
    public Currency(String id, String name, String isoCode) {
        this.id = id;
        this.name = name;
        this.isoCode = isoCode;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public String getIsoCode() {
        return isoCode;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public Object getKey() {
        return id;
    }
    
    @Override
    public String toString() {
        return isoCode + " - " + name;
    }
    
    private String id;
    private String isoCode;
    private String name;
}
