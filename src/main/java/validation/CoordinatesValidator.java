package validation;
import domain.Coordinates;
public class CoordinatesValidator {
    private static final double MAX_Y = 663;
    public static ValidationResult validate(Coordinates coordinates) {
        if (coordinates == null) {
            return ValidationResult.error("Coordinates не может быть null");
        }
        if (coordinates.getY() > MAX_Y) {
            return ValidationResult.error("Значение y не может быть больше " + MAX_Y);
        }
        return ValidationResult.ok();
    }
}