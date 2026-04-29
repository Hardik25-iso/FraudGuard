package com.fraudguard.hardik.model.user;

public class User {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private String accountId;

    public User(Long id, String username, String password, String fullName, String role, String accountId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.accountId = accountId;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getAccountId() { return accountId; }
}
