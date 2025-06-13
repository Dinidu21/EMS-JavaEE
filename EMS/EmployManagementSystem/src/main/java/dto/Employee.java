package dto;

public class Employee {
    private int id;
    private String photo;
    private String name;
    private String email;
    private String role;
    private String address;
    private String status;
    private String created_at;
    private String updated_at;

    public Employee() {}

    public Employee(int id, String photo, String name, String email, String role, String address, String status, String created_at, String updated_at) {
        this.id = id;
        this.photo = photo;
        this.name = name;
        this.email = email;
        this.role = role;
        this.address = address;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}