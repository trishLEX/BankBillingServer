package ru.bmstu.BankBillingServer;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

class ServerTest {
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";

    @Test
    public void doTests() {
        createAccount(1);
        deposit(1, 1.5f);
        withdraw(1, 1.5f);
        balance(1);
        delete(1);
        accounts();

        createAccount(2);
        deposit(2, 2.0f);
        withdraw(2, 1.0f);
        delete(2); //BAD REQUEST, sum > 0
        accounts();
    }

    private void createAccount(int id) {
        String accountURL = "http://localhost:9696/bankaccount/" + id + "?id=" + id;
        execute(accountURL, POST);
        accounts();
    }

    private void deposit(int id, float sum) {
        String url = "http://localhost:9696/bankaccount/" + id + "/deposit" + "?id=" + id + "&sum=" + sum;
        execute(url, PUT);
        accounts();
    }

    private void withdraw(int id, float sum) {
        String url = "http://localhost:9696/bankaccount/" + id + "/withdraw" + "?id=" + id + "&sum=" + sum;
        execute(url, PUT);
        accounts();
    }

    private void balance(int id) {
        String res = execute("http://localhost:9696/bankaccount/" + id + "/balance?id=" + id, GET);
        System.out.println("BALANCE OF " + id + ": " + res);
    }

    private void delete(int id) {
        String url = "http://localhost:9696/bankaccount/" + id + "/delete" + "?id=" + id;
        execute(url, DELETE);
        accounts();
    }

    private void accounts() {
        String res = execute("http://localhost:9696/bankaccount", GET);
        System.out.println(res);
    }

    private String execute(String URL, String method) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            System.out.println("BAD REQUEST");
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}