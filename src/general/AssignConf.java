/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package general;
import java.sql.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import database.DBConn;
import java.time.*;

/**
 *
 * @author Fcaty
 */
public class AssignConf extends javax.swing.JDialog {
    private boolean editing = false;
    public boolean success = false;
    private String fullName;
    private LocalDate date;
    private int empID = 0;
    private int attendanceID = 0;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AssignConf.class.getName());

    /**
     * Creates new form AssigntoSched
     */
    public AssignConf(java.awt.Frame parent, boolean modal, boolean mode) {
        super(parent, modal);
        editing = mode;
        initComponents();
        loadConferenceSelection();
    }
    
    public void receiveData(String empID, String name){
        this.empID = Integer.parseInt(empID);
        fullName = name;
    }
    
    public void receiveEditData(String attendanceID, String empID, String name, String fees, String date){
        this.empID = Integer.parseInt(empID);
        this.attendanceID = Integer.parseInt(attendanceID);
        fullName = name;
        attendDate.setDate(LocalDate.parse(date));
        txtPayment.setText(fees);
    }
    
    private void loadConferenceSelection(){
        try(
                Connection con = DBConn.attemptConnection();
                Statement stmtLoad = con.createStatement();
           ){
            
            selectConf.removeAllItems();
            selectConf.addItem("Select");
            ResultSet rs = stmtLoad.executeQuery("SELECT title FROM conference_registration.conference");
            
            while(rs.next()){
                selectConf.addItem(rs.getString("title"));
            }
            
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "An error occured: "+ e.getMessage());
        }
    }
    
    private boolean assignToConf(){
        int confID = 0;
        try(
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmtScrapeID = con.prepareStatement("SELECT confID FROM conference_registration.conference WHERE title = ?");
                PreparedStatement pstmtInput = con.prepareStatement("INSERT INTO conference_registration.attends (empID, confID, conf_title, participant_name, fees_paid, date) "
                        + "VALUES (?, ?, ?, ?, ?, ?)");
           ){
            
            pstmtScrapeID.setString(1, (String) selectConf.getSelectedItem());
            ResultSet rs = pstmtScrapeID.executeQuery();
            while(rs.next()) {
                confID = rs.getInt("confID");
            }
            
            pstmtInput.setInt(1, empID);
            pstmtInput.setInt(2, confID);
            pstmtInput.setString(3, (String) selectConf.getSelectedItem());
            pstmtInput.setString(4, fullName);
            pstmtInput.setDouble(5, Double.parseDouble(txtPayment.getText()));
            pstmtInput.setString(6, attendDate.getDateStringOrEmptyString());
            pstmtInput.executeUpdate();
            
            return true;
            
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "An error has occured: "+ e.getMessage());
            return false;
        }
    }
    
    private boolean reassignToConf(){
        int confID = 0;
        try(
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmtScrapeID = con.prepareStatement("SELECT confID FROM conference_registration.conference WHERE title = ?"); 
                PreparedStatement pstmtInput = con.prepareStatement("UPDATE conference_registration.attends SET empID = ?, confID = ?, conf_title = ?, participant_name = ?, fees_paid = ?, date = ? WHERE attendanceID = ?");
           ){
            
            pstmtScrapeID.setString(1, (String) selectConf.getSelectedItem());
            ResultSet rs = pstmtScrapeID.executeQuery();
            
            while(rs.next()){
                confID = rs.getInt("confID");
            }
            
            pstmtInput.setInt(1, empID);
            pstmtInput.setInt(2, confID);
            pstmtInput.setString(3, (String) selectConf.getSelectedItem());
            pstmtInput.setString(4, fullName);
            pstmtInput.setDouble(5, Double.parseDouble(txtPayment.getText()));
            pstmtInput.setString(6, attendDate.getDateStringOrEmptyString());
            pstmtInput.setInt(7, attendanceID);
            pstmtInput.executeUpdate();
            
            return true;
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "An error has occured: "+ e.getMessage());
            return false;
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        selectConf = new javax.swing.JComboBox<>();
        txtPayment = new javax.swing.JTextField();
        btnAssign = new javax.swing.JButton();
        attendDate = new com.github.lgooddatepicker.components.DatePicker();
        btnReturn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Conferences");

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Date");

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Payment");

        selectConf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectConfActionPerformed(evt);
            }
        });

        btnAssign.setBackground(new java.awt.Color(0, 0, 204));
        btnAssign.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnAssign.setForeground(new java.awt.Color(255, 255, 255));
        btnAssign.setText("Assign");
        btnAssign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAssignActionPerformed(evt);
            }
        });

        btnReturn.setBackground(new java.awt.Color(0, 0, 204));
        btnReturn.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnReturn.setForeground(new java.awt.Color(255, 255, 255));
        btnReturn.setText("← Go Back");
        btnReturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4)
                    .addComponent(selectConf, 0, 227, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel6))
                    .addComponent(txtPayment))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(attendDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(337, 337, 337))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addComponent(btnReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnAssign, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(29, 29, 29)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(141, 141, 141))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attendDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectConf, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAssign, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 204));
        jLabel1.setText("Assign to Conference");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void selectConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectConfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectConfActionPerformed

    private void btnAssignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAssignActionPerformed
        if(editing){
            success = reassignToConf();
        } else {
            success = assignToConf();
        }
          
        dispose();
    }//GEN-LAST:event_btnAssignActionPerformed

    private void btnReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnActionPerformed
        dispose();
    }//GEN-LAST:event_btnReturnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AssignConf dialog = new AssignConf(new javax.swing.JFrame(), true, false);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.github.lgooddatepicker.components.DatePicker attendDate;
    private javax.swing.JButton btnAssign;
    private javax.swing.JButton btnReturn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox<String> selectConf;
    private javax.swing.JTextField txtPayment;
    // End of variables declaration//GEN-END:variables
}
