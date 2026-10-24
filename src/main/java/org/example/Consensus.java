package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;

public class Consensus {
    private static ArrayList<org.example.Row> rows = new ArrayList<org.example.Row>();

    public static ArrayList<Row> getRows() {
        return rows;
    }

    public static void setRows(ArrayList<Row> rows) {
        Consensus.rows = rows;
    }

    // This method converts a CSV file of antibodies to an array list of
    // antibodies. Each element in the array list, a Row object, represents an
    // antibody, and each Row object has several instance variables, each of
    // which represent a characteristic of the antibody.
    // This method was acquired from https://www.baeldung.com/java-csv-file-array.
    public static ArrayList<Row> convertCSVToList(String fileName) throws IOException {
        ArrayList<Row> rows1 = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            String[] record = reader.readNext();
            if (record == null || record.length < 13) {
                throw new IOException("Expected a CSV header with at least 13 columns.");
            }
            int productiveColumn = -1;
            for (int i = 0; i < record.length; i++) {
                if (record[i].trim().equalsIgnoreCase("productive")) {
                    productiveColumn = i;
                }
            }
            // Preserve the original convention: element 0 is the input header.
            rows1.add(new Row());

            while ((record = reader.readNext()) != null) {
                if (record.length < 13 || (productiveColumn >= 0 && record.length <= productiveColumn)) {
                    throw new IOException("Input record has fewer columns than required.");
                }
                if (record[12].contains("*")) {
                    // The presence of a "*" indicates that there were stop
                    // codons in the CDR3 amino acid sequence. Stop codons should not be located
                    // in the CDR3 amino acid sequence, indicating an error in the sequencing
                    // data. Antibodies with this occurrence were omitted by the algorithm.
                } else {
                    Row row = new Row();
                    row.setSeq_id(record[0]);
                    row.setD_gene(record[7]);
                    row.setCdr3_length(record[10]);
                    row.setCdr3_aa(record[12]);
                    try {
                        if (Integer.parseInt(record[10]) < 0) {
                            throw new IOException("Negative CDR3 length for " + record[0]);
                        }
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid CDR3 length for " + record[0], e);
                    }
                    if (productiveColumn >= 0) {
                        row.setProductive(record[productiveColumn].trim());
                    }

                    row.setScore(0.0);
                    row.setYydfTypeScore(0.0);
                    row.setYydfPlacementScore(0.0);
                    row.setdChainScore(0.0);
                    row.setCdr3LengthScore(0.0);
                    rows1.add(row);
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Could not read CSV data.", e);
        }
        return rows1;
    }

    public static void parseByProductive(ArrayList<Row> list) throws IOException {
        for (int i = list.size() - 1; i > 0; i--) {
            if (list.get(i).getProductive() != null) {
                if (!(list.get(i).getProductive().equalsIgnoreCase("yes")
                        || list.get(i).getProductive().equalsIgnoreCase("productive"))) {
                    list.remove(i);
                }
            }
        }
    }
}
