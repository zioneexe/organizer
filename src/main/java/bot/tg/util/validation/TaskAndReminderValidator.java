package bot.tg.util.validation;

import java.util.List;

public class TaskAndReminderValidator extends Validator<String> {

    public List<Violation> validateTitle(String text) {
        return validate(text, List.of(
                Rules.required("Назва"),
                Rules.maxLength(40, "Назва занадто довга. 🙈 Скороти до 40 символів.")
        ));
    }

    public List<Violation> validateDescription(String text) {
        return validate(text, List.of(
                Rules.required("Опис"),
                Rules.maxLength(512, "Опис занадто довгий. 🙈 Максимум 512 символів.")
        ));
    }
}
