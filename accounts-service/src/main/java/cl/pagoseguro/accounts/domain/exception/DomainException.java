package cl.pagoseguro.accounts.domain.exception;

/** Se lanza cuando se viola una regla de negocio. La capa web la traduce a HTTP 422. */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
