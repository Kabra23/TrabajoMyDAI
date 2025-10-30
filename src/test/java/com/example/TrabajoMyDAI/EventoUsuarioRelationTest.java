package com.example.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EventoUsuarioRelationTest {
    @Autowired
    private TestEntityManager em;

    @Test
    void manyToManyAddAndRemove() {
        Usuario u = new Usuario();
        u.setNombre("María");
        u.setEmail("maria@example.com");

        Evento e = new Evento();
        e.setNombre_evento("Charla");

        // ***************************************************************
        // ESTO ES LO QUE SE CORRIGE/MODIFICA:
        // Usa el método de ayuda que sincroniza ambos lados de la relación.
        u.addEvento(e);
        // ***************************************************************

        // Es mejor persistir el lado inverso (Evento) primero y luego el Dueño (Usuario)
        // para manejar posibles Cascade.PERSIST, pero en este caso no importa si ya están enlazados.
        em.persist(e);
        em.persist(u);
        em.flush();

        // -------------------------------------------------------------
        // VERIFICACIÓN DE ADICIÓN (Línea 35 original)
        // -------------------------------------------------------------

        // Recuperar el Evento desde la DB para asegurar que la colección se cargue correctamente
        // y para forzar la lectura de la tabla de unión.
        // Dado que u.addEvento(e) YA sincronizó el objeto 'e' en memoria,
        // y el em.find carga la relación, la aserción debería pasar.
        Evento foundEvento = em.find(Evento.class, e.getId_evento());
        assertNotNull(foundEvento);

        // Si Usuario tiene 1 Evento, entonces el Evento debe tener 1 Usuario.
        // La aserción ahora usará la colección del objeto recuperado desde el lado INVERSO.
        // Esta era la línea que fallaba.
        assertEquals(1, foundEvento.getUsuarios().size()); // <-- Esta es la línea 35

        // -------------------------------------------------------------
        // VERIFICACIÓN DE ELIMINACIÓN
        // -------------------------------------------------------------

        // Ya que Usuario es el dueño, eliminarlo generará un DELETE en la tabla de unión
        // debido a la configuración de CASCADE en el lado dueño.
        // Eliminamos la relación explícitamente y verificamos que desaparece
        u.removeEvento(e);
        em.persist(u);
        em.flush();
        // Limpiar el contexto de persistencia para forzar la recarga desde la BD
        em.clear();

        // Recuperar el Evento después de eliminar la relación
        Evento afterDelete = em.find(Evento.class, e.getId_evento());
        assertNotNull(afterDelete);
        // Depuración: mostrar contenidos para entender por qué seguiría habiendo usuarios
        System.out.println("Usuarios en afterDelete: " + afterDelete.getUsuarios());

        // La aserción DEBE verificar que la relación se eliminó.
        // Si la sincronización y el mapeo son correctos, debe ser 0.
        assertTrue(afterDelete.getUsuarios().isEmpty());

        // Finalmente, eliminar el Usuario por completo y comprobar que sigue vacío
        em.remove(em.find(Usuario.class, u.getDni()));
        em.flush();
        em.clear();

        Evento afterUserRemove = em.find(Evento.class, e.getId_evento());
        assertNotNull(afterUserRemove);
        assertTrue(afterUserRemove.getUsuarios().isEmpty());
    }
}