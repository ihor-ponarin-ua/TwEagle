package com.mr_faton.core.table;

import java.util.Date;

public class TwitterMessage {
    private int id;
    private String message;
    private boolean tweet;

    private String owner;
    private boolean ownerMale;

    private String recipient;
    private boolean recipientMale;

    private Date postedDate;

    private boolean synonymised;
    private boolean posted;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTweet() {
        return tweet;
    }

    public void setTweet(boolean tweet) {
        this.tweet = tweet;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isOwnerMale() {
        return ownerMale;
    }

    public void setOwnerMale(boolean ownerMale) {
        this.ownerMale = ownerMale;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isRecipientMale() {
        return recipientMale;
    }

    public void setRecipientMale(boolean recipientMale) {
        this.recipientMale = recipientMale;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Date postedDate) {
        this.postedDate = postedDate;
    }

    public boolean isSynonymised() {
        return synonymised;
    }

    public void setSynonymised(boolean synonymised) {
        this.synonymised = synonymised;
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }
}
