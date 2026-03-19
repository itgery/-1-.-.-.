package validation;
import domain.Location;
public class LocationValidator {
    public static ValidationResult validate(Location location) {
        if (location == null) {
            return ValidationResult.ok();
        }

        if (location.getY() == null) {
            return ValidationResult.error("y не может быть null");
        }

        if (location.getZ() == null) {
            return ValidationResult.error("z не может быть null");
        }

        return ValidationResult.ok();
    }
}