package org.example;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> subjects = Arrays.asList("Artificial Intelligence", "Data Structures", "Quantum Computing", "Linear Algebra", "Thermodynamics", "Project Management");
        List<String> teachers = Arrays.asList("Ivanova", "Petrov", "Kuznetsov", "Popova", "Sokolov", "Mikhailov");
        List<String> groups = Arrays.asList("COMP-101", "PHYS-201", "COMP-102", "PHYS-202", "ENG-301", "ENG-302");

        Map<String, List<String>> teacherSubjects = new HashMap<>();
        teacherSubjects.put("Ivanova", Arrays.asList("Artificial Intelligence", "Linear Algebra"));
        teacherSubjects.put("Petrov", Arrays.asList("Data Structures", "Project Management"));
        teacherSubjects.put("Kuznetsov", Collections.singletonList("Quantum Computing"));
        teacherSubjects.put("Popova", Collections.singletonList("Thermodynamics"));
        teacherSubjects.put("Sokolov", Collections.singletonList("Data Structures"));
        teacherSubjects.put("Mikhailov", Arrays.asList("Project Management", "Linear Algebra"));

        Map<String, List<String>> groupsSubjects = new HashMap<>();
        groupsSubjects.put("COMP-101", Arrays.asList("Artificial Intelligence", "Linear Algebra"));
        groupsSubjects.put("COMP-102", Arrays.asList("Data Structures", "Project Management"));
        groupsSubjects.put("PHYS-201", Collections.singletonList("Quantum Computing"));
        groupsSubjects.put("PHYS-202", Collections.singletonList("Thermodynamics"));
        groupsSubjects.put("ENG-301", Collections.singletonList("Data Structures"));
        groupsSubjects.put("ENG-302", Collections.singletonList("Project Management"));

        Map<String, Integer> teacherMaxHours = new HashMap<>();
        teacherMaxHours.put("Ivanova", 20);
        teacherMaxHours.put("Petrov", 30);
        teacherMaxHours.put("Kuznetsov", 20);
        teacherMaxHours.put("Popova", 10);
        teacherMaxHours.put("Sokolov", 30);
        teacherMaxHours.put("Mikhailov", 20);

        List<String> audiences = Arrays.asList("A1", "A2", "B1", "B2", "C1", "C2");
        int classesPerDay = 5;


        GeneticScheduler scheduler = new GeneticScheduler(subjects, teachers, groups, classesPerDay, teacherSubjects, audiences, teacherMaxHours, groupsSubjects);
        AbstractMap.SimpleEntry<List<MyClass>, Double> result = scheduler.solve(500, 50);
        List<MyClass> bestSchedule = result.getKey();
        double fitness = result.getValue();

        System.out.println("Best schedule:");
        for (MyClass lesson : bestSchedule) {
            System.out.println(lesson.subject + " - " + lesson.teacher + " - " + lesson.group + " - " + lesson.time + " - " + lesson.audience);
        }
        System.out.println("Rating: " + fitness);
    }
}
