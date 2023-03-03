package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
   public static Client client1 = new Client("usr1");
   public static Client client2 = new Client("usr2");
    public static void main(String[] args) throws InterruptedException {
        DBClients();
        final int size = 17;
        DeadlockTask[] tasks = new DeadlockTask[size];
        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                tasks[i] = new DeadlockTask("first", client1, client2, 1);
            } else {
                tasks[i] = new DeadlockTask("second", client2, client1, 1);
            }
        }
        for (int i = 0; i < size; i++) {
            tasks[i].start();
        }
        for (int i = 0; i < size; i++) {
            tasks[i].join();
        }
    }
    public static void DBClients(){
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://192.168.0.104:5432/postgres";
            String login = "postgres";
            String password = "postgres";
            Connection con = DriverManager.getConnection(url, login, password);
            String query1 = "SELECT * FROM clients WHERE clients.name = '" + client1.getName() +"'";
            String query2 = "SELECT * FROM clients WHERE clients.name = '" + client2.getName() +"'";
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query1);
                while (rs.next()) {
                    client1.setCardNumber(Integer.parseInt(rs.getString("card")));
                    client1.setMoney(Integer.parseInt(rs.getString("money")));
                }
                ResultSet rs2 = stmt.executeQuery(query2);
                while (rs2.next()) {
                    client2.setCardNumber(Integer.parseInt(rs2.getString("card")));
                    client2.setMoney(Integer.parseInt(rs2.getString("money")));
                }
                rs.close();
                rs2.close();
                stmt.close();
            } finally {
                con.close();
            }
        }catch (Exception e) {
            System.out.println("Не удалось установить содинение с БД");
            e.printStackTrace();
        }
    }
}

