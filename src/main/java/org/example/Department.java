package org.example;

public class Department {
    public final String name;
    public final boolean inDepartment;
    public final int classroom;


    public Department(String name, boolean inDepartment, int classroom) {
        this.name = name;
        this.inDepartment = inDepartment;
        this.classroom = inDepartment ? classroom : -1;
    }
}
