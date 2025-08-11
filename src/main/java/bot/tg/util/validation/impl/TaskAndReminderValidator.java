package bot.tg.util.validation.impl;

import bot.tg.util.validation.Rules;
import bot.tg.util.validation.Validator;
import bot.tg.util.validation.Violation;

import java.util.List;

import static bot.tg.constant.Validation.*;

public class TaskAndReminderValidator extends Validator<String> {

    public List<Violation> validateTitle(String text) {
        return validate(text, List.of(
                Rules.required(TITLE),
                Rules.maxLength(TITLE_ERROR_MAX_LENGTH, TITLE_ERROR)
        ));
    }

    public List<Violation> validateDescription(String text) {
        return validate(text, List.of(
                Rules.required(DESCRIPTION),
                Rules.maxLength(DESCRIPTION_MAX_LENGTH, DESCRIPTION_ERROR)
        ));
    }
}
