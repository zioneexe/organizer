package bot.tg.validation;

import java.util.ArrayList;
import java.util.List;

public abstract class Validator<T> {

    public List<Violation> validate(T text, List<Rule<T>> rules) {
        List<Violation> violations = new ArrayList<>();
        for (Rule<T> rule : rules) {
            Violation violation = rule.validate(text);
            if (violation == null) continue;
            violations.add(violation);
        }

        return violations;
    }
}
