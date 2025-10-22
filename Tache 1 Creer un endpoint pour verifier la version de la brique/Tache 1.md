
---

# Documentation — Endpoint `/or-demat/version`

## Description

Avant de commencer les développements métier de la dématérialisation des OR, il est nécessaire d’ajouter un **endpoint simple** permettant d’exposer **la version du module `or-demat`**.

Cet endpoint servira à **vérifier que le module est présent, fonctionnel et bien déployé** dans l’environnement.

---

## Détails techniques

| Élément               | Valeur                                                   |
| --------------------- | -------------------------------------------------------- |
| **URL**               | `GET /or-demat/version`                                  |
| **Contrôleur**        | `OrDematController`                                      |
| **Package**           | `nc.opt.psp.web.rest.ordemat`                            |
| **Format de réponse** | JSON                                                     |
| **Version**           | *retournée en dur pour cette première étape* (`"1.0.0"`) |

### Exemple de réponse JSON

```json
{
  "module": "or-demat",
  "version": "1.0.0"
}
```

---

## Le Contrôleur
Ce fichier expose un endpoint HTTP accessible publiquement. Il permet de retourner la version actuelle du module or-demat. C’est ce que les autres applications ou développeurs pourront appeler pour vérifier si le module est bien en place.

```java
package nc.opt.psp.web.rest.ordemat;     // Définit le "dossier logique" du fichier (package Java)


import org.springframework.http.ResponseEntity;     // Permet de renvoyer une réponse HTTP propre (200, JSON, etc.)
import org.springframework.web.bind.annotation.GetMapping;  // Pour déclarer une route HTTP GET
import org.springframework.web.bind.annotation.RestController;  // Indique que cette classe expose des endpoints REST
import java.util.Map;  // Permet de créer un objet JSON simple sous forme clé/valeur

@RestController  // Déclare cette classe comme un contrôleur REST géré par Spring Boot
public class OrDematController {  // Nom de la classe (doit commencer par une Majuscule)

    @GetMapping("/or-demat/version")  // Spécifie l’URL de l’endpoint → accessible via GET
    public ResponseEntity<Map<String, String>> getVersion() {  // Méthode appelée quand on accède à l'URL

        Map<String, String> response = Map.of(  // Création d’un petit JSON immuable
            "module", "or-demat",  // Clé "module" → valeur "or-demat"
            "version", "1.0.0"     // Clé "version" → valeur "1.0.0" (en dur pour l’instant)
        );

        return ResponseEntity.ok(response);  // Retourne HTTP 200 OK + le JSON dans le corps de la réponse
    }
}

```

---

## Test unitaire
Ce fichier sert à vérifier automatiquement que le contrôleur fonctionne correctement.
Il valide que le JSON retourné contient bien le module et la version attendue.
Cela garantit que si quelqu’un casse ce comportement plus tard, le test le détectera immédiatement.
```java
package nc.opt.psp.web.rest.ordemat;  // Même package que le contrôleur pour rester organisé

import org.junit.jupiter.api.Test;  // Annotation pour indiquer une méthode de test
import static org.assertj.core.api.Assertions.assertThat;  // Pour faire des vérifications lisibles (assertions)
import java.util.Map;  // Pour gérer la réponse JSON sous forme clé/valeur

class OrDematControllerUnitTest {  // Nom de la classe de test

    @Test  // Indique à JUnit que ceci est un test automatisé
    void shouldReturnCorrectVersion() {  // Nom du test → décrit clairement l’intention

        OrDematController controller = new OrDematController();  // On instancie le contrôleur manuellement (pas besoin de Spring)

        var response = controller.getVersion().getBody();  // On exécute la méthode, et on récupère SEULEMENT le JSON

        assertThat(response).isNotNull();  // Vérifie que la réponse n’est pas vide
        assertThat(response.get("module")).isEqualTo("or-demat");  // Vérifie que "module" == "or-demat"
        assertThat(response.get("version")).isEqualTo("1.0.0");  // Vérifie que "version" == "1.0.0"
    }
}

```

---

## Critères d’acceptation

| Critère                                                                   | Statut |
| ------------------------------------------------------------------------- | ------ |
| **L’endpoint `/or-demat/version` est accessible sans authentification**   | ✅      |
| **Retourne un code HTTP `200 OK`**                                        | ✅      |
| **Retourne un JSON contenant `module` et `version`**                      | ✅      |
| **La réponse contient les valeurs correctes (`"or-demat"` et `"1.0.0"`)** | ✅      |
| **Un test unitaire valide ce comportement**                               | ✅      |

---

Si tu veux, je peux t’ajouter une **section "amélioration future"** (lecture dynamique via `application.properties`) ou faire un **vrai fichier `.md` prêt à copier/coller**. Souhaite-tu cela ?
