package org.example;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class GeneticScheduler {
    private final List<String> subjects;
    private final List<String> teachers;
    private final List<String> groups;
    private final List<String> audiences;
    private final int classesPerDay;
    private final Map<String, List<String>> teacherSubjects;
    private final Map<String, Integer> teacherMaxHours;
    private final Map<String, List<String>> groupsSubjects;
    private static final Random random = new Random();

    public GeneticScheduler(List<String> subjects, List<String> teachers, List<String> groups, int classesPerDay, Map<String, List<String>> teacherSubjects, List<String> audiences, Map<String, Integer> teacherMaxHours, Map<String, List<String>> groupsSubjects) {
        this.subjects = subjects;
        this.teachers = teachers;
        this.groups = groups;
        this.classesPerDay = classesPerDay;
        this.teacherSubjects = teacherSubjects;
        this.audiences = audiences;
        this.teacherMaxHours = teacherMaxHours;
        this.groupsSubjects = groupsSubjects;
    }

    private Schedule createRandomSchedule() {
        List<MyClass> collect = subjects.stream().map(subject -> {
            MyClass c = new MyClass();
            c.subject = subject;
            c.teacher = teachers.get(random.nextInt(teachers.size()));
            c.group = groups.get(random.nextInt(groups.size()));
            c.time = random.nextInt(classesPerDay) + 1;
            c.audience = audiences.get(random.nextInt(audiences.size()));
            return c;
        }).collect(Collectors.toList());
        Schedule scedule = new Schedule(collect, calculateFitness(collect, teacherSubjects, teacherMaxHours, groupsSubjects));
        return scedule;
    }

    private List<Schedule> generateRandomPopulation(int populationSize) {
        return IntStream.range(0, populationSize).mapToObj(i -> createRandomSchedule()).collect(Collectors.toList());
    }

    private static double calculateFitness(List<MyClass> schedule, Map<String, List<String>> teacherSubjects, Map<String, Integer> teacherMaxHours, Map<String, List<String>> groupsSubjects) {
        long conflicts = calculateTimeConflicts(schedule)
                + calculateTeacherSubjectConflicts(schedule, teacherSubjects)
                + calculateGroupSubjectConflicts(schedule, groupsSubjects)
                + calculateTeachingHoursConflicts(schedule, teacherMaxHours);

        return 1.0 / (1.0 + conflicts);
    }
    private static long calculateTimeConflicts(List<MyClass> schedule) {
        return schedule.stream()
                .flatMap(c1 -> schedule.stream()
                        .filter(c2 -> !c1.equals(c2) && c1.time == c2.time)
                        .filter(c2 -> c1.group.equals(c2.group) || c1.teacher.equals(c2.teacher) || c1.audience.equals(c2.audience)))
                .count();
    }
    private static long calculateTeacherSubjectConflicts(List<MyClass> schedule, Map<String, List<String>> teacherSubjects) {
        return schedule.stream()
                .filter(c -> !teacherSubjects.getOrDefault(c.teacher, Collections.emptyList()).contains(c.subject))
                .count();
    }
    private static long calculateGroupSubjectConflicts(List<MyClass> schedule, Map<String, List<String>> groupsSubjects) {
        return schedule.stream()
                .filter(c -> !groupsSubjects.getOrDefault(c.group, Collections.emptyList()).contains(c.subject))
                .count();
    }
    private static long calculateTeachingHoursConflicts(List<MyClass> schedule, Map<String, Integer> teacherMaxHours) {
        Map<String, Long> teachingHours = schedule.stream()
                .collect(Collectors.groupingBy(MyClass::getTeacher, Collectors.summingLong(MyClass::getTime)));

        return teachingHours.entrySet().stream()
                .filter(entry -> teacherMaxHours.containsKey(entry.getKey()) && entry.getValue() > teacherMaxHours.get(entry.getKey()))
                .count();
    }


    private Schedule mutate(Schedule schedule) {
        List<MyClass> myClasses = schedule.myClasses.stream()
                .map(c -> random.nextDouble() < 0.1 ? createRandomSchedule().myClasses.get(0) : c).collect(Collectors.toList());
        return new Schedule(myClasses,calculateFitness(myClasses, teacherSubjects, teacherMaxHours, groupsSubjects));
    }

    private AbstractMap.SimpleEntry<Schedule, Schedule> crossover(List<MyClass> schedule1, List<MyClass> schedule2) {
        int crossoverPoint1 = random.nextInt(subjects.size() - 1);
        int crossoverPoint2 = random.nextInt(subjects.size() - crossoverPoint1 - 1) + crossoverPoint1 + 1;

        List<MyClass> child1 = new ArrayList<>(schedule1.subList(0, crossoverPoint1));
        child1.addAll(schedule2.subList(crossoverPoint1, crossoverPoint2));
        child1.addAll(schedule1.subList(crossoverPoint2, subjects.size()));
        Schedule childSchedule1 = new Schedule(child1,calculateFitness(child1, teacherSubjects, teacherMaxHours, groupsSubjects));

        List<MyClass> child2 = new ArrayList<>(schedule2.subList(0, crossoverPoint1));
        child2.addAll(schedule1.subList(crossoverPoint1, crossoverPoint2));
        child2.addAll(schedule2.subList(crossoverPoint2, subjects.size()));
        Schedule childSchedule2 = new Schedule(child2,calculateFitness(child2, teacherSubjects, teacherMaxHours, groupsSubjects));

        return new AbstractMap.SimpleEntry<>(childSchedule1, childSchedule2);
    }

    private record Schedule(List<MyClass> myClasses, double fitnessScore){}

    public AbstractMap.SimpleEntry<List<MyClass>, Double> solve(int populationSize, int generations) {
        List<Schedule> population = generateRandomPopulation(populationSize);
        double bestFitnessScore = 0;
        List<MyClass> bestSchedule = null;

        double crossoverProbability = 0.8;
        double mutationProbability = 0.1;
        double mutationGeneralProbability = 0.8;

        for (int generation = 0; generation < generations; generation++) {
            Collections.sort(population, new Comparator<Schedule>() {
                @Override
                public int compare(Schedule o1, Schedule o2) {
                    return -Double.compare(o1.fitnessScore, o2.fitnessScore);
                }
            });

            bestSchedule = population.get(0).myClasses;
            bestFitnessScore = population.get(0).fitnessScore;

            int quarterSize = population.size() / 4; // Calculate the first quarter size
            List<Schedule> firstQuarterList = new ArrayList<>(population.subList(0, quarterSize)); // Get the first quarter of the list

            List<Schedule> newPopulation = new ArrayList<>();
            newPopulation.addAll(firstQuarterList); // Add the elements of the first quarter back to the original list


            System.out.println("Generation " + (generation + 1) + ": Best rating = " + bestFitnessScore);
            System.out.println();

            while (newPopulation.size() < populationSize) {
                Schedule parent1 = population.get(random.nextInt(population.size()));
                Schedule parent2 = population.get(random.nextInt(population.size()));

                if (random.nextDouble() < crossoverProbability) {
                    AbstractMap.SimpleEntry<Schedule, Schedule> children = crossover(parent1.myClasses, parent2.myClasses);
                    newPopulation.add(children.getKey());
                    newPopulation.add(children.getValue());
                }

                if (random.nextDouble() < mutationGeneralProbability) {
                    for (int i = newPopulation.size() - 2; i < newPopulation.size(); i++) {
                        if (random.nextDouble() < mutationProbability) {
                            newPopulation.set(i, mutate(newPopulation.get(i)));
                        }
                    }
                }
            }

            population = new ArrayList<>(newPopulation.subList(0, populationSize));
        }

        return new AbstractMap.SimpleEntry<>(bestSchedule, bestFitnessScore);
    }
}