package com.wf.yeecode.a01;

public class User {
    private Integer id;
    private String name;
    private String email;
    private Integer age;
    private Integer sex;
    private String schoolName;


    public User() {
    }

    public User(String name, String email, Integer age, Integer sex, String schoolName) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.sex = sex;
        this.schoolName = schoolName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

  @Override
  public String toString() {
    return "User{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", email='" + email + '\'' +
      ", age=" + age +
      ", sex=" + sex +
      ", schoolName='" + schoolName + '\'' +
      '}';
  }
}
