package xyz.jinenze.wuhumc.action;

public class ActionContext {
    private boolean terminate;

    public boolean isTerminate() {
        return terminate;
    }

    public ActionContext setTerminate(boolean terminate) {
        this.terminate = terminate;
        return this;
    }

    ActionContext() {
    }
}
