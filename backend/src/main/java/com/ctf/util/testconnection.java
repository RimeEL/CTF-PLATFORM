package com.ctf.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class testconnection {

    public static void main(String[] args) {

        String url = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String username = "postgres.hpbmeowxtqmbiyrbmgjm";
        String password = "UVcq4Ji4Q6LoLOAg";

        try {
            Connection conn = DriverManager.getConnection(url, username, password);

            if (conn != null) {
                System.out.println("Connection successful!");
            }

            conn.close();

        } catch (Exception e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }
    }
}