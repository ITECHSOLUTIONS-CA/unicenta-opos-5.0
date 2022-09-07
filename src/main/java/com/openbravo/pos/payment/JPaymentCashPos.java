//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.payment;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.util.ThumbNailBuilder;
import dev.itechsolutions.pos.currency.Currency;
import dev.itechsolutions.pos.currency.DataLogicCurrencies;
import dev.itechsolutions.pos.rate.CurrencyRate;
import dev.itechsolutions.pos.rate.DataLogicRate;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 *
 * @author adrianromero
 */
public class JPaymentCashPos extends javax.swing.JPanel implements JPaymentInterface {
    
    private final JPaymentNotifier m_notifier;

    private double m_dPaid;
    private double m_dTotal;  
    private double m_dTotalBase;
    private double m_rate = 0;
    private final Boolean priceWith00;
    private SentenceList m_sentCurrencies;
    private ComboBoxValModel m_currModel;
    private Date m_date;
    private DataLogicRate dlRate;
    
    public JPaymentCashPos(JPaymentNotifier notifier, DataLogicSystem dlSystem
            , DataLogicCurrencies dlCurrencies, DataLogicRate dlRate, Date date) {
        this(notifier, dlSystem);
        m_sentCurrencies = dlCurrencies.getAllPS();
        m_currModel = new ComboBoxValModel();
        m_date = date;
        this.dlRate = dlRate;
    }
    
    /** Creates new form JPaymentCash
     * @param notifier
     * @param dlSystem */
    private JPaymentCashPos(JPaymentNotifier notifier, DataLogicSystem dlSystem) {
        
        m_notifier = notifier;
        
        initComponents();  
        
        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);
        
// added JDL 11.05.13        
        AppConfig m_config =  new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));        
        m_config.load();        
        priceWith00 =("true".equals(m_config.getProperty("till.pricewith00")));
        if (priceWith00) {
            // use '00' instead of '.'
            m_jKeys.dotIs00(true);
        }
