package itstep.learning.models;

import org.apache.commons.fileupload.FileItem;

import java.util.Date;

public class SignupFormModel {
    private String name;
    private String email;
    private String phone;
    private String login;
    private String password;
    private String repeat;
    private Date birthdate;
    private String avatar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
/*
Д.З. Реалізувати сервіси парсингу форм (FormParseService)
 та управління збереженням/видачею файлів (StorageService)
у власних курсових проєктах
 */
