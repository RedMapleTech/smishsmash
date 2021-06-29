package tech.redmaple.smishsmash.analysis;

public class AnalysedURL {
    private boolean suspicious;
    private String analysis;
    private boolean analysed = false;

    public boolean isAnalysed() {
        return analysed;
    }

    public void setAnalysed(boolean analysed) {
        this.analysed = analysed;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public boolean isSuspicious() {
        return suspicious;
    }

    public void setSuspicious(boolean suspicious) {
        this.suspicious = suspicious;
    }
}
