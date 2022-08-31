/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.currency;

import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Argenis Rodríguez
 */
public class CurrencyPanel extends JPanelTable {
    
    private TableDefinition tlCurrency;
    private CurrencyView jEditor;
    
    @Override
    protected void init() {
        DataLogicCurrencies dlCurrency = (DataLogicCurrencies) app.getBean(DataLogicCurrencies.class.getCanonicalName());
        tlCurrency = dlCurrency.getTableCurrency();
        jEditor = new CurrencyView(app, dirty);
    }
    
    @Override
    public EditorRecord getEditor() {
        return jEditor;
    }
    
    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tlCurrency);
    }
    
    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tlCurrency
                , new int[] {0, 1, 2});
    }
    
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Currency");
    }
    
    @Override
    public Vectorer getVectorer() {
        return tlCurrency.getVectorerBasic(new int [] {2});
    }
    
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tlCurrency.getComparatorCreator(new int [] {0});
    }
    
    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tlCurrency.getRenderStringBasic(new int[] {2, 1}));
    }
}
