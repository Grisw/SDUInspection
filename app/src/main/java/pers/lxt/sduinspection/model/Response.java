package pers.lxt.sduinspection.model;

/**
 * The lock with data.
 * @param <T> The type of the data the lock contains.
 */
public class Response<T> {
    private int code;
    private String message;
    private T object;
    private Throwable exception;

    public Response(){ }

    public Response(Throwable exception){
        this.exception = exception;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
