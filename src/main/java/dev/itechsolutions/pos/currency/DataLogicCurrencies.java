/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.currency;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;

/**
 *
 * @author Argenis Rodríguez
 */
public class DataLogicCurrencies extends BeanFactoryDataSingle {
    
    private Session s;
    private TableDefinition tCurrency;
    
    /**
     * static currency data
     */
    private static final Datas[] currencyDatas = new Datas[] {
        Datas.STRING,
        Datas.STRING,
        Datas.STRING
    };
    
    @Override
    public void init(Session s) {
        this.s = s;
        
        tCurrency = new TableDefinition(s
                , "currency"
                , new String [] {
                    "ID",
                    "NAME",
                    "ISO_CODE"
                }, new String[] {
                    "ID",
                    AppLocal.getIntString("label.currency.name"),
                    AppLocal.getIntString("label.currency.isocode")
                }, currencyDatas
                , new Formats[] {
                    Formats.STRING,
                    Formats.STRING,
                    Formats.STRING
                }, new int[] {0});
    }
    
    public Currency getById(String id) throws BasicException {
        return (Currency) new PreparedSentence(s, "SELECT id, name, iso_code"
                + " FROM currency WHERE id = ?"
                , SerializerWriteString.INSTANCE
                , dr -> new Currency(dr.getString(1), dr.getString(2), dr.getString(3)))
                .find(id);
    }
    
    public TableDefinition getTableCurrency() {
        return tCurrency;
    }
    
    public SentenceList getAllPS() {
        return new PreparedSentence(s, "SELECT id, name, iso_code"
                + " FROM currency"
                , null
                , dr -> new Currency(dr.getString(1), dr.getString(2), dr.getString(3)));
    }
    
    public List<Currency> getAll() throws BasicException {
        return getAllPS().list();
    }
}
