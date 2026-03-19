import cli.CommandInterpreter;

public class Main {
    public static void main(String[] args) {
        String filename = System.getenv("PERSON_DATA_FILE");

        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("\n ОШИБКА: Не задана переменная окружения PERSON_DATA_FILE");
            System.err.println("\nКак исправить:");
            System.err.println("  Windows: set PERSON_DATA_FILE=data.xml");
            System.err.println("  Linux/Mac: export PERSON_DATA_FILE=data.xml");
            System.err.println("  В IDE: добавить в конфигурацию запуска");
            System.exit(1);
        }

        System.out.println("\nФайл данных: " + filename);
        System.out.println("Тип коллекции: java.util.HashSet<Person>");
        System.out.println("\n" + "=".repeat(60));

        try {
            CommandInterpreter interpreter = new CommandInterpreter(filename);
            interpreter.run();

        } catch (Exception e) {
            System.err.println("\nКритическая ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\nПрограмма завершена.");
    }
}