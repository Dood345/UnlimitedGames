package com.appsters.unlimitedgames.app.data.model;

import com.appsters.unlimitedgames.app.util.RequestStatus;

public class FriendRequest {
    private String requestId;
    private String fromUserId;
    private String toUserId;
    private RequestStatus status;
    private long timestamp;

    public FriendRequest() {
        this.status = RequestStatus.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    public FriendRequest(String requestId, String fromUserId, String toUserId) {
        this.requestId = requestId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.status = RequestStatus.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
