package ru.linachan.jormungand;

public enum JormungandSubProcessState {

    READY(0),
    WAITING(1),
    RUNNING(2),
    FINISHED(3),
    ERROR(4),
    NOT_EXISTS(-1);

    private Integer state;

    JormungandSubProcessState(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return this.state;
    }
}
