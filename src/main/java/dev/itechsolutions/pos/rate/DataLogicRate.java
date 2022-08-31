/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.rate;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import dev.itechsolutions.util.TimestampUtil;
import java.util.Date;

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
