package com.edu.eci.arep.arep_parcial1;

import java.io.*;
import java.net.*;

public class HttpServer {

    private static final String CALCULATOR_URL = "http://localhost:37000/compreflex?comando=";
    private static final String RESPONSE_HEADER = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(37000);
        System.out.println("ServiceFacade escuchando en puerto 36000...");

        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                handleClientRequest(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String inputLine = in.readLine();

        if (inputLine == null) return;
        System.out.println("Solicitud recibida: " + inputLine);

        if (inputLine.startsWith("GET /calculadora")) {
            sendWebClient(out);
        } else if (inputLine.startsWith("GET /computar?comando=")) {
            String command = inputLine.split("=")[1].split(" ")[0];
            forwardToCalculator(out, command);
        } else {
            out.println("HTTP/1.1 404 Not Found\r\n\r\n");
        }

        in.close();
        out.close();
    }

    private static void sendWebClient(PrintWriter out) {
        String htmlResponse = """
            HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n
            <!DOCTYPE html>
            <html>
            <head><title>Calculadora Web</title></head>
            <body>
                <h2>Calculadora Web</h2>
                <input type='text' id='command' placeholder='Ejemplo: sin(30)'>
                <button onclick='compute()'>Calcular</button>
                <p>Resultado: <span id='result'></span></p>
                <script>
                    function compute() {
                        let command = document.getElementById('command').value;
                        fetch('/computar?comando=' + command)
                            .then(response => response.json())
                            .then(data => document.getElementById('result').innerText = data.result)
                            .catch(error => console.error('Error:', error));
                    }
                </script>
            </body>
            </html>
        """;
        out.println(htmlResponse);
    }

    private static void forwardToCalculator(PrintWriter out, String command) throws IOException {
        URL url = new URL(CALCULATOR_URL + command);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();
        in.close();

        out.println(RESPONSE_HEADER + response);
    }
}
