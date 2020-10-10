package su.grinev.gallery.model;

public class RestErrorInfo {
    public final String detail;
    public final String message;

    public RestErrorInfo(Exception ex, String detail) {
        this.message = ex.getLocalizedMessage();
        this.detail = detail;
    }

    @Override
    public String toString(){
        return "RestErrorInfo {" +
                "detail='" + this.detail + '\'' +
                ", message='" + this.message + '\''+'}';
    }
}
