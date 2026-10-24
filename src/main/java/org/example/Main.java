// All code was written independently unless otherwise specified.
// Restored from the original PDF with limited corrections documented in CHANGES.md.
package org.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar antibody-ranking.jar input.csv output.csv");
            System.exit(1);
        }
        if (Paths.get(args[0]).toRealPath().equals(Paths.get(args[1]).toAbsolutePath().normalize())
                || (java.nio.file.Files.exists(Paths.get(args[1]))
                && java.nio.file.Files.isSameFile(Paths.get(args[0]), Paths.get(args[1])))) {
            throw new IOException("Input and output must be different files.");
        }

        // CSV file name refers to a CSV holding antibody sequencing data.
        Ranking.setMaxCdr3Length(args[0]);
        ArrayList<Row> rows = Consensus.convertCSVToList(args[0]);
        Consensus.parseByProductive(rows); // Removes non-productive antibodies when labeled.

        // The first row represents the titles of columns in the CSV.
        String header = "Rank,Sequence ID,Diversity (D)-gene,CDR3 Length,CDR3 Amino Acid Sequence,"
                + "Overall Score,CDR3 Length Score,Sulfation Motif Type Score,"
                + "Sulfation Motif Placement Score,Diversity (D)-gene Score";

        // Iterate through the array list of antibodies, setting the appropriate
        // instance variables to the results of the property score calculating methods.
        for (int i = 1; i < rows.size(); i++) {
            rows.get(i).setCdr3LengthScore(Ranking.cdr3LengthScore(Integer.valueOf(rows.get(i).getCdr3_length())));
            rows.get(i).setdChainScore(Ranking.dChainScore(rows.get(i).getD_gene()));
            rows.get(i).setYydfTypeScore(Ranking.YYDFTypeScore(rows.get(i).getCdr3_aa()));
            rows.get(i).setYydfPlacementScore(Ranking.yydfPlacementScore(rows.get(i).getCdr3_aa()));
            rows.get(i).setScore(Ranking.calculateScore(0.2, 0.2, 0.25, 0.35, rows.get(i)));
        }

        Ranking.sortByScore(rows); // Sorts the antibodies from greatest to least score.
        for (int i = 1; i < rows.size(); i++) {
            rows.get(i).setRank(i); // Assigns each antibody its rank.
        }
        // The input header is not an antibody and should not be exported as one.
        ConsensusWriter.writeToCsv(rows.subList(1, rows.size()), header, args[1]);
    }
}
