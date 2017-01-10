package util;

public enum Wood {
    NORMAL("Logs", "Tree"), OAK("Oak logs", "Oak tree");

    private String logName;
    private String treeName;

    Wood(String logName, String treeName){
        setLogName(logName);
        setTreeName(treeName);
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }
}