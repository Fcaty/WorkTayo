/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

package general;
import database.DBConn;
import java.sql.*;
import java.sql.ResultSetMetaData.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author AMD
 */

public class GlobalReports extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalReports.class.getName());
    
    
    //Loads all conference options
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
    
    //Refreshes participant list for the selected conference
    private void loadConferenceAttendees(){
        //Table declaration
        DefaultTableModel aList = (DefaultTableModel) attendeeList.getModel();
        aList.setRowCount(0);
        
        try(
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmtScrape = con.prepareStatement("SELECT attendanceID, empID, participant_name, fees_paid, date FROM conference_registration.attends WHERE conf_title = ?");
           ){
            
            pstmtScrape.setString(1, (String) selectConf.getSelectedItem());
            ResultSet rs = pstmtScrape.executeQuery();
            
            while(rs.next()){
                Object[] row = {
                  rs.getString("attendanceID"),
                  rs.getString("empID"),
                  rs.getString("participant_name"),
                  Double.parseDouble(rs.getString("fees_paid")),
                  rs.getString("date")
                };
                aList.addRow(row);
            }
            
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "An error has occured: "+ e.getMessage());
        }
    }
    
    //Loads information for lifetime revenue, total unique participants, and total conferences
    private void loadInfo(){
        //Variable declaration
        double totalCash = 0;
        int totalParticipants = 0;
        int totalConferences = 0;
        
        
        try(
                Connection con = DBConn.attemptConnection();
                Statement stmtScrapeAttends = con.createStatement();
                Statement stmtScrapeParticipants = con.createStatement();
                Statement stmtScrapeConferences = con.createStatement();
                
                ResultSet rsAttends = stmtScrapeAttends.executeQuery("SELECT fees_paid FROM conference_registration.attends");
                ResultSet rsParticipants = stmtScrapeParticipants.executeQuery("SELECT * FROM conference_registration.participant");
                ResultSet rsConferences = stmtScrapeConferences.executeQuery("SELECT * FROM conference_registration.conference");
           ){
            
            //Calculates total cash
            while(rsAttends.next()){
                totalCash += rsAttends.getDouble("fees_paid");
            }
            
            //Counts amount of participants
            while(rsParticipants.next()){
                totalParticipants++;
            }
            
            //Counts amount of conferences
            while(rsConferences.next()){
                totalConferences++;
            }
            
            //Updates labels
            txtCashTotal.setText(Double.toString(totalCash));
            txtParticipantTotal.setText(Integer.toString(totalParticipants));
            txtConfTotal.setText(Integer.toString(totalConferences));
            
        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "An error occured: "+ e.getMessage());
        }
    }
    
    //Edits a selected participant to reassign them to a conference
    private void reassignConferenceAttendee(){
        //Variable declaration
        int selectedParticipant = attendeeList.getSelectedRow();
        
        //Error handling for no selected participant
        if(selectedParticipant == -1){
            JOptionPane.showMessageDialog(this, "No participant selected");
            return;
        }
        
        AssignConf edit = new AssignConf(this, true, true);
        edit.receiveEditData(attendeeList.getValueAt(selectedParticipant, 0).toString(), attendeeList.getValueAt(selectedParticipant, 1).toString(), attendeeList.getValueAt(selectedParticipant, 2).toString(), attendeeList.getValueAt(selectedParticipant, 3).toString(), attendeeList.getValueAt(selectedParticipant, 4).toString());
        edit.setVisible(true);
        
        if (edit.success){
            JOptionPane.showMessageDialog(this, "Successfully re-assigned!");
        } else {
            JOptionPane.showMessageDialog(this, "Reassignment unsuccessful.");
        }
        
    }
    
    //Removes a participant from the conference attendance
    private void removeConferenceAttendee(){
        
        //Variable declaration
        int selectedParticipant =  attendeeList.getSelectedRow();
        int choice = 0;
        
        //Error handling for no selected participant
        if(selectedParticipant == -1){
            JOptionPane.showMessageDialog(this, "No participant selected.");
            return;
        }
        
        int attendanceID =  Integer.parseInt(attendeeList.getValueAt(selectedParticipant, 0).toString());
        
        try(
                Connection con = DBConn.attemptConnection();
                PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM conference_registration.attends WHERE attendanceID = ?");
           ){
            
            pstmtDelete.setInt(1, attendanceID);
            
            //Confirmation for removal (0 == yes, 1 == no)
            choice = JOptionPane.showConfirmDialog(this, "Remove participant from conference?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if(choice == 0){
                pstmtDelete.executeUpdate();
                JOptionPane.showMessageDialog(this, "Removed from conference");
            } else if (choice == 1) {
                JOptionPane.showMessageDialog(this, "Operation cancelled.");
            }

        } catch (SQLException e){
            JOptionPane.showMessageDialog(this, "An error occured: "+ e.getMessage());
        }
    }
   
    //Exports the participant list for the currently selected conference
    private void exportCurrentConferenceList(){
        
        //Error handling for no selected conference
        if(selectConf.getSelectedIndex() == 0){
            JOptionPane.showMessageDialog(this, "Select a conference first!");
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV File");
        
        //Generates filename
        String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        chooser.setSelectedFile(new File ((String) selectConf.getSelectedItem() + currentTime+ ".csv"));
        int choice = chooser.showSaveDialog(this);
        
        if (choice == JFileChooser.APPROVE_OPTION){
            File savedFile = chooser.getSelectedFile();
            String filePath = savedFile.getAbsolutePath();
        
            //Prints information and formats it in a .csv file
            try(
                    Connection con = DBConn.attemptConnection();
                    PreparedStatement pstmtScraper = con.prepareStatement("SELECT empID, participant_name, fees_paid, date FROM conference_registration.attends WHERE conf_title = ?");
                    PrintWriter pw = new PrintWriter(new FileWriter(filePath));
               ){
                
                pstmtScraper.setString(1, (String) selectConf.getSelectedItem());
                ResultSet rs = pstmtScraper.executeQuery();
                
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                
                //Header
                pw.println("ATTENDANCE LIST");
                pw.println((String) selectConf.getSelectedItem());
                
                for(int i = 1; i <= columnCount; i++){
                    pw.print(rsMeta.getColumnName(i));
                    if(i < columnCount) {
                        pw.print(",");
                    }
                }
                pw.println();
                
                //Data
                while(rs.next()){
                    for(int i = 1; i <= columnCount; i++){
                        pw.print(rs.getString(i));
                        if (i < columnCount){
                            pw.print(",");
                        }
                    }
                    pw.println();
                }
                
                JOptionPane.showMessageDialog(this, "Conference data exported to: "+filePath);
                
            } catch (SQLException e){
                JOptionPane.showMessageDialog(this, "An error occured: "+ e.getMessage());
            } catch (FileNotFoundException e){
                JOptionPane.showMessageDialog(this, "File not found.");
            } catch (IOException e){
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        } 
    }
    
    //Exports all participants
    private void exportAllParticipants(){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV File");
        
        //Generates filename
        String currentTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        chooser.setSelectedFile(new File ((String) "MasterList" + currentTime+ ".csv"));
        int choice = chooser.showSaveDialog(this);
        
        if (choice == JFileChooser.APPROVE_OPTION){
            File savedFile = chooser.getSelectedFile();
            String filePath = savedFile.getAbsolutePath();
        
        
            try(
                    Connection con = DBConn.attemptConnection();
                    Statement stmtScraper = con.createStatement();
                    ResultSet rs = stmtScraper.executeQuery("SELECT * FROM conference_registration.participant");
                    PrintWriter pw = new PrintWriter(new FileWriter(filePath));
               ){
                
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                
                //Header
                pw.println("PARTICIPANT LIST");
                
                for(int i = 1; i <= columnCount; i++){
                    pw.print(rsMeta.getColumnName(i));
                    if(i < columnCount) {
                        pw.print(",");
                    }
                }
                pw.println();
                
                //Data
                while(rs.next()){
                    for(int i = 1; i <= columnCount; i++){
                        pw.print(rs.getString(i));
                        if (i < columnCount){
                            pw.print(",");
                        }
                    }
                    pw.println();
                }
                
                JOptionPane.showMessageDialog(this, "Masterlist exported to: "+filePath);
                
            } catch (SQLException e){
                JOptionPane.showMessageDialog(this, "An error occured: "+ e.getMessage());
            } catch (FileNotFoundException e){
                JOptionPane.showMessageDialog(this, "File not found.");
            } catch (IOException e){
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        } 
    }
    
    

    /**
     * Creates new form GlobalReports
     */
    public GlobalReports() {
    initComponents(); // This line is for your NetBeans UI     // Add this line to run your code
    loadConferenceAttendees();
    loadConferenceSelection();
    loadInfo();
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnExportAllParticipants = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        btnReturn = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        selectConf = new javax.swing.JComboBox<>();
        btnSelectConf = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        attendeeList = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        txtCashTotal = new javax.swing.JLabel();
        txtParticipantTotal = new javax.swing.JLabel();
        txtConfTotal = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        btnReassign = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        btnExportCurAttendance = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WorkTayo");

        jPanel2.setPreferredSize(new java.awt.Dimension(1400, 820));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setPreferredSize(new java.awt.Dimension(1400, 850));

        jLabel1.setText("WorkTayo!");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("System-wide analytics across all events");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel5.setText("Global Reports");

        btnExportAllParticipants.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnExportAllParticipants.setText("[ ↓ ] Export Master Participant List (CSV)");
        btnExportAllParticipants.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportAllParticipantsActionPerformed(evt);
            }
        });

        jLabel3.setText("Admin");

        btnReturn.setText("Go Back");
        btnReturn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReturnActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("View Attendees"));

        jLabel2.setText("Select Conference:");

        selectConf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnSelectConf.setText("Select");
        btnSelectConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectConfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectConf, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSelectConf)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectConf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectConf))
                .addGap(3, 3, 3))
        );

        attendeeList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Attendance ID", "Employee ID", "Full Name", "Fees Paid", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(attendeeList);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("KPI"));
        jPanel6.setLayout(new java.awt.GridLayout(3, 3, 0, 5));

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Total Lifetime Revenue");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(jLabel18);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Total Unique Participants");
        jLabel22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(jLabel22);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("Total Conferences Hosted");
        jLabel23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(jLabel23);

        txtCashTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtCashTotal.setText("// Cash Variable");
        txtCashTotal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(txtCashTotal);

        txtParticipantTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtParticipantTotal.setText("// Number of participants");
        txtParticipantTotal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(txtParticipantTotal);

        txtConfTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtConfTotal.setText("// num of conferences");
        txtConfTotal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(txtConfTotal);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("💰 ");
        jLabel20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(jLabel20);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("👤");
        jLabel15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(jLabel15);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("👥");
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel6.add(jLabel17);

        btnReassign.setText("Reassign");
        btnReassign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReassignActionPerformed(evt);
            }
        });

        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1378, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnReassign)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRemove)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReassign)
                    .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel6.setText("© WorkTayo LTD.");
        jLabel6.setAlignmentX(0.1F);

        btnExportCurAttendance.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnExportCurAttendance.setText("[ ↓ ] Export Current Attendance (CSV)");
        btnExportCurAttendance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportCurAttendanceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnExportAllParticipants))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExportCurAttendance))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3))
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(507, 507, 507))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(464, 464, 464)))
                                .addComponent(btnReturn)))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(618, 618, 618))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(btnReturn)
                        .addGap(33, 33, 33))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)))
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExportCurAttendance)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExportAllParticipants, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 9, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 906, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 9, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportCurAttendanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportCurAttendanceActionPerformed
        exportCurrentConferenceList();
    }//GEN-LAST:event_btnExportCurAttendanceActionPerformed

    private void btnReturnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReturnActionPerformed
        Home home = new Home();
        home.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnReturnActionPerformed

    private void btnExportAllParticipantsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportAllParticipantsActionPerformed
        exportAllParticipants();
    }//GEN-LAST:event_btnExportAllParticipantsActionPerformed

    private void btnSelectConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectConfActionPerformed
        if(selectConf.getSelectedIndex() == 0){
            JOptionPane.showMessageDialog(this, "No conference selected.");
            return;
        } 
        loadConferenceAttendees();
    }//GEN-LAST:event_btnSelectConfActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        removeConferenceAttendee();
        loadConferenceAttendees();
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnReassignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReassignActionPerformed
        reassignConferenceAttendee();
        loadConferenceAttendees();
    }//GEN-LAST:event_btnReassignActionPerformed

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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new GlobalReports().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable attendeeList;
    private javax.swing.JButton btnExportAllParticipants;
    private javax.swing.JButton btnExportCurAttendance;
    private javax.swing.JButton btnReassign;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnReturn;
    private javax.swing.JButton btnSelectConf;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> selectConf;
    private javax.swing.JLabel txtCashTotal;
    private javax.swing.JLabel txtConfTotal;
    private javax.swing.JLabel txtParticipantTotal;
    // End of variables declaration//GEN-END:variables
}
