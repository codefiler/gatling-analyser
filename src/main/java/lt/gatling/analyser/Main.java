package lt.gatling.analyser;

import lt.gatling.analyser.exceptions.NoSuitableDataException;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Main {
    private static Map<String, ResponseVector> vector = new HashMap();
    private static long from = 0;
    private static long to = 0;

    private static boolean goodTime(String timestamp) {
        if (Long.parseLong(timestamp) >= from && Long.parseLong(timestamp) <= to) {
            return true;
        } else {
            return false;
        }
    }
    /** Формат 2021-09-23 23:59:59 */
    private static void setInterval(String fromTime, String toTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime ldtFrom = LocalDateTime.parse(fromTime, formatter);
        ZonedDateTime zdtFrom = ldtFrom.atZone(ZoneId.of("Europe/Moscow"));
        from = zdtFrom.toInstant().toEpochMilli();

        LocalDateTime ldtTo = LocalDateTime.parse(toTime, formatter);
        ZonedDateTime zdtTo = ldtTo.atZone(ZoneId.of("Europe/Moscow"));
        to = zdtTo.toInstant().toEpochMilli();
    }

    private static void readFromLogHttp(String filename) throws IOException, NoSuitableDataException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        String[] columns;
        do {
            line = reader.readLine();
            if (line == null) {
                break;
            }

            columns = line.split("\t+");

            if (columns[0].equals("REQUEST")) {
                if (columns.length < 5) {
                    System.out.println("ОШИБКА: обрезанная строка");
                    continue;
                }

                if (!goodTime(columns[2])) {
                    continue;
                }

                if (vector.get(columns[1]) == null) {
                    vector.put(columns[1], new ResponseVector());
                }

                if (columns[4].equals("OK")) {
                    vector.get(columns[1]).add((Double.parseDouble(columns[3]) - Double.parseDouble(columns[2])) / 1000);
                }
                else {
                    vector.get(columns[1]).addError();
                }
            }
        } while (line != null);

        reader.close();

        if (vector.size() == 0) {
            throw new NoSuitableDataException();
        }
    }

    private static void readFromLogMq(String filename) throws IOException, NoSuitableDataException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        String[] columns;
        do {
            line = reader.readLine();
            if (line == null) {
                break;
            }

            columns = line.split("\t+");

            if (columns[0].equals("REQUEST")) {
                if (columns.length < 6) {
                    System.out.println("ОШИБКА: обрезанная строка");
                    continue;
                }

                if (!goodTime(columns[3])) {
                    continue;
                }

                if (vector.get(columns[2]) == null) {
                    vector.put(columns[2], new ResponseVector());
                }

                if (columns[5].equals("OK")) {
                    vector.get(columns[2]).add((Double.parseDouble(columns[4]) - Double.parseDouble(columns[3])) / 1000);
                }
                else {
                    vector.get(columns[2]).addError();
                }
            }
        } while (line != null);

        reader.close();

        if (vector.size() == 0) {
            throw new NoSuitableDataException();
        }
    }

    private static void printToCsv(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("Transaction,Min,Max,Avg,90perc,95perc,99perc,StdDev,Pass,Fail");
        for (Map.Entry<String, ResponseVector> entry : vector.entrySet()) {
            printWriter.println(
                    entry.getKey() + "," +
                            entry.getValue().getMin() + "," +
                            entry.getValue().getMax() + "," +
                            entry.getValue().getAvg() + "," +
                            entry.getValue().getPercentile(90) + "," +
                            entry.getValue().getPercentile(95) + "," +
                            entry.getValue().getPercentile(99) + "," +
                            entry.getValue().getStdDev() + "," +
                            entry.getValue().getPass() + "," +
                            entry.getValue().getErrors());
        }
        printWriter.close();
    }

    public static void main(String[] args) throws IOException, NoSuitableDataException {
        FileInputStream fis;
        Properties property = new Properties();

        fis = new FileInputStream("config.properties");
        property.load(fis);

        String fromTime = property.getProperty("date.time.from");
        String toTime = property.getProperty("date.time.to");
        String simulationFile = property.getProperty("file.simulation");
        String resultFile = property.getProperty("file.result");

        setInterval(fromTime, toTime);

        if (property.getProperty("gatling.protocol").equalsIgnoreCase("http")) {
            readFromLogHttp(simulationFile);
        } else if (property.getProperty("gatling.protocol").equalsIgnoreCase("mq")) {
            readFromLogMq(simulationFile);
        }

        printToCsv(resultFile);
    }
}
