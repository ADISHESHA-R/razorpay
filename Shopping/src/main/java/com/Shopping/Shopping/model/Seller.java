package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Entity
@Table(name = "sellers")
@Getter
@Setter
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String whatsappNumber;
    private String businessEmail;
    private String gstNumber;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    public String getPhotoBase64() {
        return this.photo != null ? Base64.getEncoder().encodeToString(this.photo) : null;
    }
}
