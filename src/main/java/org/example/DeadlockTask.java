package org.example;

import java.sql.*;

public class DeadlockTask extends Thread {
    private static final String url = "jdbc:postgresql://192.168.0.104:5432/postgres";
    private static final String login = "postgres";
    private static final String password = "postgres";

    private Client clientFrom;
    private Client clientTo;
    private static int money;


    public DeadlockTask(String name, Client clientFrom, Client clientTo, int money) {
        super(name);
        this.clientFrom = clientFrom;
        this.clientTo = clientTo;
        this.money = money;
    }

    public void DBClientsMoney(){

        String query1 = "UPDATE clients SET money = money - " + money +""+" WHERE clients.name = '" + clientFrom.name +"'";
        String query2 = "UPDATE clients SET money = money + " + money +""+" WHERE clients.name = '" + clientTo.name +"'";

        try (Connection con = DriverManager.getConnection(url, login, password);
             PreparedStatement stmt1 = con.prepareStatement(query1);
             PreparedStatement stmt2 = con.prepareStatement(query2))
            {
                stmt1.executeUpdate();
                stmt2.executeUpdate();

            }
        catch (Exception e) {
            System.out.println("Не удалось выполнить вставку");
        }
    }

    public void run() {
        while (true) {
            if (clientFrom.tryLock()) {
                if (clientTo.tryLock()) {
                    try {
                        DBClientsMoney();
                        System.out.println(
                                String.format(
                                        "Клиент %s перевёл %d рублей клиенту %s",
                                        clientFrom.name,
                                        money,
                                        clientTo.name
                                )
                        );
                    }
                    finally {
                        clientTo.unlock();
                        clientFrom.unlock();
                        break;
                    }
                }
                else {
                    clientFrom.unlock();
                }
            }
        }
    }
}
