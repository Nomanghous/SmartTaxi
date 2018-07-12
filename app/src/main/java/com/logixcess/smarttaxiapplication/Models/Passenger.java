package com.logixcess.smarttaxiapplication.Models;

public class Passenger
{
    private String fk_user_id;
    private Boolean is_working_student;
    private String orgnization_name;

    public String getFk_user_id() {
        return fk_user_id;
    }

    public void setFk_user_id(String fk_user_id) {
        this.fk_user_id = fk_user_id;
    }

    public Boolean getIs_working_student() {
        return is_working_student;
    }

    public void setIs_working_student(Boolean is_working_student) {
        this.is_working_student = is_working_student;
    }

    public String getOrgnization_name() {
        return orgnization_name;
    }

    public void setOrgnization_name(String orgnization_name) {
        this.orgnization_name = orgnization_name;
    }
}
