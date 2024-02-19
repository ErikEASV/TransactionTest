package com.example.TransactionTest;


import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author smsj
 * tilrettet af EK
 */
public class TransactionTestCons {

    public static void main(String[] args) throws Exception {
        transactionTest();
        //batchTest();
    }

    private static void transactionTest() throws SQLException {

        Connection conn = null;

        // Opsæt SQL kald
        String withdrawMoneySQL = "UPDATE Account SET Balance = Balance - ? WHERE ID = ?";
        String depositMoneySQL = "UPDATE Account SET Balance = Balance + ? WHERE ID = ?";

        try {
            // Start forbindelse til database
            conn = DBConnector.getConnection();

            // Vi starter transaktionen
            conn.setAutoCommit(false);

            //SQL statement #1 - hæv 1000 fra Account #2
            PreparedStatement withdrawMoney = conn.prepareStatement(withdrawMoneySQL);
            withdrawMoney.setInt(1, 1000); //money
            withdrawMoney.setInt(2, 2); //account id
            withdrawMoney.executeUpdate();

            // Fremprovokér evt exception... og se hvad der sker
            // int i = 10 / 0;

            //SQL statement #2 - indsæt 1000 to Account #1
            PreparedStatement depositMoney = conn.prepareStatement(depositMoneySQL);
            depositMoney.setInt(1, 1000); //money
            depositMoney.setInt(2, 1);  //account ID
            depositMoney.executeUpdate();

            // Nu skulle vi gerne kunne committe...
            conn.commit();

            // Hvis commit gik igennem er transaktionen fuldført
            System.out.println("Transaction committed succesfully");

            // Vi checker og resetter databasen
            String sql = "SELECT * FROM Account";
            System.out.println("Resultat af sql: "+sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                    String id = rs.getString("id");
                    int balance = rs.getInt("balance");
                    System.out.println("id: " + id + " saldo: " + balance);
                }

            sql = "UPDATE Account Set balance = 100 where id = 1";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            sql = "UPDATE Account Set balance = 1000 where id = 2";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);

        } catch (Exception e) {
            if (conn != null) {
                // Ups, der noget som gik galt. Vi ruller tilbage
                conn.rollback();
                System.out.println("Databasen rulles tilbage...");
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                // Afslut og ryd op
                conn.setAutoCommit(true);
                conn.close(); // dette vil også smide evt andre ufuldendte transaktioner ud
            }
        }

    }


    private static void batchTest() {
        try (Connection conn = new DBConnector().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Account VALUES (?,?)");

            for (int i = 5; i < 10000; i++) {
                stmt.setString(1, "Account #" + i);
                stmt.setInt(2, i);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}