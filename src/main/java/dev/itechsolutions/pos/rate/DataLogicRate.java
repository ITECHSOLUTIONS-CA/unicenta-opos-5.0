/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.rate;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.ticket.TaxInfo;
import dev.itechsolutions.pos.ticket.ITSProductInfo;
import dev.itechsolutions.util.NumberUtil;
import dev.itechsolutions.util.TimestampUtil;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Argenis Rodríguez
 */
public class DataLogicRate extends BeanFactoryDataSingle {
    
    protected Session s;
    private TableDefinition trate;
    
    /**
     * static data definition
     */
    private static final Datas[] ratedatas = new Datas[] {
        Datas.STRING,
        Datas.DOUBLE,
        Datas.TIMESTAMP,
        Datas.STRING
    };
    
    @Override
    public void init(Session s) {
        this.s = s;
        trate = new TableDefinition(s
            , "currencyRate"
            , new String[]{
                "ID",
                "RATE",
                "DATEFROM",
                "CURRENCYID"
            }, new String[] {
                "ID",
                AppLocal.getIntString("label.rate"),
                AppLocal.getIntString("label.valid.from"),
                AppLocal.getIntString("label.currency")
            }, ratedatas, new Formats[] {
                Formats.STRING,
                Formats.DOUBLE,
                Formats.DATE,
                Formats.STRING
            }, new int[] {0});
    }
    
    public SentenceList getCurrencyRateSt() {
        return new PreparedSentence(s, "SELECT"
                    + " cr.ID"
                    + ", cr.RATE"
                    + ", cr.DATEFROM"
                    + ", cr.CURRENCYID"
                    + ", c.ISO_CODE"
                + " FROM currencyRate cr"
                + " INNER JOIN currency c ON c.id = cr.currencyId"
                , null
                , new SerializerReadBasic(new Datas[] {
                    Datas.STRING, Datas.DOUBLE, Datas.TIMESTAMP
                    , Datas.STRING, Datas.STRING
                }));
    }
    
