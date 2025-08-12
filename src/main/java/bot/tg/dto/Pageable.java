package bot.tg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Pageable {

    public static int FIRST = 1;

    private int page;

    private int pageSize;

    private int totalPages;

    public static Pageable of(int pageNumber, int pageSize, int total) {
        return new Pageable(pageNumber, pageSize, total);
    }

}
