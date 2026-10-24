package org.example;

import java.io.*;
import java.util.List;

public class ConsensusWriter {
    // This method writeToCsv was acquired from
    // https://stackoverflow.com/questions/67741964/how-to-write-list-of-objects-in-a-csv-using-opencsv-csvwriter-in-java
    public static void writeToCsv(List<Row> sampleList, String header, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println(header);
            for (Row sample : sampleList) {
                writer.println(sample.toString());
            }
            if (writer.checkError()) {
                throw new IOException("Could not write output CSV: " + fileName);
            }
        }
    }
}
