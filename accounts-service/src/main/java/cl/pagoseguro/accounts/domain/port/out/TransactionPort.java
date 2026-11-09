package cl.pagoseguro.accounts.domain.port.out;

import java.util.function.Supplier;

/**
 * Puerto para ejecutar un bloque de trabajo de forma atómica: o se guarda todo, o nada.
 *
 * ¿Por qué un puerto y no @Transactional? Porque @Transactional es de Spring, y la capa
 * de aplicación no debe depender de frameworks. La infraestructura lo implementa con
 * TransactionTemplate; en los tests basta con una lambda que ejecute el bloque.
 */
public interface TransactionPort {

    <T> T inTransaction(Supplier<T> work);
}
