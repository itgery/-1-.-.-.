package storage;
import domain.*;
import domain.Location;
import service.CollectionManager;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.xml.stream.*;

public class FileManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String INDENT = "    ";


    public static void loadFromFile(String filename, CollectionManager manager) {
        File file = new File(filename);


        if (!checkFileAccess(file, false)) return;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            HashSet<Person> collection = new HashSet<>();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bis);

            ParsingContext ctx = new ParsingContext();

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        ctx.startElement(reader.getLocalName());
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        String text = reader.getText().trim();
                        if (!text.isEmpty()) {
                            ctx.setText(text);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        Person completedPerson = ctx.endElement(reader.getLocalName());
                        if (completedPerson != null) {
                            collection.add(completedPerson);
                        }
                        break;
                }
            }

            reader.close();
            manager.setCollection(collection);
            System.out.println("Загружено " + collection.size() + " элементов из файла.");

        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveToFile(String filename, CollectionManager manager) {
        File file = new File(filename);

        try {
            if (!checkFileAccess(file, true)) return;

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                XMLStreamWriter writer = XMLOutputFactory.newInstance()
                        .createXMLStreamWriter(bos, "UTF-8");

                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeCharacters("\n");
                writer.writeStartElement("persons");
                writer.writeCharacters("\n");

                for (Person person : manager.getAll()) {
                    writePerson(writer, person);
                }

                writer.writeEndElement();
                writer.writeEndDocument();
                writer.flush();
                writer.close();

                System.out.println("Коллекция сохранена в файл " + filename);
            }

        } catch (Exception e) {
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean checkFileAccess(File file, boolean needWrite) {
        if (!file.exists()) {
            if (!needWrite) {
                System.out.println("Файл не существует. Будет создана пустая коллекция.");
                return false;
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Не удалось создать файл: " + e.getMessage());
                return false;
            }
        }

        if (needWrite && !file.canWrite()) {
            System.out.println("Нет прав на запись в файл.");
            return false;
        }

        if (!needWrite && !file.canRead()) {
            System.out.println("Нет прав на чтение файла. Будет создана пустая коллекция.");
            return false;
        }

        return true;
    }


    private static void writePerson(XMLStreamWriter writer, Person person) throws XMLStreamException {
        writer.writeCharacters(INDENT);
        writer.writeStartElement("person");
        writer.writeCharacters("\n");

        writeElement(writer, "id", String.valueOf(person.getId()), 2);
        writeElement(writer, "name", person.getName(), 2);

        // Coordinates
        writeCoordinates(writer, person.getCoordinates());

        writeElement(writer, "creationDate", String.valueOf(person.getCreationDate().getTime()), 2);
        writeElement(writer, "height", String.valueOf(person.getHeight()), 2);

        // Опциональные поля
        writeOptionalElement(writer, "birthday",
                person.getBirthday() != null ? person.getBirthday().format(DATE_FORMATTER) : "", 2);
        writeOptionalElement(writer, "hairColor",
                person.getHairColor() != null ? person.getHairColor().name() : "", 2);
        writeOptionalElement(writer, "nationality",
                person.getNationality() != null ? person.getNationality().name() : "", 2);

        // Location
        if (person.getLocation() != null) {
            writeLocation(writer, person.getLocation());
        }

        writer.writeCharacters(INDENT);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private static void writeCoordinates(XMLStreamWriter writer, Coordinates coords) throws XMLStreamException {
        writer.writeCharacters(INDENT.repeat(2));
        writer.writeStartElement("coordinates");
        writer.writeCharacters("\n");

        writeElement(writer, "x", String.valueOf(coords.getX()), 3);
        writeElement(writer, "y", String.valueOf(coords.getY()), 3);

        writer.writeCharacters(INDENT.repeat(2));
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private static void writeLocation(XMLStreamWriter writer, Location loc) throws XMLStreamException {
        writer.writeCharacters(INDENT.repeat(2));
        writer.writeStartElement("location");
        writer.writeCharacters("\n");

        writeElement(writer, "x", String.valueOf(loc.getX()), 3);
        writeElement(writer, "y", String.valueOf(loc.getY()), 3);
        writeElement(writer, "z", String.valueOf(loc.getZ()), 3);

        writer.writeCharacters(INDENT.repeat(2));
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private static void writeElement(XMLStreamWriter writer, String name, String value, int indent)
            throws XMLStreamException {
        writer.writeCharacters(INDENT.repeat(indent));
        writer.writeStartElement(name);
        writer.writeCharacters(value);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private static void writeOptionalElement(XMLStreamWriter writer, String name, String value, int indent)
            throws XMLStreamException {
        writer.writeCharacters(INDENT.repeat(indent));
        writer.writeStartElement(name);
        writer.writeCharacters(value);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private static class ParsingContext {
        private Person currentPerson;
        private Coordinates currentCoordinates;
        private Location currentLocation;
        private String currentElement;
        private StringBuilder currentText = new StringBuilder();

        public void startElement(String elementName) {
            currentElement = elementName;
            currentText.setLength(0);

            switch (elementName) {
                case "person":
                    currentPerson = new Person(0, null, null, null, 0, null, null, null, null);
                    currentCoordinates = null;
                    currentLocation = null;
                    break;
                case "coordinates":
                    currentCoordinates = new Coordinates(0, 0);
                    break;
                case "location":
                    currentLocation = new Location(0, 0.0, 0);
                    break;
            }
        }

        public void setText(String text) {
            if (currentPerson != null) {
                setPersonField(text);
            } else if (currentCoordinates != null && currentLocation == null) {
                setCoordinatesField(text);
            } else if (currentLocation != null) {
                setLocationField(text);
            }
        }

        private void setPersonField(String text) {
            switch (currentElement) {
                case "id": currentPerson.setId(Integer.parseInt(text)); break;
                case "name": currentPerson.setName(text); break;
                case "creationDate": currentPerson.setCreationDate(new Date(Long.parseLong(text))); break;
                case "height": currentPerson.setHeight(Float.parseFloat(text)); break;
                case "birthday":
                    if (!text.isEmpty()) {
                        currentPerson.setBirthday(LocalDateTime.parse(text, DATE_FORMATTER));
                    }
                    break;
                case "hairColor":
                    if (!text.isEmpty()) {
                        currentPerson.setHairColor(Color.valueOf(text));
                    }
                    break;
                case "nationality":
                    if (!text.isEmpty()) {
                        currentPerson.setNationality(Country.valueOf(text));
                    }
                    break;
            }
        }

        private void setCoordinatesField(String text) {
            switch (currentElement) {
                case "x": currentCoordinates.setX(Long.parseLong(text)); break;
                case "y": currentCoordinates.setY(Double.parseDouble(text)); break;
            }
        }

        private void setLocationField(String text) {
            switch (currentElement) {
                case "x": currentLocation.setX(Double.parseDouble(text)); break;
                case "y": currentLocation.setY(Double.parseDouble(text)); break;
                case "z": currentLocation.setZ(Integer.parseInt(text)); break;
            }
        }

        public Person endElement(String elementName) {
            switch (elementName) {
                case "coordinates":
                    if (currentPerson != null) {
                        currentPerson.setCoordinates(currentCoordinates);
                    }
                    break;
                case "location":
                    if (currentPerson != null) {
                        currentPerson.setLocation(currentLocation);
                    }
                    break;
                case "person":
                    Person completed = currentPerson;
                    currentPerson = null;
                    return completed;
            }
            return null;
        }
    }
}