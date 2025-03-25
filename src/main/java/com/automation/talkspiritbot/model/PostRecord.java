package com.automation.talkspiritbot.model;


public record PostRecord(
        String id,
        String postCreator,
        String postDate,
        String postTitle,
        boolean hasAttachedFile,
        String postLink,
        String postAttachedFileName,
        String postAttachedFileButtonXPath
) {

    public boolean hasUnknownFields() {
        return "Unknown".equals(postCreator()) ||
                "Unknown".equals(postDate()) ||
                "Unknown".equals(postTitle());
    }


}
