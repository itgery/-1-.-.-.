package validation;
import domain.Person;
import domain.Coordinates;
import java.util.Date;
public class PersonValidator {
    public static ValidationResult validate(Person person) {
        if (person == null) {
            return ValidationResult.error("Person не может быть null");
        }
        if (person.getId() <= 0) {
            return ValidationResult.error("id должен быть больше 0");
        }
        if (person.getName() == null) {
            return ValidationResult.error("name не может быть null");
        }
        if (person.getName().trim().isEmpty()) {
            return ValidationResult.error("name не может быть пустой строкой");
        }
        if (person.getCoordinates() == null) {
            return ValidationResult.error("coordinates не может быть null");
        }
        ValidationResult coordinatesResult = CoordinatesValidator.validate(person.getCoordinates());
        if (!coordinatesResult.isValid()) {
            return coordinatesResult;
        }
        if (person.getCreationDate() == null) {
            return ValidationResult.error("creationDate не может быть null");
        }
        if (person.getHeight() <= 0) {
            return ValidationResult.error("height должен быть больше 0");
        }
        if (person.getLocation() != null) {
            ValidationResult locationResult = LocationValidator.validate(person.getLocation());
            if (!locationResult.isValid()) {
                return locationResult;
            }
        }
        return ValidationResult.ok();
    }
}