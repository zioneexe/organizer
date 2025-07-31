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
