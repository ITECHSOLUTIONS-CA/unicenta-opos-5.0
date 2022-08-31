/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.itechsolutions.pos.rate;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.RenderStringBasic;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Argenis Rodríguez
 */
public class RatePanel extends JPanelTable {
    
    private TableDefinition tlRate;
    private RateView jEditor;
    private ListProviderCreator lProvider;
    
    @Override
    protected void init() {
        DataLogicRate dlRate = (DataLogicRate) app.getBean(DataLogicRate.class.getCanonicalName());
        tlRate = dlRate.getTableRate();
        jEditor = new RateView(app, dirty);
        lProvider = new ListProviderCreator(dlRate.getCurrencyRateSt());
    }
    
    @Override
    public void activate() throws BasicException {
        jEditor.activate();
        super.activate();
    }
    
    @Override
    public ListProvider getListProvider() {
        return lProvider;
    }
    
    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tlRate
                , new int [] {0, 1, 2, 3});
    }
    
    @Override
    public Vectorer getVectorer() {
        return tlRate.getVectorerBasic(new int [] {2});
    }
    
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tlRate.getComparatorCreator(new int [] {0});
    }
    
    @Override
    public ListCellRenderer getListCellRenderer() {
        //return new ListCellRendererBasic(tlRate.getRenderStringBasic(new int[] {4, 2, 1}));
        return new ListCellRendererBasic(new RenderStringBasic(new Formats[] {
            Formats.STRING, Formats.DOUBLE, Formats.DATE, Formats.STRING, Formats.STRING
        }, new int[] {4, 2, 1}));
    }
    
    @Override
    public EditorRecord getEditor() {
        return jEditor;
    }
    
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.ConversionRate");
    }
}
