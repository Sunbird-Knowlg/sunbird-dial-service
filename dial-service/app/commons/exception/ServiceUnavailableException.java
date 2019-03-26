/**
 *
 * @author Rhea Fernandes
 */
package commons.exception;

public class ServiceUnavailableException extends  MiddlewareException {
    private static final long serialVersionUID = 4443445476165211068L;

    public ServiceUnavailableException(String errCode, String message) {
        super(errCode, message);
    }

    public ServiceUnavailableException(String errCode, String message, Object... params) {
        super(errCode, message, params);
    }

    public ServiceUnavailableException(String errCode, String message, Throwable root) {
        super(errCode, message, root);
    }

    public ServiceUnavailableException(String errCode, String message, Throwable root, Object... params) {
        super(errCode, message, root, params);
    }
    public ResponseCode getResponseCode() {
        return ResponseCode.SERVICE_UNAVAILABLE;
    }
}
