# Antibody Ranking for HIV Vaccine Research

A Java pipeline that scores and ranks antibody sequences using biological features to identify and prioritize candidates for experimental evaluation.

I independently developed this multi-objective scoring framework as a scientific programmer in the Farzan Laboratory at Harvard Medical School and Boston Children's Hospital. Its outputs informed a study published in *Immunity* Journal, on which I am a co-author:

[Heavy-chain CDR3-engineered B cells facilitate in vivo evaluation of HIV-1 vaccine candidates](https://doi.org/10.1016/j.immuni.2023.07.003)

## Motivation

Selecting promising antibodies from sequencing data requires comparing several biological properties together. This project translates those criteria into an explicit, adjustable scoring system, automating candidate prioritization and making the reasons behind each ranking inspectable.

## How it works

For each input CSV, the pipeline:

1. Reads antibody identifiers, gene assignments, and CDR3 sequences.
2. Excludes sequences containing stop codons and filters productivity labels when available.
3. Calculates four component scores.
4. Combines the scores using a weighted sum.
5. Sorts candidates from highest to lowest score and exports their ranks and score breakdowns.

| Criterion | What it evaluates | Weight |
| --- | --- | --- |
| CDR3 length | Length of the antibody's CDR3 region, normalized by the input file's maximum sequence length; lengths below 24 receive zero | 35% |
| D-gene identity | Preference for specified diversity-gene assignments | 20% |
| Sulfation motif type | Sequence patterns associated with candidate sulfation motifs | 20% |
| Sulfation motif placement | Position of a candidate motif relative to the sequence center | 25% |

Weights are parameters in `Ranking.calculateScore`; their values are set in `Main.java`. Each file is ranked separately. This is a rule-based method, not a trained machine-learning model.

## My contribution

I independently designed the biological scoring framework and implemented the original Java pipeline. CSV utility methods adapted from external examples are attributed in the source code.

## Input and output

Input must be a comma-separated CSV with a header and at least 13 columns. The original column positions are retained:

| Column (1-based) | Field |
| --- | --- |
| 1 | Sequence ID |
| 8 | D-gene assignment |
| 11 | CDR3 length, as a nonnegative integer |
| 13 | CDR3 amino acid sequence, in uppercase |

An optional column named `productive` accepts `yes` or `productive`, ignoring case and surrounding whitespace. Other labels are excluded. If the column is absent, this label-based filter is skipped. Convert other encodings, such as `T`/`F`, before running.

Output contains each antibody's rank, identifier, gene assignment, sequence, overall score, and four component scores. CSV fields containing commas or quotes are escaped.

## Code organization

| File | Purpose |
| --- | --- |
| `Main.java` | Runs the pipeline and sets scoring weights |
| `Ranking.java` | Calculates component scores, combines them, and sorts candidates |
| `Consensus.java` | Reads and filters sequencing records |
| `Row.java` | Stores an antibody record and its scores |
| `ConsensusWriter.java` | Exports ranked results to CSV |

## Implementation notes
The program loads one input file into memory and uses an O(n log n) sort. Original experimental datasets are not included, but results of top-ranked antibodies are included. Presence of the YYD(F) motif in top antibodies is highlighted in yellow.

## Acknowledgments

CSV parsing uses [OpenCSV](https://opencsv.sourceforge.net/). Original source comments acknowledge examples from [Baeldung](https://www.baeldung.com/java-csv-file-array) and [Stack Overflow](https://stackoverflow.com/questions/67741964/how-to-write-list-of-objects-in-a-csv-using-opencsv-csvwriter-in-java).
