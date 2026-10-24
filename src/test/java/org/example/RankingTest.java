package org.example;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RankingTest {
    @TempDir
    Path temp;

    @Test
    void scoresRetainOriginalConstants() throws Exception {
        assertEquals(0.9, Ranking.dChainScore("D3-3"), 1e-12);
        assertEquals(0.7, Ranking.dChainScore("D3-9"), 1e-12);
        assertEquals(0.0, Ranking.dChainScore("other"), 1e-12);
        assertEquals(0.8, Ranking.YYDFTypeScore("AAAAAYYDFAAAAA"), 1e-12);
        assertEquals(0.6, Ranking.YYDFTypeScore("AAAAAYDFAAAAA"), 1e-12);
        assertEquals(0.4, Ranking.YYDFTypeScore("AAAAAYDAAAAA"), 1e-12);
        assertEquals(0.0, Ranking.YYDFTypeScore("AAAAAAAAAAAA"), 1e-12);
        assertEquals(0.1, Ranking.yydfPlacementScore("YD"), 1e-12);
        Row row = new Row();
        row.setdChainScore(0.9);
        row.setYydfTypeScore(0.8);
        row.setYydfPlacementScore(0.5);
        row.setCdr3LengthScore(1.0);
        assertEquals(0.815, Ranking.calculateScore(0.2, 0.2, 0.25, 0.35, row), 1e-12);
    }

    @Test
    void motifSearchMatchesIndependentCenterDistanceCalculation() throws Exception {
        Random random = new Random(42);
        String alphabet = "AYDFW";
        for (int length = 0; length <= 60; length++) {
            for (int example = 0; example < 100; example++) {
                StringBuilder sequence = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    sequence.append(alphabet.charAt(random.nextInt(alphabet.length())));
                }
                String seq = sequence.toString();
                int best = -1;
                double bestDistance = Double.POSITIVE_INFINITY;
                for (int i = 0; i + 1 < length; i++) {
                    String pair = seq.substring(i, i + 2);
                    double distance = Math.abs((i + 0.5) - (length - 1) / 2.0);
                    if ((pair.equals("YD") || pair.equals("DY")) && distance < bestDistance) {
                        best = i;
                        bestDistance = distance;
                    }
                }
                String expected = "";
                double placement = 0.0;
                if (best >= 0) {
                    boolean yd = seq.startsWith("YD", best);
                    expected = seq.substring(Math.max(0, best - (yd ? 1 : 2)),
                            Math.min(length, best + (yd ? 4 : 3)));
                    placement = length == 2 ? 0.1
                            : Math.min(best, length - best - 2) / (double) (length / 2);
                }
                assertEquals(expected, Ranking.getYYDF(seq), seq);
                assertEquals(placement, Ranking.yydfPlacementScore(seq), 1e-12, seq);
            }
        }
    }

    @Test
    void sortsDescendingAndPreservesHeaderAndTies() throws Exception {
        ArrayList<Row> rows = new ArrayList<>();
        Row header = new Row();
        rows.add(header);
        for (double score : new double[]{0.2, 0.9, 0.9, 0.1}) {
            Row row = new Row();
            row.setScore(score);
            rows.add(row);
        }
        Row firstTie = rows.get(2);
        Row secondTie = rows.get(3);
        Ranking.sortByScore(rows);
        assertSame(header, rows.get(0));
        assertSame(firstTie, rows.get(1));
        assertSame(secondTie, rows.get(2));
        assertEquals("0.1", rows.get(4).getScore());
    }

    @Test
    void csvRoundTripsSpecialCharactersWithoutExtraColumn() throws Exception {
        Row row = new Row();
        row.setSeq_id("sample, \"quoted\"\nnext line");
        try (CSVReader reader = new CSVReader(new StringReader(row.toString()))) {
            String[] fields = reader.readNext();
            assertEquals(10, fields.length);
            assertEquals(row.getSeq_id(), fields[1]);
        }
    }

    @Test
    void endToEndFiltersScoresAndExports() throws Exception {
        Path output = temp.resolve("ranked.csv");
        Main.main(new String[]{"examples/synthetic-antibodies.csv", output.toString()});
        try (CSVReader reader = new CSVReader(Files.newBufferedReader(output))) {
            List<String[]> rows = reader.readAll();
            assertEquals(4, rows.size());
            assertEquals("Rank", rows.get(0)[0]);
            assertEquals("synthetic_A", rows.get(1)[1]);
            assertEquals("1", rows.get(1)[0]);
            for (int i = 1; i < rows.size(); i++) {
                assertEquals(10, rows.get(i).length);
                assertEquals(Integer.toString(i), rows.get(i)[0]);
                if (i > 1) {
                    assertTrue(Double.parseDouble(rows.get(i - 1)[5]) >= Double.parseDouble(rows.get(i)[5]));
                }
            }
        }
        assertEquals(24.0, Ranking.getMaxCdr3Length());
        assertEquals(0.0, Ranking.cdr3LengthScore(23));
        assertEquals(1.0, Ranking.cdr3LengthScore(24));
    }

    @Test
    void supportsEmptyAndUnlabeledDatasets() throws Exception {
        String header = Files.readAllLines(Path.of("examples/synthetic-antibodies.csv")).get(0);
        Path input = temp.resolve("empty.csv");
        Path output = temp.resolve("empty-ranked.csv");
        Files.writeString(input, header + "\n");
        Main.main(new String[]{input.toString(), output.toString()});
        assertEquals(1, Files.readAllLines(output).size());
        assertEquals(0.0, Ranking.cdr3LengthScore(24));

        // No productivity column: keep rows rather than inventing labels.
        Files.writeString(input, header.substring(0, header.lastIndexOf(',')) + "\n"
                + "sample,,,,,,,D3-3,,,2,,YD\n");
        Main.main(new String[]{input.toString(), output.toString()});
        assertEquals(2, Files.readAllLines(output).size());
    }

    @Test
    void rejectsMalformedInputAndSameFileOutput() throws Exception {
        Path input = temp.resolve("bad.csv");
        Files.writeString(input, "only,two\n");
        assertThrows(java.io.IOException.class, () -> Consensus.convertCSVToList(input.toString()));
        assertThrows(java.io.IOException.class,
                () -> Main.main(new String[]{input.toString(), input.toString()}));
        assertEquals("only,two\n", Files.readString(input));
    }
}
