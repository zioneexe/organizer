package bot.tg.validation;

@FunctionalInterface
public interface Rule<T> {

    Violation validate(T value);
}
