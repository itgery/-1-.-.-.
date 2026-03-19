package cli;

import domain.*;
import service.CollectionManager;
import storage.FileManager;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
public class CommandInterpreter {
    private final CollectionManager manager;
    private final Scanner scanner;
    private final String filename;
    private boolean running;
    public CommandInterpreter(String filename) {
        this.manager = new CollectionManager();
        this.scanner = new Scanner(System.in);
        this.filename = filename;
        this.running = true;

        FileManager.loadFromFile(filename, manager);
    }
    public void run() {
        System.out.println("Программа управления коллекцией Person запущена.");
        System.out.println("Введите 'help' для списка команд.");

        while (running) {
            try {
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String argument = parts.length > 1 ? parts[1] : "";

                executeCommand(command, argument);

            } catch (Exception e) {
                System.out.println("Ошибка при выполнении команды: " + e.getMessage());
            }
        }

        scanner.close();
    }
    private void executeCommand(String command, String argument) {
        switch (command) {
            case "help":
                printHelp();
                break;

            case "info":
                printInfo();
                break;

            case "show":
                showAll();
                break;

            case "add":
                addPerson();
                break;

            case "update":
                updatePerson(argument);
                break;

            case "remove_by_id":
                removeById(argument);
                break;

            case "clear":
                clear();
                break;

            case "save":
                save();
                break;

            case "execute_script":
                executeScript(argument);
                break;

            case "exit":
                exit();
                break;

            case "add_if_min":
                addIfMin();
                break;

            case "remove_greater":
                removeGreater();
                break;

            case "remove_lower":
                removeLower();
                break;

            case "filter_starts_with_name":
                filterStartsWithName(argument);
                break;

            case "print_ascending":
                printAscending();
                break;

            case "print_descending":
                printDescending();
                break;

            default:
                System.out.println("Неизвестная команда. Введите 'help' для списка команд.");
        }
    }
    private void printHelp() {
        System.out.println("\nДоступные команды:");
        System.out.println("  help - вывести справку по доступным командам");
        System.out.println("  info - вывести информацию о коллекции");
        System.out.println("  show - вывести все элементы коллекции");
        System.out.println("  add - добавить новый элемент в коллекцию");
        System.out.println("  update id - обновить значение элемента коллекции");
        System.out.println("  remove_by_id id - удалить элемент по id");
        System.out.println("  clear - очистить коллекцию");
        System.out.println("  save - сохранить коллекцию в файл");
        System.out.println("  execute_script file_name - выполнить скрипт из файла");
        System.out.println("  exit - завершить программу");
        System.out.println("  add_if_min - добавить элемент, если он меньше минимального");
        System.out.println("  remove_greater - удалить все элементы, превышающие заданный");
        System.out.println("  remove_lower - удалить все элементы, меньшие заданного");
        System.out.println("  filter_starts_with_name name - вывести элементы, имя которых начинается с подстроки");
        System.out.println("  print_ascending - вывести элементы в порядке возрастания");
        System.out.println("  print_descending - вывести элементы в порядке убывания");
    }
    private void printInfo() {
        System.out.println(manager.getInfo());
    }
    private void showAll() {
        if (manager.getAll().isEmpty()) {
            System.out.println("Коллекция пуста.");
            return;
        }

        for (Person person : manager.getAll()) {
            System.out.println(person);
        }
    }
    private void addPerson() {
        System.out.println("\n=== Добавление нового Person ===");

        try {
            String name = readString("Введите имя (не может быть пустым): ", false);

            System.out.println("\n--- Ввод координат ---");
            long x = readLong("Введите x (целое число): ");
            double y = readDouble("Введите y (вещественное число, не более 663): ");
            Coordinates coordinates = new Coordinates(x, y);

            float height = readFloat("Введите рост (число > 0): ", true);

            System.out.println("\n--- Ввод даты рождения (можно пропустить) ---");
            System.out.println("Формат: yyyy-MM-ddTHH:mm:ss (например, 1990-05-15T14:30:00)");
            LocalDateTime birthday = readLocalDateTime("Введите дату рождения или оставьте пустым: ", true);

            System.out.println("\n--- Ввод цвета волос ---");
            System.out.println("Доступные цвета: " + Color.getAvailableColors());
            Color hairColor = readEnum("Введите цвет волос или оставьте пустым: ", Color.class, true);

            System.out.println("\n--- Ввод национальности ---");
            System.out.println("Доступные страны: " + Country.getAvailableCountries());
            Country nationality = readEnum("Введите национальность или оставьте пустым: ", Country.class, true);

            Location location = readLocation();

            Person person = manager.createAndAdd(name, coordinates, height,
                    birthday, hairColor, nationality, location);

            if (person != null) {
                System.out.println("\n✓ Person успешно добавлен с id: " + person.getId());
            }

        } catch (Exception e) {
            System.out.println("\n✗ Ошибка при создании Person: " + e.getMessage());
        }
    }
    private void updatePerson(String idStr) {
        if (idStr.isEmpty()) {
            System.out.println("Ошибка: не указан id. Используйте: update id");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Person existingPerson = manager.getById(id);

            if (existingPerson == null) {
                System.out.println("Ошибка: Person с id " + id + " не найден.");
                return;
            }

            System.out.println("\n=== Обновление Person с id " + id + " ===");
            System.out.println("Текущие данные: " + existingPerson);
            System.out.println("Введите новые данные (оставьте пустым для сохранения текущего значения):\n");

            String name = readString("Имя [" + existingPerson.getName() + "]: ", true);
            if (name.isEmpty()) {
                name = existingPerson.getName();
            }

            Coordinates coordinates = existingPerson.getCoordinates();
            System.out.println("\nТекущие координаты: x=" + coordinates.getX() + ", y=" + coordinates.getY());
            if (askYesNo("Изменить координаты?")) {
                long x = readLong("Введите x: ");
                double y = readDouble("Введите y: ");
                coordinates = new Coordinates(x, y);
            }

            String heightStr = readString("Рост [" + existingPerson.getHeight() + "]: ", true);
            float height = heightStr.isEmpty() ? existingPerson.getHeight() : Float.parseFloat(heightStr);

            LocalDateTime birthday = existingPerson.getBirthday();
            System.out.println("\nТекущая дата рождения: " + (birthday == null ? "не указана" : birthday));
            if (askYesNo("Изменить дату рождения?")) {
                birthday = readLocalDateTime("Введите новую дату рождения: ", true);
            }

            Color hairColor = existingPerson.getHairColor();
            System.out.println("\nТекущий цвет волос: " + (hairColor == null ? "не указан" : hairColor));
            if (askYesNo("Изменить цвет волос?")) {
                System.out.println("Доступные цвета: " + Color.getAvailableColors());
                hairColor = readEnum("Введите новый цвет волос: ", Color.class, true);
            }

            Country nationality = existingPerson.getNationality();
            System.out.println("\nТекущая национальность: " + (nationality == null ? "не указана" : nationality));
            if (askYesNo("Изменить национальность?")) {
                System.out.println("Доступные страны: " + Country.getAvailableCountries());
                nationality = readEnum("Введите новую национальность: ", Country.class, true);
            }

            Location location = existingPerson.getLocation();
            System.out.println("\nТекущее местоположение: " + (location == null ? "не указано" : location));
            if (askYesNo("Изменить местоположение?")) {
                location = readLocation();
            }

            Person updatedPerson = new Person(id, name, coordinates, existingPerson.getCreationDate(),
                    height, birthday, hairColor, nationality, location);

            if (manager.update(id, updatedPerson)) {
                System.out.println("\n✓ Person успешно обновлен.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: id должен быть числом.");
        }
    }
    private void removeById(String idStr) {
        if (idStr.isEmpty()) {
            System.out.println("Ошибка: не указан id. Используйте: remove_by_id id");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            if (manager.remove(id)) {
                System.out.println("✓ Элемент с id " + id + " удален.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: id должен быть числом.");
        }
    }
    private void clear() {
        if (askYesNo("Вы уверены, что хотите очистить всю коллекцию?")) {
            manager.clear();
            System.out.println("✓ Коллекция очищена.");
        } else {
            System.out.println("Операция отменена.");
        }
    }
    private void save() {
        FileManager.saveToFile(filename, manager);
    }
    private void executeScript(String scriptFileName) {
        if (scriptFileName.isEmpty()) {
            System.out.println("Ошибка: не указано имя файла. Используйте: execute_script file_name");
            return;
        }

        File scriptFile = new File(scriptFileName);

        if (!scriptFile.exists()) {
            System.out.println("Ошибка: файл скрипта не существует.");
            return;
        }

        if (!scriptFile.canRead()) {
            System.out.println("Ошибка: нет прав на чтение файла скрипта.");
            return;
        }

        System.out.println("\n=== Выполнение скрипта: " + scriptFileName + " ===");

        try (Scanner scriptScanner = new Scanner(scriptFile)) {
            int lineNumber = 0;

            while (scriptScanner.hasNextLine()) {
                lineNumber++;
                String commandLine = scriptScanner.nextLine().trim();

                if (commandLine.isEmpty() || commandLine.startsWith("#")) {
                    continue;
                }

                System.out.println("\n[" + lineNumber + "] > " + commandLine);

                String[] parts = commandLine.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String argument = parts.length > 1 ? parts[1] : "";

                if (command.equals("add") || command.equals("update") ||
                        command.equals("add_if_min") || command.equals("remove_greater") ||
                        command.equals("remove_lower")) {
                    System.out.println("✗ Команда '" + command + "' не поддерживается в скриптах (требует интерактивного ввода)");
                    continue;
                }

                executeCommand(command, argument);
            }

            System.out.println("\n=== Скрипт выполнен ===");

        } catch (FileNotFoundException e) {
            System.out.println("Ошибка при чтении файла скрипта: " + e.getMessage());
        }
    }
    private void exit() {
        System.out.println("Завершение программы...");
        running = false;
    }
    private void addIfMin() {
        System.out.println("\n=== Добавление Person (только если рост меньше минимального) ===");

        // Создаем временного Person для сравнения
        Person tempPerson = createTempPersonForComparison();
        if (tempPerson == null) return;

        if (manager.addIfMin(tempPerson)) {
            System.out.println("✓ Элемент добавлен, так как его рост меньше минимального.");
        } else {
            System.out.println("Элемент не добавлен: его рост не меньше минимального в коллекции.");
        }
    }
    private void removeGreater() {
        System.out.println("\n=== Удаление элементов с ростом больше заданного ===");

        Person tempPerson = createTempPersonForComparison();
        if (tempPerson == null) return;

        int beforeCount = manager.getAll().size();
        boolean removed = manager.removeGreater(tempPerson);
        int afterCount = manager.getAll().size();

        if (removed) {
            System.out.println("✓ Удалено элементов: " + (beforeCount - afterCount));
        } else {
            System.out.println("Элементы не удалены (нет элементов с большим ростом).");
        }
    }
    private void removeLower() {
        System.out.println("\n=== Удаление элементов с ростом меньше заданного ===");

        Person tempPerson = createTempPersonForComparison();
        if (tempPerson == null) return;

        int beforeCount = manager.getAll().size();
        boolean removed = manager.removeLower(tempPerson);
        int afterCount = manager.getAll().size();

        if (removed) {
            System.out.println("✓ Удалено элементов: " + (beforeCount - afterCount));
        } else {
            System.out.println("Элементы не удалены (нет элементов с меньшим ростом).");
        }
    }
    private Person createTempPersonForComparison() {
        try {
            System.out.println("Введите данные для сравнения:");

            String name = readString("Введите имя (для справки): ", false);
            long x = readLong("Введите x: ");
            double y = readDouble("Введите y: ");
            Coordinates coordinates = new Coordinates(x, y);
            float height = readFloat("Введите рост для сравнения: ", true);

            return new Person(0, name, coordinates, new java.util.Date(),
                    height, null, null, null, null);

        } catch (Exception e) {
            System.out.println("Ошибка при создании объекта для сравнения: " + e.getMessage());
            return null;
        }
    }
    private void filterStartsWithName(String prefix) {
        if (prefix.isEmpty()) {
            System.out.println("Ошибка: не указано имя. Используйте: filter_starts_with_name имя");
            return;
        }

        List<Person> filtered = manager.filterStartsWithName(prefix);

        if (filtered.isEmpty()) {
            System.out.println("Элементы, имя которых начинается с '" + prefix + "', не найдены.");
        } else {
            System.out.println("Найдено элементов: " + filtered.size());
            for (Person person : filtered) {
                System.out.println(person);
            }
        }
    }
    private void printAscending() {
        List<Person> sorted = manager.getAscending();

        if (sorted.isEmpty()) {
            System.out.println("Коллекция пуста.");
        } else {
            System.out.println("Элементы в порядке возрастания (по росту):");
            for (Person person : sorted) {
                System.out.println("  " + person.getId() + ": " + person.getName() +
                        " (рост: " + person.getHeight() + ")");
            }
        }
    }
    private void printDescending() {
        List<Person> sorted = manager.getDescending();

        if (sorted.isEmpty()) {
            System.out.println("Коллекция пуста.");
        } else {
            System.out.println("Элементы в порядке убывания (по росту):");
            for (Person person : sorted) {
                System.out.println("  " + person.getId() + ": " + person.getName() +
                        " (рост: " + person.getHeight() + ")");
            }
        }
    }
    private String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!allowEmpty && input.isEmpty()) {
                System.out.println("✗ Ошибка: значение не может быть пустым. Повторите ввод.");
                continue;
            }

            return input;
        }
    }
    private long readLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                return Long.parseLong(input);

            } catch (NumberFormatException e) {
                System.out.println("✗ Ошибка: введите целое число.");
            }
        }
    }
    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                double value = Double.parseDouble(input);

                // Проверка ограничения для y
                if (prompt.contains("y") && value > 663) {
                    System.out.println("✗ Ошибка: y не может быть больше 663. Повторите ввод.");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("✗ Ошибка: введите вещественное число (используйте точку как разделитель).");
            }
        }
    }
    private float readFloat(String prompt, boolean positive) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                float value = Float.parseFloat(input);

                if (positive && value <= 0) {
                    System.out.println("✗ Ошибка: значение должно быть больше 0.");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("✗ Ошибка: введите число.");
            }
        }
    }
    private LocalDateTime readLocalDateTime(String prompt, boolean allowEmpty) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty() && allowEmpty) {
                    return null;
                }

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                return LocalDateTime.parse(input);

            } catch (DateTimeParseException e) {
                System.out.println("✗ Ошибка: неверный формат даты. Используйте формат yyyy-MM-ddTHH:mm:ss");
            }
        }
    }
    private <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass, boolean allowEmpty) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim().toUpperCase();

                if (input.isEmpty() && allowEmpty) {
                    return null;
                }

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                return Enum.valueOf(enumClass, input);

            } catch (IllegalArgumentException e) {
                System.out.println("✗ Ошибка: введите одно из допустимых значений.");
            }
        }
    }
    private Location readLocation() {
        System.out.println("\n--- Ввод местоположения (можно пропустить) ---");

        if (!askYesNo("Хотите ввести местоположение?")) {
            return null;
        }

        try {
            double x = readDouble("Введите x: ");
            Double y = readDoubleObject("Введите y (не может быть null): ", false);
            Integer z = readInteger("Введите z (не может быть null): ", false);

            return new Location(x, y, z);

        } catch (Exception e) {
            System.out.println("Ошибка при вводе местоположения: " + e.getMessage());
            return null;
        }
    }
    private Double readDoubleObject(String prompt, boolean allowNull) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty() && allowNull) {
                    return null;
                }

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                return Double.parseDouble(input);

            } catch (NumberFormatException e) {
                System.out.println("✗ Ошибка: введите вещественное число.");
            }
        }
    }
    private Integer readInteger(String prompt, boolean allowNull) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty() && allowNull) {
                    return null;
                }

                if (input.isEmpty()) {
                    System.out.println("✗ Ошибка: значение не может быть пустым.");
                    continue;
                }

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {
                System.out.println("✗ Ошибка: введите целое число.");
            }
        }
    }
    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }

            System.out.println("Пожалуйста, введите y или n.");
        }
    }
}