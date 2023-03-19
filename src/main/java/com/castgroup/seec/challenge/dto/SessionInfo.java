package com.castgroup.seec.challenge.dto;

public class SessionInfo {
    private final String sessionId;
    private final long sessionTimeout;

    public SessionInfo(String sessionId, long sessionTimeout) {
        this.sessionId = sessionId;
        this.sessionTimeout = sessionTimeout;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }
}
