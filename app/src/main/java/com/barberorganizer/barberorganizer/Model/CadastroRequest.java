package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class CadastroRequest {

    private String email;
    private String password;

    @SerializedName("data")
    private UserData data;

    public CadastroRequest(
            String email,
            String password,
            String nome,
            String telefone
    ) {
        this.email = email;
        this.password = password;
        this.data = new UserData(nome, telefone);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNome() {
        return data != null ? data.getName() : null;
    }

    public String getTelefone() {
        return data != null ? data.getPhone() : null;
    }

    public static class UserData {

        @SerializedName("name")
        private String name;

        @SerializedName("phone")
        private String phone;

        public UserData(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }
    }
}