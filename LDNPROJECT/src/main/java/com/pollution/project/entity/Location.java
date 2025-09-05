package com.pollution.project.entity;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ManyToMany
    private Set<User> users = new HashSet<>();

    @Embedded
    private AirQualityData airQualityData;

    // Constructors, getters, and setters

    public Location() {}

    public Location(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String name, Double latitude, Double longitude, AirQualityData airQualityData) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.airQualityData = airQualityData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public AirQualityData getAirQualityData() {
        return airQualityData;
    }

    public void setAirQualityData(AirQualityData airQualityData) {
        this.airQualityData = airQualityData;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", airQualityData=" + airQualityData +
                '}';
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
        user.getLocations().add(this);
    }

    public boolean removeUser(User user) {
        boolean res = this.users.remove(user);
        if (res) {
            user.getLocations().remove(this);
        }

        return res;
    }  

    public boolean editUser(User oldUser, User newUser) {
        if (removeUser(oldUser)) {
            addUser(newUser);
            return true;
        }
        return false;
    }

    public boolean hasUser(User user) {
        return this.users.contains(user);
    }

    public int getUserCount() {
        return this.users.size();
    }  

    public boolean isValid() {
        return this.name != null && !this.name.isEmpty()
                && this.latitude != null && this.latitude >= -90 && this.latitude <= 90
                && this.longitude != null && this.longitude >= -180 && this.longitude <= 180;
    }

    public boolean hasAirQualityData() {
        return this.airQualityData != null;
    }

    public void updateAirQualityData(AirQualityData newData) {
        this.airQualityData = newData;
    }

    public boolean isEmpty() {
        return (this.name == null || this.name.isEmpty())
                || this.latitude == null
                || this.longitude == null;
    }

    @Override 
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Location location = (Location) obj;

        if (!name.equals(location.name)) return false;
        if (!latitude.equals(location.latitude)) return false;
        return longitude.equals(location.longitude);
    }

    @Override
    public int hashCode() {
        return 31 + (id != null ? id.hashCode() : 0);
    }
}
