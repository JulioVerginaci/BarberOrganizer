package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class SupabaseUser {

    private String id;
    private String email;

    @SerializedName("user_metadata")
    private UserMetadata userMetadata;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UserMetadata getUserMetadata() {
        return userMetadata;
    }

    public static class UserMetadata {

        private String name;

        public String getName() {
            return name;
        }
    }
}