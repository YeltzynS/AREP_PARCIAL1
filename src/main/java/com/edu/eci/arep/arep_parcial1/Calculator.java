package com.edu.eci.arep.arep_parcial1;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

public class Calculator {

    private static final String RESPONSE_HEADER = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(37000);
        System.out.println("ReflexCalculator escuchando en puerto 37000...");

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
        System.out.println("Solicitud recibida en ReflexCalculator: " + inputLine);

        if (inputLine.startsWith("GET /compreflex?comando=")) {
            String command = inputLine.split("=")[1].split(" ")[0];
            String response = processCommand(command);
            out.println(RESPONSE_HEADER + response);
        } else {
            out.println("HTTP/1.1 404 Not Found\r\n\r\n");
        }

        in.close();
        out.close();
    }

    private static String processCommand(String command) {
        try {
            String functionName = command.substring(0, command.indexOf("("));
            String[] params = command.substring(command.indexOf("(") + 1, command.indexOf(")")).split(",");

            if (functionName.equals("bbl")) {
                List<Double> numbers = new ArrayList<>();
                for (String param : params) {
                    numbers.add(Double.parseDouble(param));
                }
                List<Double> sorted = bubbleSort(numbers);
                return "{\"operation\": \"bbl\", \"result\": " + sorted.toString() + "}";
            } else {
                Class<?> mathClass = Math.class;
                Method method;
                Object result;

                if (params.length == 1) {
                    double value = Double.parseDouble(params[0]);
                    method = mathClass.getMethod(functionName, double.class);
                    result = method.invoke(null, value);
                } else {
                    double value1 = Double.parseDouble(params[0]);
                    double value2 = Double.parseDouble(params[1]);
                    method = mathClass.getMethod(functionName, double.class, double.class);
                    result = method.invoke(null, value1, value2);
                }

                return String.format("{\"operation\": \"%s\", \"result\": %f}", functionName, result);
            }
        } catch (Exception e) {
            return "{\"error\": \"Comando no válido\"}";
        }
    }

    private static List<Double> bubbleSort(List<Double> numbers) {
        int n = numbers.size();
        Double[] array = numbers.toArray(new Double[0]);

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    double temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        return Arrays.asList(array);
    }
}