//        m_config=null;
       
        String code = dlSystem.getResourceAsXML("payment.cash");
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                script.put("payment", new ScriptPaymentCash(dlSystem));    
                script.eval(code);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                msg.show(this);
            }
        }
        
    }
    
    /**
     *
     * @param customerext
     * @param dTotal
     * @param transID
     */
    @Override
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {
        
        m_dTotalBase = dTotal;
        m_dTotal = dTotal;
        
        m_jTendered.reset();
        m_jTendered.activate();
        
        try {
            List<Currency> currencies = m_sentCurrencies.list();
            Currency baseCurrency = dlRate.getBaseCurrency(currencies, m_date);
            m_currModel = new ComboBoxValModel(currencies);
            
            if (baseCurrency != null)
            {
                m_currModel.setSelectedKey(baseCurrency.getId());
                m_rate = 1;
            } else
                m_rate = -1;
            
            jCurrencyCombo.setModel(m_currModel);
        } catch (BasicException ex) {
            Logger.getLogger(JPaymentCashPos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        printState();        
    }

    /**
     *
     * @return
     */
    @Override
    public PaymentInfo executePayment() {
        PaymentInfoCash pInfo;
        if (m_dPaid - m_dTotal >= 0.0) {
            // pago completo
            pInfo = new PaymentInfoCash(m_dTotal * m_rate, m_dPaid * m_rate);
        } else {
            // pago parcial
            pInfo = new PaymentInfoCash(m_dPaid * m_rate, m_dPaid * m_rate);
        }        
        pInfo.setRate(m_rate);
        pInfo.setCurrencyId((String) m_currModel.getSelectedKey());
        return pInfo;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    private void printState() {

        Double value = m_jTendered.getDoubleValue();
        if (value == null || value == 0.0) {
            m_dPaid = m_dTotal;
        } else {            
            m_dPaid = value;

        }   

        int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);
        
        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(m_dPaid));
        m_jChangeEuros.setText(iCompare > 0 
                ? Formats.CURRENCY.formatValue(m_dPaid - m_dTotal)
                : null); 
        
        m_notifier.setStatus(m_dPaid > 0.0, iCompare >= 0);
    }
    
    private class RecalculateState implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }

    /**
     *
     */
    public class ScriptPaymentCash {
        
        private final DataLogicSystem dlSystem;
        private final ThumbNailBuilder tnbbutton;
        private final AppConfig m_config;
        
        /**
         *
         * @param dlSystem
         */
        public ScriptPaymentCash(DataLogicSystem dlSystem) {
//added 19.04.13 JDL        
            AppConfig m_config =  new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));        
            m_config.load();
            this.m_config = m_config;
        
            this.dlSystem = dlSystem;
            tnbbutton = new ThumbNailBuilder(64, 50, "com/openbravo/images/cash.png");
        }
        
        /**
         *
         * @param image
         * @param amount
         */
        public void addButton(String image, double amount) {
            JButton btn = new JButton();
//added 19.04.13 JDL removal of text on payment buttons if required.   
            try {
            if ((m_config.getProperty("payments.textoverlay")).equals("false")){
                btn.setIcon(new ImageIcon(tnbbutton.getThumbNailText(dlSystem.getResourceAsImage(image),"")));  
            } else {
                btn.setIcon(new ImageIcon(tnbbutton.getThumbNailText(dlSystem.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount)))); 
            }
            } catch (Exception e){
                    btn.setIcon(new ImageIcon(tnbbutton.getThumbNailText(dlSystem.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount))));        
            }   
            
            btn.setFocusPainted(false);
            btn.setFocusable(false);
            btn.setRequestFocusEnabled(false);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.addActionListener(new AddAmount(amount));
            jPanel6.add(btn);  
        }
    }
    
    
    
    private class AddAmount implements ActionListener {        
        private final double amount;
        public AddAmount(double amount) {
            this.amount = amount;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Double tendered = m_jTendered.getDoubleValue();
 
            if (tendered == null) {
                 m_jTendered.setDoubleValue(amount);
            } else {
              m_jTendered.setDoubleValue(tendered + amount);    
              
            }

            printState();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jChangeEuros = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        m_jMoneyEuros = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();
        jLabel1 = new javax.swing.JLabel();
        jCurrencyCombo = new javax.swing.JComboBox<>();

        setPreferredSize(new java.awt.Dimension(700, 400));
        setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(450, 70));
        jPanel4.setLayout(null);

        m_jChangeEuros.setBackground(new java.awt.Color(255, 255, 255));
        m_jChangeEuros.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jChangeEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jChangeEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jChangeEuros.setOpaque(true);
        m_jChangeEuros.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jChangeEuros);
        m_jChangeEuros.setBounds(120, 36, 160, 30);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.ChangeCash")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(jLabel6);
        jLabel6.setBounds(10, 36, 100, 30);

        jLabel8.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.InputCash")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(jLabel8);
        jLabel8.setBounds(10, 4, 100, 30);

        m_jMoneyEuros.setBackground(new java.awt.Color(255, 255, 255));
        m_jMoneyEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jMoneyEuros);
        m_jMoneyEuros.setBounds(120, 4, 160, 30);

        jPanel5.add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel6.setPreferredSize(new java.awt.Dimension(450, 10));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

        add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jTendered.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.currency")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel2.add(jLabel1, java.awt.BorderLayout.CENTER);

        jCurrencyCombo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jCurrencyCombo.setToolTipText("");
        jCurrencyCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCurrencyComboActionPerformed(evt);
            }
        });
        jPanel2.add(jCurrencyCombo, java.awt.BorderLayout.PAGE_END);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jCurrencyComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCurrencyComboActionPerformed
        String currencyId = (String) m_currModel.getSelectedKey();
        
        if (currencyId == null
                || currencyId.trim().length() == 0)
            return ;
        
        try {
            CurrencyRate currRate = dlRate.getRate(currencyId, m_date);
            
            if (currRate == null)
            {
                String msg = AppLocal.getIntString("message.no.currency.conversion");
                Currency currency = (Currency) m_currModel.getSelectedItem();
                msg = MessageFormat.format(msg, currency.getIsoCode());
                //m_currModel.setSelectedItem(null);
                m_rate = -1;
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
                return ;
            }
            m_rate = currRate.getRate();
            m_dTotal = m_rate != 0 ? m_dTotalBase / m_rate : 0;
            m_jTendered.setDoubleValue(null);
            printState();
        } catch (BasicException ex) {
            Logger.getLogger(JPaymentCashPos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCurrencyComboActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jKeysActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jCurrencyCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel m_jChangeEuros;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    // End of variables declaration//GEN-END:variables
    
}
