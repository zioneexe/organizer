package bot.tg.util.validation;

@FunctionalInterface
public interface Rule<T> {

    Violation validate(T value);
}
