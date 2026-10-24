package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class Ranking {
    private static double maxCdr3Length;
    private static int cdr3_aaColumn = 12;
    private static int d_geneColumn = 7;
    private static int cdr3_length = 10;

    public static void setMaxCdr3Length(String fileName) throws IOException {
        int currentMax = 0;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            String[] header = reader.readNext(); // Skip column titles.
            if (header == null || header.length <= cdr3_aaColumn) {
                throw new IOException("Expected a CSV header with at least 13 columns.");
            }
            for (String[] record = reader.readNext(); record != null; record = reader.readNext()) {
                if (record.length <= cdr3_aaColumn) {
                    throw new IOException("Input record has fewer than 13 columns.");
                }
                if (record[cdr3_aaColumn].length() > currentMax) {
                    currentMax = record[cdr3_aaColumn].length();
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Could not read CSV data.", e);
        }
        maxCdr3Length = currentMax;
    }

    public static double getMaxCdr3Length() {
        return maxCdr3Length;
    }

    // Calculates the score for the HCDR3 length property.
    public static double cdr3LengthScore(int length) throws IOException {
        double score = 0.1;
        if (length < 24 || maxCdr3Length == 0) {
            score = 0.0;
            return score;
        }
        score = length / maxCdr3Length;
        return score;
    }

    // Calculates the score for the sulfation motif type property.
    public static double YYDFTypeScore(String cdr3AA) throws IOException {
        String result = getYYDF(cdr3AA);
        double score = 0.0;

        if (result.equals("")) {
            return 0.0; // If there is no YYDF, the score is 0.0.
        }
        if (result.contains("YYDF") || result.contains("FDYY")) {
            score = 0.8;
        } else if (result.contains("YYD") || result.contains("DYY")
                || result.contains("YDF") || result.contains("FDY")) {
            // Since the substring includes context before and after the Y or D, it will work.
            score = 0.6;
        } else {
            score = 0.4;
        }

        if (result.contains("FW")) {
            score -= 0.2;
        } else if (cdr3AA.contains("W")) {
            score += 0.1;
        }
        return score;
    }

    // Calculates the score for the sulfation motif placement property.
    public static double yydfPlacementScore(String cdr3AA) throws IOException {
        double indexRight = -1;
        double indexLeft = -1;
        double score = 0;
        if (cdr3AA.length() <= 1) {
            return 0.0;
        }
        if (cdr3AA.length() == 2) {
            if (cdr3AA.contains("YD") || cdr3AA.contains("DY")) {
                return 0.1;
            }
            return 0.0;
        }
        for (int i = 0; i < cdr3AA.length() / 2; i++) {
            if (cdr3AA.substring(i, i + 2).equals("YD")) {
                indexLeft = i;
            } else if (cdr3AA.substring(i, i + 2).equals("DY")) {
                indexLeft = i;
            }
        }
        for (int i = cdr3AA.length() - 1; i > cdr3AA.length() / 2; i--) {
            if (cdr3AA.substring(i - 1, i + 1).equals("YD")) {
                indexRight = i;
            } else if (cdr3AA.substring(i - 1, i + 1).equals("DY")) {
                indexRight = i;
            }
        }
        if (indexLeft == -1 && indexRight == -1) {
            return 0.0;
        }
        // Greater distance from an end means closer to the center. Prefer left on a tie.
        if (indexLeft != -1 && (indexRight == -1 || indexLeft >= cdr3AA.length() - 1 - indexRight)) {
            score = indexLeft / (double) (cdr3AA.length() / 2);
            return score;
        } else {
            score = (cdr3AA.length() - 1 - indexRight) / (double) (cdr3AA.length() / 2);
            return score;
        }
    }

    // Helper method for the YYDFTypeScore method. Returns the centermost sulfation motif.
    public static String getYYDF(String cdr3AA) throws IOException {
        String currentRight = "";
        String currentLeft = "";
        int indexRight = -1;
        int indexLeft = -1;

        if (cdr3AA.length() <= 2) {
            if (cdr3AA.contains("YD") || cdr3AA.contains("DY")) {
                return cdr3AA;
            }
            return "";
        }
        for (int i = 0; i < cdr3AA.length() / 2; i++) {
            if (cdr3AA.substring(i, i + 2).equals("YD")) {
                indexLeft = i;
                currentLeft = cdr3AA.substring(Math.max(0, i - 1), Math.min(cdr3AA.length(), i + 4));
            } else if (cdr3AA.substring(i, i + 2).equals("DY")) {
                indexLeft = i;
                currentLeft = cdr3AA.substring(Math.max(0, i - 2), Math.min(cdr3AA.length(), i + 3));
            }
        }
        // Scan from the right end toward the center, keeping the centermost match.
        for (int i = cdr3AA.length() - 2; i >= cdr3AA.length() / 2; i--) {
            if (cdr3AA.substring(i, i + 2).equals("YD")) {
                indexRight = i;
                currentRight = cdr3AA.substring(Math.max(0, i - 1), Math.min(cdr3AA.length(), i + 4));
            } else if (cdr3AA.substring(i, i + 2).equals("DY")) {
                indexRight = i;
                currentRight = cdr3AA.substring(Math.max(0, i - 2), Math.min(cdr3AA.length(), i + 3));
            }
        }
        if (indexLeft == -1) {
            return currentRight;
        }
        if (indexRight == -1) {
            return currentLeft;
        }
        // Greater distance from an end means closer to the center. Prefer left on a tie.
        if (indexLeft >= cdr3AA.length() - 2 - indexRight) {
            return currentLeft;
        } else {
            return currentRight;
        }
    }

    // Sorts the array list of antibodies from greatest to least total score.
    public static void sortByScore(ArrayList<Row> rows) throws IOException {
        // Keep the header at index 0. Replace quadratic selection sort with a library sort.
        rows.subList(1, rows.size()).sort(
                Comparator.comparingDouble((Row row) -> Double.parseDouble(row.getScore())).reversed());
    }

    // This method calculates the total score for an antibody. The weights
    // were made parameters instead of hardcoded values to make them easier to adjust.
    public static double calculateScore(double dChain, double sulfType,
                                        double sulfPlace, double length, Row row) {
        double score = 0;
        double dchainScore = Double.valueOf(row.getdChainScore());
        double sulfTypeScore = Double.valueOf(row.getYydfTypeScore());
        double sulfPlaceScore = Double.valueOf(row.getYydfPlacementScore());
        double lengthScore = Double.valueOf(row.getCdr3LengthScore());
        score = dchainScore * dChain + sulfTypeScore * sulfType
                + sulfPlaceScore * sulfPlace + lengthScore * length;
        return score;
    }

    // Calculates the score for the Diversity (D)-gene property.
    public static double dChainScore(String dChain) throws IOException {
        double score = 0.0;
        if (dChain.contains("D3-3")) {
            score = 0.9;
        } else if (dChain.contains("D3-9")) {
            score = 0.7;
        } else if (dChain.contains("D3-16")) {
            score = 0.7;
        } else if (dChain.contains("D3-22")) {
            score = 0.7;
        } else {
            score = 0.0;
        }
        return score;
    }
}
