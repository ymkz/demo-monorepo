package dev.ymkz.demo.core.domain.event;

public enum AppEvent {
    VALIDATION_FAILED("dw1001", "バリデーションエラーが発生しました"),

    DATABASE_ACCESS_FAILED("de1001", "データベースへのアクセスに失敗しました"),
    DATABASE_MYBATIS_ERROR("de2001", "MyBatisでエラーが発生しました");

    private final String code;
    private final String message;

    AppEvent(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
