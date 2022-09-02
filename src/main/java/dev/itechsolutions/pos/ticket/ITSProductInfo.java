/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.ticket;

import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.pos.ticket.ProductInfoExt;

/**
 *
 * @author Argenis Rodríguez
 */
public class ITSProductInfo extends ProductInfoExt {
    
    /**
     *
     * @return
     */
    public static SerializerRead getSerializerRead() {
        return dr -> {
            ITSProductInfo product = new ITSProductInfo();
            product.m_ID = dr.getString(1);
            product.m_sRef = dr.getString(2);
            product.m_sCode = dr.getString(3);
            product.m_sCodetype = dr.getString(4);
            product.m_sName = dr.getString(5);
            product.m_dPriceBuy = dr.getDouble(6);
            product.m_dPriceSell = dr.getDouble(7);
            product.categoryid = dr.getString(8);
            product.taxcategoryid = dr.getString(9);
            product.attributesetid = dr.getString(10);
            product.m_stockCost = dr.getDouble(11);
            product.m_stockVolume = dr.getDouble(12);
            product.m_Image = ImageUtils.readImage(dr.getBytes(13));
            product.m_bCom = dr.getBoolean(14);
            product.m_bScale = dr.getBoolean(15);
            product.m_bConstant = dr.getBoolean(16);
            product.m_bPrintKB = dr.getBoolean(17);
            product.m_bSendStatus = dr.getBoolean(18);
            product.setService(dr.getBoolean(19));
            product.attributes = ImageUtils.readProperties(dr.getBytes(20));
            product.m_sDisplay = dr.getString(21);
            product.m_bVprice = dr.getBoolean(22);
            product.m_bVerpatrib = dr.getBoolean(23);
            product.m_sTextTip = dr.getString(24);
            product.m_bWarranty = dr.getBoolean(25);
            product.m_dStockUnits = dr.getDouble(26);
            product.m_sPrinter = dr.getString(27);
            product.supplierid = dr.getString(28);
            product.setUomID(dr.getString(29));
            product.memodate = dr.getString(30);
            product.basePriceBuy = dr.getDouble(31);
            product.basePriceSell = dr.getDouble(32);
            product.baseCurrencyId = dr.getString(33);
            
            return product;
        };
    }
    
    public void setBaseCurrencyId(String baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }
    
    public String getBaseCurrencyId() {
        return baseCurrencyId;
    }
    
    public void setBasePriceBuy(double basePriceBuy) {
        this.basePriceBuy = basePriceBuy;
    }
    
    public double getBasePriceBuy() {
        return basePriceBuy;
    }
    
    public void setBasePriceSell(double basePriceSell) {
        this.basePriceSell = basePriceSell;
    }
    
    public double getBasePriceSell() {
        return basePriceSell;
    }
    
    protected String baseCurrencyId;
    protected double basePriceBuy, basePriceSell;
}
