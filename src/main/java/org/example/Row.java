package org.example;

public class Row {
    private String seq_id;
    private String d_gene;
    private String cdr3_length;
    private String cdr3_aa;
    private String productive;
    private String score;
    private String cdr3LengthScore;
    private String yydfTypeScore;
    private String yydfPlacementScore;
    private String dChainScore;
    private int rank;
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getYydfPlacementScore() {
        return yydfPlacementScore;
    }

    public void setYydfPlacementScore(Double yydfPlacementScore) {
        String scoreString = "" + yydfPlacementScore;
        this.yydfPlacementScore = scoreString;
    }

    public void setYydfPlacementScoreRow1(String yydfPlacementScoreScore)
    {
        this.yydfPlacementScore = yydfPlacementScoreScore;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getScore() {
        return score;
    }

    public String getCdr3LengthScore() {
        return cdr3LengthScore;
    }

    public void setCdr3LengthScore(Double cdr3LengthScore) {
        String scoreString = "" + cdr3LengthScore;
        this.cdr3LengthScore = scoreString;
    }

    public void setCdr3LengthScoreRow1(String cdr3LengthScore) {
        this.cdr3LengthScore = cdr3LengthScore;
    }

    public String getYydfTypeScore() {
        return yydfTypeScore;
    }

    public void setYydfTypeScore(Double yydfTypeScore) {
        String scoreString = "" + yydfTypeScore;
        this.yydfTypeScore = scoreString;
    }

    public void setYydfTypeScoreRow1(String yydfTypeScore) {
        this.yydfTypeScore = yydfTypeScore;
    }

    public String getdChainScore() {
        return dChainScore;
    }

    public void setdChainScore(Double dChainScore) {
        String scoreString = "" + dChainScore;
        this.dChainScore = scoreString;
    }

    public void setdChainScoreRow1(String dChainScore) {
        this.dChainScore = dChainScore;
    }

    // This method toString was acquired from
    // https://stackoverflow.com/questions/67741964/how-to-write-list-of-objects-in-a-csv-using-opencsv-csvwriter-in-java
    @Override
    public String toString(){
        StringBuilder dataBuilder = new StringBuilder();
        appendFieldValue(dataBuilder, String.valueOf(rank));
        appendFieldValue(dataBuilder, seq_id);
        appendFieldValue(dataBuilder, d_gene);
        appendFieldValue(dataBuilder, cdr3_length);
        appendFieldValue(dataBuilder, cdr3_aa);
        appendFieldValue(dataBuilder, score);
        appendFieldValue(dataBuilder, cdr3LengthScore);
        appendFieldValue(dataBuilder, yydfTypeScore);
        appendFieldValue(dataBuilder, yydfPlacementScore);
        appendFieldValue(dataBuilder, dChainScore);
        return dataBuilder.substring(0, dataBuilder.length() - 1);
    }

    // This method appendFieldValue was acquired from
    // https://stackoverflow.com/questions/67741964/how-to-write-list-of-objects-in-a-csv-using-opencsv-csvwriter-in-java
    private void appendFieldValue(StringBuilder dataBuilder, String fieldValue) {
        if(fieldValue != null) {
            // Quote CSV fields containing delimiters, quotes, or line breaks.
            if (fieldValue.contains(",") || fieldValue.contains("\"")
                    || fieldValue.contains("\n") || fieldValue.contains("\r")) {
                dataBuilder.append('"').append(fieldValue.replace("\"", "\"\"")).append('"').append(",");
            } else {
                dataBuilder.append(fieldValue).append(",");
            }
        } else {
            dataBuilder.append("").append(",");
        }
    }

    public void setScore(double score) {
        String scoreString = "" + score;
        this.score = scoreString;
    }

    public String getSeq_id() {
        return seq_id;
    }

    public void setSeq_id(String seq_id) {
        this.seq_id = seq_id;
    }

    public String getProductive() {
        return productive;
    }

    public void setProductive(String productive) {
        this.productive = productive;
    }

    public String getD_gene() {
        return d_gene;
    }

    public void setD_gene(String d_gene) {
        this.d_gene = d_gene;
    }

    public String getCdr3_length() {
        return cdr3_length;
    }

    public void setCdr3_length(String cdr3_length) {
        this.cdr3_length = cdr3_length;
    }

    public String getCdr3_aa() {
        return cdr3_aa;
    }

    public void setCdr3_aa(String cdr3_aa) {
        this.cdr3_aa = cdr3_aa;
    }
}
