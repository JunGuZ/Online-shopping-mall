package com.example.entity;


import java.io.Serializable;

public class Pets implements Serializable {

    private Integer id;
    private Integer userId;
    private String petName;
    private String petKind;
    private String age;
    private String health;
    private String info;

    public Pets() {
    }

    public Pets(Integer id, Integer userId, String petName, String petKind, String age, String health, String info) {
        this.id = id;
        this.userId = userId;
        this.petName = petName;
        this.petKind = petKind;
        this.age = age;
        this.health = health;
        this.info = info;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetKind() {
        return petKind;
    }

    public void setPetKind(String petKind) {
        this.petKind = petKind;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
