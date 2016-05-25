package com.pulzit.discovery.domain;

/**
 * Created by gastonsanguinetti on 03/05/16.
 */
public class NodeData {

    private String name;
    private String desc;
    private int followersCount;
    private String lastUpdate;
    private Long lastStatusId;
    private String imageUrl;

    public NodeData() {}

    public NodeData(String name, int followersCount, String desc, String lastUpdate, Long lastStatusId,
                    String imageUrl) {
        this.name = name;
        this.desc = desc;
        this.followersCount = followersCount;
        this.lastUpdate = lastUpdate;
        this.lastStatusId = lastStatusId;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getLastStatusId() {
        return lastStatusId;
    }

    public void setLastStatusId(Long lastStatusId) {
        this.lastStatusId = lastStatusId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
