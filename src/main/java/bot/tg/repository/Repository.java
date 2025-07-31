package bot.tg.repository;

import java.util.List;

public interface Repository<T, UpdateDto, I> {

    String create(T dto);

    T getById(I id);

    boolean existsById(I id);

    List<T> getAll();

    T update(I id, UpdateDto dto);

    boolean deleteById(I id);
}
