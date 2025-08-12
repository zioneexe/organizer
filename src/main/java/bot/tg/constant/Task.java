package bot.tg.constant;

public final class Task {

    private Task() {}

    public static final class Response {

        private Response() {}

        public static final String TASK_CREATE = "Нове завдання";
        public static final String TASK_TITLE = "Яка назва завдання? ✨";
        public static final String TASK_DESCRIPTION = "Який опис завдання? \uD83D\uDC40";
        public static final String TASK_CREATED = "Вітаю, завдання створено!";
        public static final String TASK_COMPLETED = "Завдання виконано. Вітаю!";
        public static final String TASK_IN_PROGRESS = "Що ж.. Працюй далі :)";
        public static final String TASK_DELETED = "🗑 Завдання видалено.";
        public static final String TASK_NOT_FOUND = "⚠️ Завдання не знайдено.";
        public static final String TASK_NO_DESCRIPTION = "Без опису";
        public static final String TASK_NO_DESCRIPTION_RESPONSE = "Окей, без опису 👍";
        public static final String TASK_EDIT_QUESTION = "Що хочеш змінити?";
        public static final String TASK_EDIT_ENTER_NAME = "Введи нову назву.";
        public static final String TASK_EDIT_ENTER_DESCRIPTION = "Введи новий опис.";

        public static final String TASK_EDIT_TITLE_LABEL = "Назву";
        public static final String TASK_EDIT_DESCRIPTION_LABEL = "Опис";
        public static final String TASK_EDIT_DELETE_LABEL = "Видалити";
        public static final String TASK_EDIT_CANCEL_LABEL = "Скасувати";


        public static final String TASK_UPDATE_NOT_FOUND = "Помилка: завдання для редагування не знайдено.";
        public static final String TASK_UPDATE_LABEL_SUCCESS = "Назву оновлено ✅";
        public static final String TASK_UPDATE_DESCRIPTION_SUCCESS = "Опис оновлено ✅";
    }

    public static final class Callback {

        private Callback() {}

        public static final String NEW_TASK = "task_new";
        public static final String PAGE_TASK = "task_page";
        public static final String IN_PROGRESS_TASK = "task_in_progress";
        public static final String COMPLETED_TASK = "task_completed";
        public static final String EDIT_TASK = "task_edit";
        public static final String DETAILS_TASK = "task_details";
        public static final String BACK_TO_TASKS = "task_back_to_list";
        public static final String DELETE_TASK = "task_delete";
        public static final String SKIP_DESCRIPTION_TASK = "task_skip_description";

        public static final String EDIT_NAME_TASK = "task_edit_name";
        public static final String EDIT_DESCRIPTION_TASK = "task_edit_description";
        public static final String CANCEL_EDIT_TASK = "task_cancel_edit";
    }
}