    /**
     * 
     * @author Argenis Rodríguez
     * @return 
     */
    public final SentenceExecTransaction getRateInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            protected int execInTransaction(Object params) throws BasicException {
                return execInsert(params);
            }
        };
    }
    
    /**
     * 
     * @author Argenis Rodríguez
     * @return 
     */
    public final SentenceExecTransaction getRateUpdate() {
        return new SentenceExecTransaction(s) {
            @Override
            protected int execInTransaction(Object params) throws BasicException {
                return execUpdate(params);
            }
        };
    }
    
    /**
     * 
     * @author Argenis Rodríguez
     * @return 
     */
    public final SentenceExecTransaction getRateDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            protected int execInTransaction(Object params) throws BasicException {
                return execDelete(params);
            }
        };
    }
    
    private int execDelete(Object params) throws BasicException {
        
        Object[] values = (Object[]) params;
        int i = new PreparedSentence(s
                , "DELETE FROM currencyRate"
                + " WHERE ID = ?"
                , SerializerWriteString.INSTANCE)
                .exec(values[0]);
        
        afterDeleteRate(values);
        
        return i;
    }
    
    private int execUpdate(Object params) throws BasicException {
        
        Object[] values = (Object[]) params;
        int i = new PreparedSentence(s
                , "UPDATE currencyRate SET RATE = ?"
                        + ", DATEFROM = ?, CURRENCYID = ?"
                        + " WHERE ID = ?"
                , new SerializerWriteBasicExt(new Datas[] {Datas.STRING
                        , Datas.DOUBLE
                        , Datas.TIMESTAMP
                        , Datas.STRING}
                        , new int[] {1, 2, 3, 0}))
                .exec(params);
        
        afterCurrencyRate(values);
        
        return i;
    }
    
    private int execInsert(Object params) throws BasicException {
        
        Object[] values = (Object[]) params;
        int i = new PreparedSentence(s
                , "INSERT INTO currencyRate("
                    + "ID, RATE"
                    + ", DATEFROM, CURRENCYID)"
                + " VALUES (?, ?, ?, ?)"
                , new SerializerWriteBasicExt(ratedatas
                        , new int[] {0, 1, 2, 3}))
                .exec(params);
        
        afterCurrencyRate(values);
        
        return i;
    }
    
    public int updatePriceFromCurrency(Object params) throws BasicException {
        
        Object[] values = (Object[]) params;
        
        CurrencyRate rate = new CurrencyRate(values);
        
        List<ITSProductInfo> products = getByBaseCurrencyId(rate.getCurrencyId());
        
        int updates = 0;
        
        for (ITSProductInfo product: products)
            updates += updateProductPrice(product, rate);
        
        return updates;
    }
    
    private void afterDeleteRate(Object [] values) throws BasicException {
        CurrencyRate rate = new CurrencyRate(values);
        
        CurrencyRate actualRate = getActualRate(rate.getCurrencyId());
        
        if (actualRate == null
                || rate.getDateFrom().after(TimestampUtil.now())
                || rate.getDateFrom().before(actualRate.getDateFrom()))
            return ;
        
        List<ITSProductInfo> products = getByBaseCurrencyId(rate.getCurrencyId());
        
        for (ITSProductInfo product: products)
            updateProductPrice(product, actualRate);
    }
    
    private void afterCurrencyRate(Object[] values) throws BasicException {
        CurrencyRate rate = new CurrencyRate(values);
        
        CurrencyRate actualRate = getActualRate(rate.getCurrencyId());
        
        if (actualRate == null || !rate.getId().equals(actualRate.getId()))
            return ;
        
        List<ITSProductInfo> products = getByBaseCurrencyId(rate.getCurrencyId());
        
        for (ITSProductInfo product: products)
            updateProductPrice(product, actualRate);
    }
    
    private int updateProductPrice(ITSProductInfo product, CurrencyRate rate) throws BasicException {
        
        double priceBuy = NumberUtil.round(product.getBasePriceBuy() * rate.getRate(), 2);
        double priceSellTax = NumberUtil.round(product.getBasePriceSell() * rate.getRate(), 2);
        
        TaxInfo tax = getTaxByCategory(product.getTaxCategoryID());
        double taxRate = tax != null ? tax.getRate() : 0.0;
        
        double priceSell = priceSellTax / (1 + taxRate);
        
        product.setPriceBuy(priceBuy);
        product.setPriceSell(priceSell);
        
        return updateProduct(product);
    }
    
    private int updateProduct(ITSProductInfo product) throws BasicException {
        
       return new PreparedSentence(s, "UPDATE products SET PRICEBUY = ?"
                + ", PRICESELL = ?"
                + " WHERE ID = ?"
                , new SerializerWriteBasic(Datas.DOUBLE, Datas.DOUBLE, Datas.STRING))
                .exec(product.getPriceBuy(), product.getPriceSell(), product.getID());
    }
    
    private TaxInfo getTaxByCategory(String taxCategoryId) throws BasicException {
        return (TaxInfo) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "NAME, "
                + "CATEGORY, "
                + "CUSTCATEGORY, "
                + "PARENTID, "
                + "RATE, "
                + "RATECASCADE, "
                + "RATEORDER "
                + "FROM taxes "
                + "WHERE PARENTID IS NULL "
                + "AND CATEGORY = ? "
                + "ORDER BY NAME "
                , SerializerWriteString.INSTANCE
                , (DataRead dr) -> new TaxInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getString(4),
                dr.getString(5),
                dr.getDouble(6),
                dr.getBoolean(7),
                dr.getInt(8)))
                .find(taxCategoryId);
    }
    
    public List<ITSProductInfo> getByBaseCurrencyId(String baseCurrencyId) throws BasicException {
        return (List<ITSProductInfo>) new PreparedSentence(s
                , "SELECT "
                + "ID, "
                + "REFERENCE, "
                + "CODE, "
                + "CODETYPE, "
                + "NAME, "
                + "PRICEBUY, "
                + "PRICESELL, "
                + "CATEGORY, "
                + "TAXCAT, "
                + "ATTRIBUTESET_ID, "
                + "STOCKCOST, "
                + "STOCKVOLUME, "
                + "IMAGE, "
                + "ISCOM, "
                + "ISSCALE, "
                + "ISCONSTANT, "
                + "PRINTKB, "
                + "SENDSTATUS, "
                + "ISSERVICE, "
                + "ATTRIBUTES, "
                + "DISPLAY, "
                + "ISVPRICE, "
                + "ISVERPATRIB, "
                + "TEXTTIP, "
                + "WARRANTY, "
                + "STOCKUNITS, "
                + "PRINTTO, "
                + "SUPPLIER, "
                + "UOM, "
                + "MEMODATE, "
                + "BASEPRICEBUY, "
                + "BASEPRICESELL, "
                + "BASECURRENCY "
                + "FROM products WHERE BASECURRENCY = ?"
                , SerializerWriteString.INSTANCE
                , ITSProductInfo.getSerializerRead()).list(baseCurrencyId);
    }
    
    /**
     * 
     * @author Argenis Rodríguez
     * @param currencyId
     * @param date
     * @return 
     * @throws com.openbravo.basic.BasicException 
     */
    public CurrencyRate getRate(String currencyId, Date date) throws BasicException {
        return (CurrencyRate) new PreparedSentence(s, "SELECT id, rate, datefrom, currencyId"
                + " FROM currencyRate WHERE currencyId = ? AND datefrom <= ?"
                + " ORDER BY datefrom DESC", (dp, param) -> {
                    Object[] params = (Object[]) param;
                    Datas.STRING.setValue(dp, 1, params[0]);
                    Datas.TIMESTAMP.setValue(dp, 2, params[1]);
                }, (dr) -> new CurrencyRate(dr.getString(1),dr.getDouble(2)
                        , dr.getTimestamp(3), dr.getString(4)))
                .find(currencyId, date);
    }
    
    public CurrencyRate getById(String id) throws BasicException {
        return (CurrencyRate) new PreparedSentence(s, "SELECT id, rate, datefrom, currencyId"
                + " FROM currencyRate WHERE id = ?"
                , SerializerWriteString.INSTANCE
                , (dr) -> new CurrencyRate(dr.getString(1),dr.getDouble(2)
                        , dr.getTimestamp(3), dr.getString(4)))
                .find(id);
    }
    
    /**
     * 
     * @author Argenis Rodríguez
     * @param currencyId
     * @return
     * @throws BasicException 
     */
    public CurrencyRate getActualRate(String currencyId) throws BasicException {
        return getRate(currencyId, TimestampUtil.now());
    }
    
    public final TableDefinition getTableRate() {
        return trate;
    }
}
