package service;
import domain.*;
import validation.PersonValidator;
import validation.ValidationResult;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
public class CollectionManager {
    private HashSet<Person> collection;
    private Date initializationDate;
    private int currentId = 1;
    public CollectionManager() {
        this.collection = new HashSet<>();
        this.initializationDate = new Date();
    }
    private int generateId() {
        return currentId++;
    }
    public boolean add(Person person) {
        ValidationResult result = PersonValidator.validate(person);
        if (!result.isValid()) {
            System.out.println("Ошибка валидации: " + result.getMessage());
            return false;
        }
        return collection.add(person);
    }
    public Person createAndAdd(String name, Coordinates coordinates, float height,
                               LocalDateTime birthday, Color hairColor,
                               Country nationality, Location location) {
        int id = generateId();
        Date creationDate = new Date();

        Person person = new Person(id, name, coordinates, creationDate,
                height, birthday, hairColor, nationality, location);

        ValidationResult result = PersonValidator.validate(person);
        if (!result.isValid()) {
            System.out.println("Ошибка валидации: " + result.getMessage());
            return null;
        }
        collection.add(person);
        return person;
    }
    public Person getById(int id) {
        return collection.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }
    public HashSet<Person> getAll() {
        return new HashSet<>(collection);
    }
    public boolean update(int id, Person updatedPerson) {
        Person existingPerson = getById(id);
        if (existingPerson == null) {
            System.out.println("Элемент с id " + id + " не найден");
            return false;
        }
        updatedPerson.setId(id);
        updatedPerson.setCreationDate(existingPerson.getCreationDate());
        ValidationResult result = PersonValidator.validate(updatedPerson);
        if (!result.isValid()) {
            System.out.println("Ошибка валидации: " + result.getMessage());
            return false;
        }
        collection.remove(existingPerson);
        collection.add(updatedPerson);
        return true;
    }
    public boolean remove(int id) {
        Person person = getById(id);
        if (person == null) {
            System.out.println("Элемент с id " + id + " не найден");
            return false;
        }
        return collection.remove(person);
    }
    public void clear() {
        collection.clear();
    }
    public String getInfo() {
        return "Тип коллекции: " + collection.getClass().getName() + "\n" +
                "Дата инициализации: " + initializationDate + "\n" +
                "Количество элементов: " + collection.size();
    }
    public boolean addIfMin(Person person) {
        if (collection.isEmpty()) {
            return add(person);
        }
        Person minPerson = Collections.min(collection);
        if (person.compareTo(minPerson) < 0) {
            return add(person);
        }
        return false;
    }
    public boolean removeGreater(Person person) {
        return collection.removeIf(p -> p.compareTo(person) > 0);
    }
    public boolean removeLower(Person person) {
        return collection.removeIf(p -> p.compareTo(person) < 0);
    }
    public List<Person> filterStartsWithName(String prefix) {
        return collection.stream()
                .filter(p -> p.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }
    public List<Person> getAscending() {
        return collection.stream()
                .sorted()
                .collect(Collectors.toList());
    }
    public List<Person> getDescending() {
        return collection.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }
    public void setCollection(HashSet<Person> collection) {
        this.collection = collection;
        this.currentId = collection.stream()
                .mapToInt(Person::getId)
                .max()
                .orElse(0) + 1;
    }
}