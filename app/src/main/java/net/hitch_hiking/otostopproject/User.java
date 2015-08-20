package net.hitch_hiking.otostopproject;

/**
 * Created by root on 7/9/15.
 */
public class User {
    private String fullName;
    private String email;
    private int rating;
    private int birthYear;
    private String userId;
    private String carInfo;

    public String getCarInfo() {
        return carInfo;
    }

    private boolean isDriver = false;

    public String getUserId() {
        return userId;
    }

    public User(int birthYear, String fullName, int rating, boolean isDriver, String email,
                String userId, String carInfo) {
        this.birthYear = birthYear;
        this.fullName = fullName;
        this.rating = rating;
        this.isDriver = isDriver;
        this.email = email;
        this.userId = userId;
        this.carInfo = carInfo;
    }

    public User() {}
    public User(String fullName, int birthYear) {
        this.fullName = fullName;
        this.birthYear = birthYear;
    }
    public long getBirthYear() {return birthYear;}
    public String getFullName() {return fullName;}
    public int getRating() {return rating;}
    public boolean getIsDriver() {return isDriver;}
    public String getEmail() {return email;}

}
